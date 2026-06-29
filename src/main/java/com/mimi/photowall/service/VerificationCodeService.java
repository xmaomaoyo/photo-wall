package com.mimi.photowall.service;

import com.mimi.photowall.enums.CodeType;

/**
 * 验证码服务接口
 * 定义验证码的生成、发送、验证等操作
 */
public interface VerificationCodeService {

    /**
     * 生成验证码
     *
     * @param length 验证码长度
     * @return 验证码
     */
    String generateCode(int length);

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号
     * @param codeType    验证码类型
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    String sendSmsCode(String phoneNumber, CodeType codeType);

    /**
     * 发送短信验证码（默认登录类型）
     *
     * @param phoneNumber 手机号
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    String sendSmsCode(String phoneNumber);

    /**
     * 发送邮箱验证码
     *
     * @param email    邮箱
     * @param codeType 验证码类型
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    String sendEmailCode(String email, CodeType codeType);

    /**
     * 发送邮箱验证码（默认登录类型）
     *
     * @param email 邮箱
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    String sendEmailCode(String email);

    /**
     * 验证验证码
     *
     * @param type   验证码类型
     * @param target 目标（手机号或邮箱）
     * @param code   验证码
     * @return 是否验证成功
     */
    boolean verifyCode(CodeType type, String target, String code);

    /**
     * 检查频率限制
     *
     * @param type   验证码类型
     * @param target 目标（手机号或邮箱）
     * @return 是否可以发送
     */
    boolean checkRateLimit(String type, String target);

    /**
     * 检查每日发送限制
     *
     * @param phoneNumber 手机号
     * @return 是否可以发送
     */
    boolean checkDailyLimit(String phoneNumber);

    /**
     * 生成图形验证码
     *
     * @return 验证码ID
     */
    String generateCaptcha();

    /**
     * 输出验证码图片到响应流
     *
     * @param captchaId 验证码ID
     * @param response  HTTP响应对象
     */
    void writeCaptchaImage(String captchaId, jakarta.servlet.http.HttpServletResponse response);

    /**
     * 验证图形验证码
     *
     * @param captchaId    验证码ID
     * @param captchaCode  用户输入的验证码
     * @return 是否验证成功
     */
    boolean verifyCaptcha(String captchaId, String captchaCode);
}
