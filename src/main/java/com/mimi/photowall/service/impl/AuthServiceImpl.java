package com.mimi.photowall.service.impl;

import com.mimi.photowall.dto.auth.*;
import com.mimi.photowall.entity.User;
import com.mimi.photowall.enums.CodeType;
import com.mimi.photowall.enums.ResultCode;
import com.mimi.photowall.exception.BusinessException;
import com.mimi.photowall.service.*;
import com.mimi.photowall.util.DesensitizeUtil;
import com.mimi.photowall.util.PasswordUtil;
import com.mimi.photowall.vo.auth.DeviceVO;
import com.mimi.photowall.vo.auth.LoginVO;
import com.mimi.photowall.vo.auth.TokenVO;
import com.mimi.photowall.vo.auth.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 认证服务实现类
 * 实现登录、注册、Token管理等操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final TokenService tokenService;
    private final VerificationCodeService verificationCodeService;

    @Override
    public String generateCaptcha() {
        return verificationCodeService.generateCaptcha();
    }

    @Override
    public void writeCaptchaImage(String captchaId, HttpServletResponse response) {
        verificationCodeService.writeCaptchaImage(captchaId, response);
    }

    @Override
    public LoginVO smsLogin(SmsLoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = getUserAgent(httpRequest);
        // 验证验证码
        if (!verificationCodeService.verifyCode(CodeType.LOGIN, request.getPhone(), request.getCode())) {
            throw new BusinessException(ResultCode.VERIFICATION_CODE_ERROR);
        }

        // 查询用户
        User user = userService.getUserByPhone(request.getPhone());

        // 用户不存在，自动创建
        boolean isNewUser = false;
        if (user == null) {
            user = createAutoUser(request.getPhone(), null);
            roleService.assignUserRoles(user.getId(), List.of(2L));
            isNewUser = true;
        }

        // 检查用户状态
        checkUserStatus(user);

        // 生成Token并返回
        return buildLoginVO(user, ip, userAgent, isNewUser);
    }

    @Override
    public LoginVO emailLogin(EmailLoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = getUserAgent(httpRequest);
        // 验证验证码
        if (!verificationCodeService.verifyCode(CodeType.LOGIN, request.getEmail(), request.getCode())) {
            throw new BusinessException(ResultCode.VERIFICATION_CODE_ERROR);
        }

        // 查询用户
        User user = userService.getUserByEmail(request.getEmail());

        // 用户不存在，自动创建
        boolean isNewUser = false;
        if (user == null) {
            user = createAutoUser(null, request.getEmail());
            roleService.assignUserRoles(user.getId(), List.of(2L));
            isNewUser = true;
        }

        // 检查用户状态
        checkUserStatus(user);

        // 生成Token并返回
        return buildLoginVO(user, ip, userAgent, isNewUser);
    }

    @Override
    public LoginVO passwordLogin(PasswordLoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = getUserAgent(httpRequest);
        // 验证图形验证码
        if (!verificationCodeService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaCode())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }

        // 查询用户
        User user = findUserByAccount(request.getAccount());
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_OR_PASSWORD_ERROR);
        }

        // 验证密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.ACCOUNT_OR_PASSWORD_ERROR);
        }

        // 检查用户状态
        checkUserStatus(user);

        // 生成Token并返回
        return buildLoginVO(user, ip, userAgent, false);
    }

    @Override
    public LoginVO register(RegisterRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = getUserAgent(httpRequest);
        // 验证密码格式
        if (!PasswordUtil.isValidFormat(request.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 验证确认密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码不一致");
        }

        // 手机号必填
        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号不能为空");
        }

        // 验证验证码（使用手机号）
        if (!verificationCodeService.verifyCode(CodeType.REGISTER, request.getPhone(), request.getCode())) {
            throw new BusinessException(ResultCode.VERIFICATION_CODE_ERROR);
        }

        // 检查用户名是否已存在
        if (userService.isUsernameExists(request.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        // 检查手机号是否已注册
        if (userService.isPhoneRegistered(request.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_REGISTERED);
        }

        // 检查邮箱是否已注册（如果填写了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userService.isEmailRegistered(request.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_REGISTERED);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getUsername());
        user = userService.createUser(user);

        // 分配默认角色
        roleService.assignUserRoles(user.getId(), List.of(2L));

        // 生成Token并返回
        return buildLoginVO(user, ip, userAgent, false);
    }

    @Override
    public LoginVO refreshToken(String refreshToken, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = getUserAgent(httpRequest);
        // 刷新Token
        String[] tokens = tokenService.refreshTokenPair(refreshToken, ip, userAgent);

        // 获取用户信息
        Long userId = tokenService.getUserIdFromAccessToken(tokens[0]);
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        List<String> roles = roleService.getUserRoleCodes(userId);

        return LoginVO.builder()
                .token(TokenVO.builder()
                        .accessToken(tokens[0])
                        .refreshToken(tokens[1])
                        .expiresIn(1800L)
                        .build())
                .userInfo(buildUserInfoVO(user, roles))
                .needResetPassword(false)
                .build();
    }

    @Override
    public void logout(String authorization, String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    public void logoutAll(String authorization) {
        String accessToken = extractToken(authorization);
        Long userId = tokenService.getUserIdFromAccessToken(accessToken);
        if (userId != null) {
            tokenService.revokeAllUserTokens(userId);
        }
    }

    @Override
    public UserInfoVO getCurrentUser(String authorization) {
        String accessToken = extractToken(authorization);
        Long userId = tokenService.getUserIdFromAccessToken(accessToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        List<String> roles = roleService.getUserRoleCodes(userId);
        return buildUserInfoVO(user, roles);
    }

    @Override
    public List<DeviceVO> getDevices(String authorization) {
        String accessToken = extractToken(authorization);
        Long userId = tokenService.getUserIdFromAccessToken(accessToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        List<java.util.Map<String, Object>> devices = tokenService.getUserDevices(userId);
        List<DeviceVO> result = new ArrayList<>();

        for (java.util.Map<String, Object> device : devices) {
            result.add(DeviceVO.builder()
                    .deviceId((String) device.get("id"))
                    .deviceName((String) device.get("deviceName"))
                    .ip((String) device.get("ip"))
                    .lastActiveTime(null)
                    .current(false)
                    .build());
        }

        return result;
    }

    @Override
    public void removeDevice(String authorization, String deviceId) {
        String accessToken = extractToken(authorization);
        Long userId = tokenService.getUserIdFromAccessToken(accessToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        tokenService.removeDevice(userId, deviceId);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        if (request.getPhone() != null) {
            verificationCodeService.sendSmsCode(request.getPhone());
        } else if (request.getEmail() != null) {
            verificationCodeService.sendEmailCode(request.getEmail());
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号或邮箱必须填一个");
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // 验证密码格式
        if (!PasswordUtil.isValidFormat(request.getNewPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 验证确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码不一致");
        }

        // 验证手机号或邮箱
        if (request.getPhone() == null && request.getEmail() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号或邮箱必须填一个");
        }

        // 验证验证码
        String target = request.getPhone() != null ? request.getPhone() : request.getEmail();
        if (!verificationCodeService.verifyCode(CodeType.RESET_PWD, target, request.getCode())) {
            throw new BusinessException(ResultCode.VERIFICATION_CODE_ERROR);
        }

        // 查询用户
        User user = request.getPhone() != null
                ? userService.getUserByPhone(request.getPhone())
                : userService.getUserByEmail(request.getEmail());

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新密码
        userService.updatePassword(user.getId(), PasswordUtil.encode(request.getNewPassword()));

        // 踢出所有设备
        tokenService.revokeAllUserTokens(user.getId());
    }

    @Override
    public void changePassword(String authorization, ChangePasswordRequest request) {
        String accessToken = extractToken(authorization);
        Long userId = tokenService.getUserIdFromAccessToken(accessToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!PasswordUtil.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.ACCOUNT_OR_PASSWORD_ERROR, "旧密码错误");
        }

        // 验证密码格式
        if (!PasswordUtil.isValidFormat(request.getNewPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 验证确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码不一致");
        }

        // 更新密码
        userService.updatePassword(userId, PasswordUtil.encode(request.getNewPassword()));

        // 踢出所有设备
        tokenService.revokeAllUserTokens(userId);
    }

    // ==================== 私有方法 ====================

    /**
     * 根据账号查找用户（支持手机号、邮箱、用户名）
     */
    private User findUserByAccount(String account) {
        if (account.matches("^1[3-9]\\d{9}$")) {
            return userService.getUserByPhone(account);
        } else if (account.contains("@")) {
            return userService.getUserByEmail(account);
        } else {
            return userService.getUserByUsername(account);
        }
    }

    /**
     * 自动创建用户
     */
    private User createAutoUser(String phone, String email) {
        User user = new User();
        if (phone != null) {
            user.setPhone(phone);
            user.setUsername("user_" + phone);
            user.setNickname("用户" + DesensitizeUtil.desensitizePhone(phone));
        } else {
            user.setEmail(email);
            user.setUsername("user_" + email.split("@")[0]);
            user.setNickname("用户" + DesensitizeUtil.desensitizeEmail(email));
        }
        user.setPassword(PasswordUtil.encode(PasswordUtil.generateRandomPassword(12)));
        return userService.createUser(user);
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(User user) {
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
    }

    /**
     * 构建登录返回对象
     */
    private LoginVO buildLoginVO(User user, String ip, String userAgent, boolean isNewUser) {
        List<String> roles = roleService.getUserRoleCodes(user.getId());
        String deviceId = UUID.randomUUID().toString();

        String[] tokens = tokenService.generateTokenPair(
                user.getId(), user.getUsername(), roles, deviceId, ip, userAgent);

        userService.updateLastLoginInfo(user.getId(), ip);

        return LoginVO.builder()
                .token(TokenVO.builder()
                        .accessToken(tokens[0])
                        .refreshToken(tokens[1])
                        .expiresIn(1800L)
                        .build())
                .userInfo(buildUserInfoVO(user, roles))
                .needResetPassword(isNewUser)
                .build();
    }

    /**
     * 构建用户信息VO
     */
    private UserInfoVO buildUserInfoVO(User user, List<String> roles) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(DesensitizeUtil.desensitizePhone(user.getPhone()))
                .email(DesensitizeUtil.desensitizeEmail(user.getEmail()))
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roles)
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .build();
    }

    /**
     * 从Authorization头中提取Token
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取User-Agent
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
