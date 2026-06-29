package com.mimi.photowall.util;

/**
 * User-Agent解析工具类
 * 用于解析浏览器类型、操作系统、设备名称
 */
public class UserAgentUtil {

    /**
     * 解析设备名称
     * 从User-Agent中提取浏览器和操作系统信息
     *
     * @param userAgent User-Agent字符串
     * @return 设备名称，如"Chrome on Windows"
     */
    public static String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }

        String browser = parseBrowser(userAgent);
        String os = parseOS(userAgent);

        return browser + " on " + os;
    }

    /**
     * 解析浏览器类型
     *
     * @param userAgent User-Agent字符串
     * @return 浏览器名称
     */
    private static String parseBrowser(String userAgent) {
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari";
        } else if (userAgent.contains("Edg")) {
            return "Edge";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            return "Opera";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown Browser";
        }
    }

    /**
     * 解析操作系统
     *
     * @param userAgent User-Agent字符串
     * @return 操作系统名称
     */
    private static String parseOS(String userAgent) {
        if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac OS")) {
            return "macOS";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        } else {
            return "Unknown OS";
        }
    }
}
