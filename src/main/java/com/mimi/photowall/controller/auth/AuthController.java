package com.mimi.photowall.controller.auth;

import com.mimi.photowall.common.Result;
import com.mimi.photowall.dto.auth.*;
import com.mimi.photowall.enums.CodeType;
import com.mimi.photowall.service.AuthService;
import com.mimi.photowall.service.VerificationCodeService;
import com.mimi.photowall.vo.auth.DeviceVO;
import com.mimi.photowall.vo.auth.LoginVO;
import com.mimi.photowall.vo.auth.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 * 处理登录、注册、Token管理等请求
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理", description = "登录、注册、Token管理等接口")
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;

    /**
     * 获取图形验证码ID
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码ID", description = "生成验证码并返回ID")
    public Result<String> getCaptcha() {
        String captchaId = authService.generateCaptcha();
        return Result.ok(captchaId);
    }

    /**
     * 获取验证码图片
     */
    @GetMapping("/captcha/{captchaId}")
    @Operation(summary = "获取验证码图片", description = "根据验证码ID获取验证码图片")
    public void getCaptchaImage(@PathVariable String captchaId, HttpServletResponse response) {
        authService.writeCaptchaImage(captchaId, response);
    }

    /**
     * 发送短信验证码（登录用）
     */
    @PostMapping("/sms/send")
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送登录验证码")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsSendRequest request) {
        verificationCodeService.sendSmsCode(request.getPhone(), CodeType.LOGIN);
        return Result.ok();
    }

    /**
     * 发送短信验证码（注册用）
     */
    @PostMapping("/sms/send/register")
    @Operation(summary = "发送注册短信验证码", description = "向指定手机号发送注册验证码")
    public Result<Void> sendSmsCodeForRegister(@Valid @RequestBody SmsSendRequest request) {
        verificationCodeService.sendSmsCode(request.getPhone(), CodeType.REGISTER);
        return Result.ok();
    }

    /**
     * 发送短信验证码（重置密码用）
     */
    @PostMapping("/sms/send/reset")
    @Operation(summary = "发送重置密码短信验证码", description = "向指定手机号发送重置密码验证码")
    public Result<Void> sendSmsCodeForReset(@Valid @RequestBody SmsSendRequest request) {
        verificationCodeService.sendSmsCode(request.getPhone(), CodeType.RESET_PWD);
        return Result.ok();
    }

    /**
     * 手机号+短信验证码登录
     */
    @PostMapping("/sms/login")
    @Operation(summary = "手机号+短信验证码登录", description = "使用手机号和验证码登录")
    public Result<LoginVO> smsLogin(
            @Valid @RequestBody SmsLoginRequest request,
            HttpServletRequest httpRequest) {
        LoginVO result = authService.smsLogin(request, httpRequest);
        return Result.ok(result);
    }

    /**
     * 发送邮箱验证码（登录用）
     */
    @PostMapping("/email/send")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送登录验证码")
    public Result<Void> sendEmailCode(@Valid @RequestBody EmailSendRequest request) {
        verificationCodeService.sendEmailCode(request.getEmail(), CodeType.LOGIN);
        return Result.ok();
    }

    /**
     * 发送邮箱验证码（注册用）
     */
    @PostMapping("/email/send/register")
    @Operation(summary = "发送注册邮箱验证码", description = "向指定邮箱发送注册验证码")
    public Result<Void> sendEmailCodeForRegister(@Valid @RequestBody EmailSendRequest request) {
        verificationCodeService.sendEmailCode(request.getEmail(), CodeType.REGISTER);
        return Result.ok();
    }

    /**
     * 发送邮箱验证码（重置密码用）
     */
    @PostMapping("/email/send/reset")
    @Operation(summary = "发送重置密码邮箱验证码", description = "向指定邮箱发送重置密码验证码")
    public Result<Void> sendEmailCodeForReset(@Valid @RequestBody EmailSendRequest request) {
        verificationCodeService.sendEmailCode(request.getEmail(), CodeType.RESET_PWD);
        return Result.ok();
    }

    /**
     * 邮箱+验证码登录
     */
    @PostMapping("/email/login")
    @Operation(summary = "邮箱+验证码登录", description = "使用邮箱和验证码登录")
    public Result<LoginVO> emailLogin(
            @Valid @RequestBody EmailLoginRequest request,
            HttpServletRequest httpRequest) {
        LoginVO result = authService.emailLogin(request, httpRequest);
        return Result.ok(result);
    }

    /**
     * 密码登录
     */
    @PostMapping("/password/login")
    @Operation(summary = "密码登录", description = "使用账号和密码登录，需要图形验证码")
    public Result<LoginVO> passwordLogin(
            @Valid @RequestBody PasswordLoginRequest request,
            HttpServletRequest httpRequest) {
        LoginVO result = authService.passwordLogin(request, httpRequest);
        return Result.ok(result);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户（手机号必填，邮箱选填）")
    public Result<LoginVO> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        LoginVO result = authService.register(request, httpRequest);
        return Result.ok(result);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用Refresh Token获取新的Token对")
    public Result<LoginVO> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        LoginVO result = authService.refreshToken(refreshToken);
        return Result.ok(result);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    @Operation(summary = "登出", description = "登出当前设备")

    public Result<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        authService.logout(authorization, refreshToken);
        return Result.ok();
    }

    /**
     * 踢出所有设备
     */
    @PostMapping("/logout-all")
    @Operation(summary = "踢出所有设备", description = "登出所有设备")
    public Result<Void> logoutAll(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logoutAll(authorization);
        return Result.ok();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息")
    public Result<UserInfoVO> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserInfoVO result = authService.getCurrentUser(authorization);
        return Result.ok(result);
    }

    /**
     * 获取登录设备列表
     */
    @GetMapping("/devices")
    @Operation(summary = "获取登录设备列表", description = "获取当前用户的所有登录设备")
    public Result<List<DeviceVO>> getDevices(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        List<DeviceVO> result = authService.getDevices(authorization);
        return Result.ok(result);
    }

    /**
     * 踢出指定设备
     */
    @PostMapping("/devices/remove")
    @Operation(summary = "踢出指定设备", description = "踢出指定设备的登录")
    public Result<Void> removeDevice(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RemoveDeviceRequest request) {
        authService.removeDevice(authorization, request.getDeviceId());
        return Result.ok();
    }

    /**
     * 忘记密码（发送验证码）
     */
    @PostMapping("/password/forgot")
    @Operation(summary = "忘记密码", description = "发送密码重置验证码（手机号或邮箱二选一）")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return Result.ok();
    }

    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    @Operation(summary = "重置密码", description = "使用验证码重置密码（手机号或邮箱二选一）")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.ok();
    }

    /**
     * 修改密码（已登录）
     */
    @PostMapping("/password/change")
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    public Result<Void> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authorization, request);
        return Result.ok();
    }
}
