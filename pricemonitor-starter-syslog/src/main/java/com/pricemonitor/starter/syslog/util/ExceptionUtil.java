package com.pricemonitor.starter.syslog.util;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * 异常工具类
 *
 * @author pricemonitor
 */
@UtilityClass
public class ExceptionUtil {

    /**
     * 获取异常堆栈信息
     *
     * @param throwable 异常对象
     * @return 堆栈信息字符串
     */
    public String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return StrUtil.EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取异常的简短描述
     *
     * @param throwable 异常对象
     * @return 简短描述
     */
    public String getSimpleMessage(Throwable throwable) {
        if (throwable == null) {
            return StrUtil.EMPTY;
        }
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }

    /**
     * 获取异常消息（不包含堆栈）
     *
     * @param throwable 异常对象
     * @return 异常消息
     */
    public String getMessage(Throwable throwable) {
        if (throwable == null) {
            return StrUtil.EMPTY;
        }
        return throwable.getMessage();
    }
}
