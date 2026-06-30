package com.mimi.photowall.service;

import com.mimi.photowall.dto.auth.*;
import com.mimi.photowall.vo.auth.DeviceVO;
import com.mimi.photowall.vo.auth.LoginVO;
import com.mimi.photowall.vo.auth.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 认证服务接口
 * 定义登录、注册、Token管理等操作
 */
public interface AuthService {

    /**
     * 生成图形验证码
     *
     * @return 验证码ID
     */
    String generateCaptcha();

    /**
     * 输出验证码图片
     *
     * @param captchaId 验证码ID
     * @param response  HTTP响应对象
     */
    void writeCaptchaImage(String captchaId, HttpServletResponse response);

    /**
     * 手机号+短信验证码登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @return 登录结果
     */
    LoginVO smsLogin(SmsLoginRequest request, HttpServletRequest httpRequest);

    /**
     * 邮箱+验证码登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @return 登录结果
     */
    LoginVO emailLogin(EmailLoginRequest request, HttpServletRequest httpRequest);

    /**
     * 密码登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @return 登录结果
     */
    LoginVO passwordLogin(PasswordLoginRequest request, HttpServletRequest httpRequest);

    /**
     * 用户注册
     *
     * @param request     注册请求
     * @param httpRequest HTTP请求
     * @return 登录结果
     */
    LoginVO register(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * 刷新Token
     *
     * @param refreshToken Refresh Token
     * @return 新的Token信息
     */
    LoginVO refreshToken(String refreshToken, HttpServletRequest httpRequest);

    /**
     * 登出
     *
     * @param authorization Authorization头
     * @param refreshToken  Refresh Token
     */
    void logout(String authorization, String refreshToken);

    /**
     * 踢出所有设备
     *
     * @param authorization Authorization头
     */
    void logoutAll(String authorization);

    /**
     * 获取当前用户信息
     *
     * @param authorization Authorization头
     * @return 用户信息
     */
    UserInfoVO getCurrentUser(String authorization);

    /**
     * 获取登录设备列表
     *
     * @param authorization Authorization头
     * @return 设备列表
     */
    List<DeviceVO> getDevices(String authorization);

    /**
     * 踢出指定设备
     *
     * @param authorization Authorization头
     * @param deviceId      设备ID
     */
    void removeDevice(String authorization, String deviceId);

    /**
     * 忘记密码（发送验证码）
     *
     * @param request 请求
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * 重置密码
     *
     * @param request 请求
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * 修改密码（已登录）
     *
     * @param authorization Authorization头
     * @param request       请求
     */
    void changePassword(String authorization, ChangePasswordRequest request);
}
