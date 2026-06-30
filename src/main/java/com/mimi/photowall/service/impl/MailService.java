package com.mimi.photowall.service.impl;

import com.mimi.photowall.util.DesensitizeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 * 负责发送邮件验证码
 */
@Slf4j
@Service
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code    验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("照片墙系统 - 验证码");
            message.setText(buildEmailContent(code));

            mailSender.send(message);
            log.info("验证码邮件发送成功: toEmail={}", desensitizeEmail(toEmail));
            return true;
        } catch (Exception e) {
            log.error("验证码邮件发送失败: toEmail={}, error={}", desensitizeEmail(toEmail), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建邮件内容
     *
     * @param code 验证码
     * @return 邮件内容
     */
    private String buildEmailContent(String code) {
        return "【照片墙系统】\n\n"
                + "您的验证码是：" + code + "\n\n"
                + "验证码有效期为5分钟，请勿泄露给他人。\n\n"
                + "如非本人操作，请忽略此邮件。\n\n"
                + "——————————————\n"
                + "照片墙系统";
    }

    /**
     * 邮箱脱敏
     */
    private String desensitizeEmail(String email) {
        return DesensitizeUtil.desensitizeEmail(email);
    }
}
