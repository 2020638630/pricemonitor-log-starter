package com.pricemonitor.starter.syslog.util;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * IP地址工具类
 * 提供IP地址解析、验证、判断等功能
 *
 * @author pricemonitor
 */
@UtilityClass
public class IpAddressUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String IP_SEPARATOR = ",";

    // IP地址正则表达式
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    /**
     * 代理IP头名称数组，按优先级排序
     */
    private static final String[] PROXY_IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_REAL_IP"
    };

    /**
     * 获取客户端IP地址
     * 按照代理IP头名称的优先级依次获取，如果都获取不到则使用 RemoteAddr
     *
     * @param request HTTP请求
     * @return IP地址，如果请求为null或无法获取则返回"unknown"
     */
    public String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip;
        for (String header : PROXY_IP_HEADERS) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // 处理多个IP的情况，取第一个IP
                if (ip.contains(IP_SEPARATOR)) {
                    ip = ip.split(IP_SEPARATOR)[0].trim();
                }
                return ip;
            }
        }

        // 如果所有代理头都没有获取到IP，则使用 RemoteAddr
        return request.getRemoteAddr();
    }

    /**
     * 判断IP地址是否有效
     *
     * @param ip IP地址
     * @return true-有效，false-无效
     */
    public boolean isValidIp(String ip) {
        if (StrUtil.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return true-内网IP，false-外网IP
     */
    public boolean isInternalIp(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        // 本地回环地址
        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return true;
        }

        // A类私有地址: 10.0.0.0 - 10.255.255.255
        if (ip.startsWith("10.")) {
            return true;
        }

        // B类私有地址: 172.16.0.0 - 172.31.255.255
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            }
        }

        // C类私有地址: 192.168.0.0 - 192.168.255.255
        if (ip.startsWith("192.168.")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为本地IP
     *
     * @param ip IP地址
     * @return true-本地IP，false-非本地IP
     */
    public boolean isLocalIp(String ip) {
        return LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip) || "localhost".equalsIgnoreCase(ip);
    }

    /**
     * 隐藏IP地址的部分信息
     * 例如: 192.168.1.100 -> 192.168.*.*
     *
     * @param ip IP地址
     * @return 隐藏后的IP地址
     */
    public String maskIp(String ip) {
        if (!isValidIp(ip)) {
            return ip;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }

        // 保留第一段，隐藏其他段
        return parts[0] + ".*.*.*";
    }

    /**
     * 获取IP地址的类型
     *
     * @param ip IP地址
     * @return IP类型: LOCAL/INTERNAL/EXTERNAL/UNKNOWN
     */
    public String getIpType(String ip) {
        if (!isValidIp(ip)) {
            return "UNKNOWN";
        }
        if (isLocalIp(ip)) {
            return "LOCAL";
        }
        if (isInternalIp(ip)) {
            return "INTERNAL";
        }
        return "EXTERNAL";
    }
}
