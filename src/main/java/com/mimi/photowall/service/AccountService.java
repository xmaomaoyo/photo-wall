package com.mimi.photowall.service;

import com.mimi.photowall.dto.account.UpdateEmailRequest;
import com.mimi.photowall.dto.account.UpdatePhoneRequest;
import com.mimi.photowall.dto.account.UpdateProfileRequest;
import com.mimi.photowall.vo.account.AccountProfileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 账号管理服务
 */
public interface AccountService {

    /**
     * 获取当前账号资料。
     *
     * @return 账号资料
     */
    AccountProfileVO getProfile();

    /**
     * 更新当前用户昵称。
     *
     * @param request 更新请求
     * @return 更新后的账号资料
     */
    AccountProfileVO updateProfile(UpdateProfileRequest request);

    /**
     * 更新当前用户头像。
     *
     * @param file 头像文件
     * @return 更新后的账号资料
     */
    AccountProfileVO updateAvatar(MultipartFile file);

    /**
     * 更新当前用户手机号。
     *
     * @param request 更新请求
     * @return 更新后的账号资料
     */
    AccountProfileVO updatePhone(UpdatePhoneRequest request);

    /**
     * 更新当前用户邮箱。
     *
     * @param request 更新请求
     * @return 更新后的账号资料
     */
    AccountProfileVO updateEmail(UpdateEmailRequest request);

    /**
     * 停用当前账号并撤销全部登录设备。
     */
    void disableAccount();
}
