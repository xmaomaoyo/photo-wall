package com.mimi.photowall.service;

import com.mimi.photowall.common.UserPrincipal;
import com.mimi.photowall.config.PhotoUploadProperties;
import com.mimi.photowall.dto.account.UpdateEmailRequest;
import com.mimi.photowall.dto.account.UpdatePhoneRequest;
import com.mimi.photowall.entity.User;
import com.mimi.photowall.enums.ResultCode;
import com.mimi.photowall.exception.BusinessException;
import com.mimi.photowall.service.impl.AccountServiceImpl;
import com.mimi.photowall.vo.account.AccountProfileVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTests {

    private static final Long CURRENT_USER_ID = 1001L;

    private UserService userService;

    private TokenService tokenService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        tokenService = mock(TokenService.class);
        accountService = new AccountServiceImpl(userService, tokenService, new PhotoUploadProperties());
        mockCurrentUser();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfileShouldDesensitizePhoneAndEmail() {
        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser());

        AccountProfileVO profile = accountService.getProfile();

        assertThat(profile.getPhone()).isEqualTo("138****8000");
        assertThat(profile.getEmail()).isEqualTo("t***@example.com");
        assertThat(profile.getPhoneBound()).isTrue();
        assertThat(profile.getEmailBound()).isTrue();
    }

    @Test
    void updatePhoneShouldRejectPhoneOwnedByAnotherUser() {
        User currentUser = currentUser();
        User registeredUser = new User();
        registeredUser.setId(2002L);
        registeredUser.setPhone("13900139000");
        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(userService.getUserByPhone("13900139000")).thenReturn(registeredUser);
        UpdatePhoneRequest request = new UpdatePhoneRequest();
        request.setPhone("13900139000");

        assertThatThrownBy(() -> accountService.updatePhone(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.PHONE_ALREADY_REGISTERED.getCode());
        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    void updateEmailShouldRejectEmailOwnedByAnotherUser() {
        User currentUser = currentUser();
        User registeredUser = new User();
        registeredUser.setId(2002L);
        registeredUser.setEmail("used@example.com");
        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(userService.getUserByEmail("used@example.com")).thenReturn(registeredUser);
        UpdateEmailRequest request = new UpdateEmailRequest();
        request.setEmail("Used@Example.com");

        assertThatThrownBy(() -> accountService.updateEmail(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.EMAIL_ALREADY_REGISTERED.getCode());
        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    void disableAccountShouldUpdateStatusAndRevokeAllTokens() {
        User currentUser = currentUser();
        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);

        accountService.disableAccount();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(CURRENT_USER_ID);
        assertThat(userCaptor.getValue().getStatus()).isZero();
        verify(tokenService).revokeAllUserTokens(CURRENT_USER_ID);
    }

    private User currentUser() {
        User user = new User();
        user.setId(CURRENT_USER_ID);
        user.setUsername("testuser");
        user.setNickname("测试用户");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setStatus(1);
        return user;
    }

    private void mockCurrentUser() {
        UserPrincipal principal = new UserPrincipal();
        principal.setUserId(CURRENT_USER_ID);
        principal.setUsername("testuser");
        principal.setRoles(List.of("USER"));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
