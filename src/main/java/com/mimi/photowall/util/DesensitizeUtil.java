package com.mimi.photowall.util;

/**
 * 脱敏工具类
 * 用于手机号、邮箱等敏感信息的脱敏处理
 */
public class DesensitizeUtil {

    /**
     * 手机号脱敏
     * 13800138000 -> 138****8000
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏
     * test@example.com -> t***@example.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String desensitizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex < 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /**
     * 用户名脱敏
     * testuser -> te**er
     *
     * @param username 用户名
     * @return 脱敏后的用户名
     */
    public static String desensitizeUsername(String username) {
        if (username == null || username.length() < 3) {
            return username;
        }
        int length = username.length();
        if (length <= 4) {
            return username.charAt(0) + "***";
        }
        return username.substring(0, 2) + "***" + username.substring(length - 2);
    }
}
