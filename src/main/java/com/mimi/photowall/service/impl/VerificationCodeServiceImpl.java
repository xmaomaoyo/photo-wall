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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 * 实现验证码的生成、发送、验证等操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private final AliyunSmsConfig aliyunSmsConfig;
    private final AliyunSmsService aliyunSmsService;
    private final MailService mailService;

    /**
     * 验证码图片缓存（captchaId -> SpecCaptcha对象）
     */
    private final Map<String, SpecCaptcha> captchaCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 验证码存储Key前缀
     */
    private static final String VERIFY_CODE_PREFIX = "verify:";

    /**
     * 频率限制Key前缀
     */
    private static final String RATE_LIMIT_PREFIX = "rate:";

    /**
     * 每日发送限制Key前缀
     */
    private static final String DAILY_LIMIT_PREFIX = "daily:";

    /**
     * 图形验证码Key前缀
     */
    private static final String CAPTCHA_PREFIX = "captcha:";

    @Override
    public String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }

    @Override
    public String sendSmsCode(String phoneNumber) {
        return sendSmsCode(phoneNumber, CodeType.LOGIN);
    }

    @Override
    public String sendSmsCode(String phoneNumber, CodeType codeType) {
        // 检查频率限制
        if (!checkRateLimit("sms", phoneNumber)) {
            throw new RuntimeException("发送过于频繁，请60秒后再试");
        }

        // 检查每日发送限制
        if (!checkDailyLimit(phoneNumber)) {
            throw new RuntimeException("今日发送次数已达上限");
        }

        // 生成验证码
        String code = generateCode(6);

        // 存储到Redis
        String verifyKey = VERIFY_CODE_PREFIX + codeType.getCode() + ":sms:" + phoneNumber;
        Map<String, String> codeInfo = new HashMap<>();
        codeInfo.put("code", code);
        codeInfo.put("attempts", "0");
        codeInfo.put("createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(verifyKey, codeInfo);
        redisTemplate.expire(verifyKey, aliyunSmsConfig.getExpire(), TimeUnit.SECONDS);

        // 设置频率限制（60秒）
        String rateKey = RATE_LIMIT_PREFIX + "sms:" + phoneNumber;
        redisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);

        // 增加每日发送计数
        String dailyKey = DAILY_LIMIT_PREFIX + "sms:" + phoneNumber;
        redisTemplate.opsForValue().increment(dailyKey);
        redisTemplate.expire(dailyKey, 24, TimeUnit.HOURS);

        // 调用阿里云SDK发送短信
        boolean sendResult = aliyunSmsService.sendSmsCode(phoneNumber, code);
        if (!sendResult) {
            // 发送失败，删除已存储的验证码
            redisTemplate.delete(verifyKey);
            redisTemplate.delete(rateKey);
            redisTemplate.delete(dailyKey);
            throw new RuntimeException("短信发送失败，请稍后再试");
        }

        log.info("短信验证码已发送: phoneNumber={}, type={}", desensitizePhone(phoneNumber), codeType.getCode());

        return code;
    }

    @Override
    public String sendEmailCode(String email) {
        return sendEmailCode(email, CodeType.LOGIN);
    }

    @Override
    public String sendEmailCode(String email, CodeType codeType) {
        // 检查频率限制
        if (!checkRateLimit("email", email)) {
            throw new RuntimeException("发送过于频繁，请60秒后再试");
        }

        // 生成验证码
        String code = generateCode(6);

        // 存储到Redis
        String verifyKey = VERIFY_CODE_PREFIX + codeType.getCode() + ":email:" + email;
        Map<String, String> codeInfo = new HashMap<>();
        codeInfo.put("code", code);
        codeInfo.put("attempts", "0");
        codeInfo.put("createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(verifyKey, codeInfo);
        redisTemplate.expire(verifyKey, aliyunSmsConfig.getExpire(), TimeUnit.SECONDS);

        // 设置频率限制（60秒）
        String rateKey = RATE_LIMIT_PREFIX + "email:" + email;
        redisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);

        // 调用邮件服务发送验证码
        boolean sendResult = mailService.sendVerificationCode(email, code);
        if (!sendResult) {
            // 发送失败，删除已存储的验证码
            redisTemplate.delete(verifyKey);
            redisTemplate.delete(rateKey);
            throw new RuntimeException("邮件发送失败，请稍后再试");
        }

        log.info("邮箱验证码已发送: email={}, type={}", desensitizeEmail(email), codeType.getCode());

        return code;
    }

    @Override
    public boolean verifyCode(CodeType type, String target, String code) {
        String verifyKey = VERIFY_CODE_PREFIX + type.getCode() + ":" +
                (target.contains("@") ? "email:" : "sms:") + target;

        Map<Object, Object> codeInfo = redisTemplate.opsForHash().entries(verifyKey);
        if (codeInfo.isEmpty()) {
            return false;
        }

        // 检查尝试次数
        int attempts = Integer.parseInt(codeInfo.get("attempts").toString());
        if (attempts >= aliyunSmsConfig.getMaxAttempts()) {
            redisTemplate.delete(verifyKey);
            return false;
        }

        // 验证码比对
        String storedCode = codeInfo.get("code").toString();
        if (!storedCode.equals(code)) {
            // 增加尝试次数
            redisTemplate.opsForHash().increment(verifyKey, "attempts", 1);
            return false;
        }

        // 验证成功，删除验证码
        redisTemplate.delete(verifyKey);
        return true;
    }

    @Override
    public boolean checkRateLimit(String type, String target) {
        String rateKey = RATE_LIMIT_PREFIX + type + ":" + target;
        return !redisTemplate.hasKey(rateKey);
    }

    @Override
    public boolean checkDailyLimit(String phoneNumber) {
        String dailyKey = DAILY_LIMIT_PREFIX + "sms:" + phoneNumber;
        String count = redisTemplate.opsForValue().get(dailyKey);
        if (count == null) {
            return true;
        }
        return Integer.parseInt(count) < aliyunSmsConfig.getDailyLimit();
    }

    @Override
    public String generateCaptcha() {
        // 使用EasyCaptcha生成验证码
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text().toLowerCase();

        // 生成唯一ID
        String captchaId = UUID.randomUUID().toString();

        // 存储验证码文本到Redis
        String captchaKey = CAPTCHA_PREFIX + captchaId;
        redisTemplate.opsForValue().set(captchaKey, code, 5, TimeUnit.MINUTES);

        // 缓存验证码图片对象
        captchaCache.put(captchaId, captcha);

        // 5分钟后自动清理缓存
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

        // 验证成功后删除验证码
        redisTemplate.delete(captchaKey);

        // 大小写不敏感比较
        return storedCode.equalsIgnoreCase(captchaCode);
    }

    /**
     * 手机号脱敏
     */
    private String desensitizePhone(String phone) {
        return DesensitizeUtil.desensitizePhone(phone);
    }

    /**
     * 邮箱脱敏
     */
    private String desensitizeEmail(String email) {
        return DesensitizeUtil.desensitizeEmail(email);
    }
}
