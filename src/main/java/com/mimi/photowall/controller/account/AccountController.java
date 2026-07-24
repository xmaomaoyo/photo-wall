package com.mimi.photowall.controller.account;

import com.mimi.photowall.common.Result;
import com.mimi.photowall.dto.account.UpdateEmailRequest;
import com.mimi.photowall.dto.account.UpdatePhoneRequest;
import com.mimi.photowall.dto.account.UpdateProfileRequest;
import com.mimi.photowall.service.AccountService;
import com.mimi.photowall.vo.account.AccountProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 账号管理接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
@Tag(name = "账号管理", description = "用户资料、联系方式和账号状态管理")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/profile")
    @Operation(summary = "获取账号资料")
    public Result<AccountProfileVO> getProfile() {
        return Result.ok(accountService.getProfile());
    }

    @PostMapping("/profile")
    @Operation(summary = "更新昵称")
    public Result<AccountProfileVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(accountService.updateProfile(request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新头像")
    public Result<AccountProfileVO> updateAvatar(@RequestPart("file") MultipartFile file) {
        return Result.ok(accountService.updateAvatar(file));
    }

    @PostMapping("/phone")
    @Operation(summary = "更新手机号")
    public Result<AccountProfileVO> updatePhone(@Valid @RequestBody UpdatePhoneRequest request) {
        return Result.ok(accountService.updatePhone(request));
    }

    @PostMapping("/email")
    @Operation(summary = "更新邮箱")
    public Result<AccountProfileVO> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        return Result.ok(accountService.updateEmail(request));
    }

    @PostMapping("/disable")
    @Operation(summary = "停用账号")
    public Result<Void> disableAccount() {
        accountService.disableAccount();
        return Result.ok();
    }
}
