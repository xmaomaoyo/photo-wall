package com.mimi.photowall.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 密码工具类
 * 用于密码加密、验证、随机密码生成
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * BCrypt加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成随机密码
     * 包含大写字母、小写字母、数字、特殊符号
     *
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8;
        }

        List<Character> passwordChars = new ArrayList<>();

        // 确保包含至少一个大写字母
        passwordChars.add(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));

        // 确保包含至少一个小写字母
        passwordChars.add(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));

        // 确保包含至少一个数字
        passwordChars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));

        // 确保包含至少一个特殊符号
        passwordChars.add(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));

        // 填充剩余字符
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;
        for (int i = 4; i < length; i++) {
            passwordChars.add(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }

        // 打乱顺序
        Collections.shuffle(passwordChars, RANDOM);

        // 转换为字符串
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    /**
     * 验证密码格式
     * 至少8位，包含大写字母、小写字母、特殊符号
     *
     * @param password 密码
     * @return 是否符合格式
     */
    public static boolean isValidFormat(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasSymbol = false;

        for (char c : password.toCharArray()) {
            if (UPPERCASE.indexOf(c) >= 0) {
                hasUppercase = true;
            } else if (LOWERCASE.indexOf(c) >= 0) {
                hasLowercase = true;
            } else if (SYMBOLS.indexOf(c) >= 0) {
                hasSymbol = true;
            }
        }

        return hasUppercase && hasLowercase && hasSymbol;
    }
}
