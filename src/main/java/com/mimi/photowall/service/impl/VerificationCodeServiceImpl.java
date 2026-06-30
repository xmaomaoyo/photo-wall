package com.mimi.photowall.service.impl;

import com.mimi.photowall.config.AliyunSmsConfig;
import com.mimi.photowall.enums.CodeType;
import com.mimi.photowall.service.VerificationCodeService;
import com.mimi.photowall.util.DesensitizeUtil;
import com.wf.captcha.SpecCaptcha;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final String VERIFY_CODE_PREFIX = "verify:";
    private static final String RATE_LIMIT_PREFIX = "rate:";
    private static final String DAILY_LIMIT_PREFIX = "daily:";
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int RATE_LIMIT_SECONDS = 60;
    private static final int DAILY_LIMIT_HOURS = 24;

    private final StringRedisTemplate redisTemplate;
    private final AliyunSmsConfig aliyunSmsConfig;
    private final AliyunSmsService aliyunSmsService;
    private final MailService mailService;
    private final Map<String, SpecCaptcha> captchaCache = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }

    @Override
    public String sendSmsCode(String phoneNumber) {
        return sendSmsCode(phoneNumber, CodeType.LOGIN);
    }

    @Override
    public String sendSmsCode(String phoneNumber, CodeType codeType) {
        if (!checkRateLimit("sms", phoneNumber)) {
            throw new RuntimeException("发送过于频繁，请60秒后再试");
        }
        if (!checkDailyLimit("sms", phoneNumber)) {
            throw new RuntimeException("今日发送次数已达上限");
        }

        String code = generateCode(DEFAULT_CODE_LENGTH);
        String verifyKey = VERIFY_CODE_PREFIX + codeType.getCode() + ":sms:" + phoneNumber;
        saveCode(verifyKey, code);

        String rateKey = RATE_LIMIT_PREFIX + "sms:" + phoneNumber;
        redisTemplate.opsForValue().set(rateKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        String dailyKey = buildDailyKey("sms", phoneNumber);
        increaseDailyCount(dailyKey);

        boolean sendResult = aliyunSmsService.sendSmsCode(phoneNumber, code);
        if (!sendResult) {
            redisTemplate.delete(verifyKey);
            redisTemplate.delete(rateKey);
            redisTemplate.delete(dailyKey);
            throw new RuntimeException("短信发送失败，请稍后再试");
        }

        log.info("短信验证码已发送 phoneNumber={}, type={}", desensitizePhone(phoneNumber), codeType.getCode());
        return code;
    }

    @Override
    public String sendEmailCode(String email) {
        return sendEmailCode(email, CodeType.LOGIN);
    }

    @Override
    public String sendEmailCode(String email, CodeType codeType) {
        if (!checkRateLimit("email", email)) {
            throw new RuntimeException("发送过于频繁，请60秒后再试");
        }
        if (!checkDailyLimit("email", email)) {
            throw new RuntimeException("今日发送次数已达上限");
        }

        String code = generateCode(DEFAULT_CODE_LENGTH);
        String verifyKey = VERIFY_CODE_PREFIX + codeType.getCode() + ":email:" + email;
        saveCode(verifyKey, code);

        String rateKey = RATE_LIMIT_PREFIX + "email:" + email;
        redisTemplate.opsForValue().set(rateKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        String dailyKey = buildDailyKey("email", email);
        increaseDailyCount(dailyKey);

        boolean sendResult = mailService.sendVerificationCode(email, code);
        if (!sendResult) {
            redisTemplate.delete(verifyKey);
            redisTemplate.delete(rateKey);
            redisTemplate.delete(dailyKey);
            throw new RuntimeException("邮件发送失败，请稍后再试");
        }

        log.info("邮箱验证码已发送 email={}, type={}", desensitizeEmail(email), codeType.getCode());
        return code;
    }

    @Override
    public boolean verifyCode(CodeType type, String target, String code) {
        String verifyKey = VERIFY_CODE_PREFIX + type.getCode() + ":"
                + (target.contains("@") ? "email:" : "sms:") + target;

        Map<Object, Object> codeInfo = redisTemplate.opsForHash().entries(verifyKey);
        if (codeInfo.isEmpty()) {
            return false;
        }

        int attempts = Integer.parseInt(codeInfo.get("attempts").toString());
        if (attempts >= aliyunSmsConfig.getMaxAttempts()) {
            redisTemplate.delete(verifyKey);
            return false;
        }

        String storedCode = codeInfo.get("code").toString();
        if (!storedCode.equals(code)) {
            redisTemplate.opsForHash().increment(verifyKey, "attempts", 1);
            return false;
        }

        redisTemplate.delete(verifyKey);
        return true;
    }

    @Override
    public boolean checkRateLimit(String type, String target) {
        String rateKey = RATE_LIMIT_PREFIX + type + ":" + target;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(rateKey));
    }

    @Override
    public boolean checkDailyLimit(String phoneNumber) {
        return checkDailyLimit("sms", phoneNumber);
    }

    @Override
    public String generateCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text().toLowerCase();
        String captchaId = UUID.randomUUID().toString();

        String captchaKey = CAPTCHA_PREFIX + captchaId;
        redisTemplate.opsForValue().set(captchaKey, code, 5, TimeUnit.MINUTES);
        captchaCache.put(captchaId, captcha);
        CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES).execute(() -> captchaCache.remove(captchaId));

        return captchaId;
    }

    @Override
    public void writeCaptchaImage(String captchaId, HttpServletResponse response) {
        SpecCaptcha captcha = captchaCache.get(captchaId);
        if (captcha == null) {
            throw new RuntimeException("验证码已过期或不存在");
        }

        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("输出验证码图片失败", e);
            throw new RuntimeException("输出验证码图片失败");
        }
    }

    @Override
    public boolean verifyCaptcha(String captchaId, String captchaCode) {
        String captchaKey = CAPTCHA_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(captchaKey);

        if (storedCode == null) {
            return false;
        }

        redisTemplate.delete(captchaKey);
        return storedCode.equalsIgnoreCase(captchaCode);
    }

    private void saveCode(String verifyKey, String code) {
        Map<String, String> codeInfo = new HashMap<>();
        codeInfo.put("code", code);
        codeInfo.put("attempts", "0");
        codeInfo.put("createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(verifyKey, codeInfo);
        redisTemplate.expire(verifyKey, aliyunSmsConfig.getExpire(), TimeUnit.SECONDS);
    }

    private boolean checkDailyLimit(String type, String target) {
        String count = redisTemplate.opsForValue().get(buildDailyKey(type, target));
        if (count == null) {
            return true;
        }
        return Integer.parseInt(count) < aliyunSmsConfig.getDailyLimit();
    }

    private void increaseDailyCount(String dailyKey) {
        redisTemplate.opsForValue().increment(dailyKey);
        redisTemplate.expire(dailyKey, DAILY_LIMIT_HOURS, TimeUnit.HOURS);
    }

    private String buildDailyKey(String type, String target) {
        return DAILY_LIMIT_PREFIX + type + ":" + target;
    }

    private String desensitizePhone(String phone) {
        return DesensitizeUtil.desensitizePhone(phone);
    }

    private String desensitizeEmail(String email) {
        return DesensitizeUtil.desensitizeEmail(email);
    }
}
