package com.pricemonitor.starter.syslog.aspect;

import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricemonitor.starter.syslog.util.ExceptionUtil;
import com.pricemonitor.starter.syslog.util.IpAddressUtil;
import com.pricemonitor.starter.syslog.annotation.SystemLog;
import com.pricemonitor.starter.syslog.dto.SystemLogDTO;
import com.pricemonitor.starter.syslog.event.SystemLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统日志切面
 * 拦截带有@SystemLog注解的方法，记录系统操作日志
 *
 * @author pricemonitor
 */
@Slf4j
@Aspect
@Order(5)
public class SystemLogAspect {

    private final ApplicationContext applicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SystemLogAspect(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 切点：拦截所有带有@SystemLog注解的方法
     */
    @Around("@annotation(com.pricemonitor.starter.syslog.annotation.SystemLog)")
    public Object recordSystemLog(ProceedingJoinPoint point) throws Throwable {
        Method targetMethod = resolveMethod(point);
        Object[] args = point.getArgs();
        long startTime = System.nanoTime();

        try {
            // 执行目标方法
            Object result = point.proceed();

            // 方法执行成功后发布日志事件
            publishEvent(args, targetMethod, startTime, result, "INFO", null);

            return result;
        } catch (Throwable e) {
            // 方法执行失败时发布日志事件
            publishEvent(args, targetMethod, startTime, null, "ERROR", e);
            throw e;
        }
    }

    /**
     * 发布日志事件
     *
     * @param args        方法参数
     * @param targetMethod 目标方法
     * @param startTime   开始时间
     * @param result      执行结果
     * @param logType     日志类型
     * @param throwable           异常对象
     */
    private void publishEvent(Object[] args, Method targetMethod, long startTime,
                              Object result, String logType, Throwable throwable) {
        try {
            SystemLog systemLogAnn = targetMethod.getAnnotation(SystemLog.class);

            // 构建日志DTO
            SystemLogDTO logDTO = new SystemLogDTO();

            // 获取服务ID
            String serviceId = getServiceId();

            // 获取跟踪ID
            String traceId = MDC.get("traceId");

            logDTO.setServiceId(serviceId)
                    .setTraceId(traceId)
                    .setType(logType)
                    .setModule(systemLogAnn.module())
                    .setTitle(systemLogAnn.title())
                    .setDescription(systemLogAnn.description())
                    .setParams(getParams(systemLogAnn.containsFile(), systemLogAnn.ignoreParams(), args))
                    .setExecuteTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime))
                    .setCreateTime(LocalDateTime.now());

            // 获取HTTP请求信息
            HttpServletRequest request = getRequest();
            if (request != null) {
                logDTO.setMethod(request.getMethod())
                        .setUrl(request.getRequestURI())
                        .setIp(IpAddressUtil.getIpAddress(request));
            }

            // 设置执行结果
            if (result != null) {
                try {
                    logDTO.setResult(objectMapper.writeValueAsString(result));
                } catch (Exception ex) {
                    logDTO.setResult(String.valueOf(result));
                }
            }

            // 设置异常信息
            if (throwable != null) {
                logDTO.setException(ExceptionUtil.getStackTrace(throwable));
            }

            // 获取用户信息（使用反射访问bssc-cloud-common的TokenInfoContext）
            try {
                Class<?> tokenInfoContextClass = Class.forName("com.bssc.cloud.common.context.TokenInfoContext");
                Method getTokenInfoMethod = tokenInfoContextClass.getMethod("getTokenInfo");
                Object tokenInfo = getTokenInfoMethod.invoke(null);

                if (tokenInfo != null) {
                    Class<?> tokenInfoClass = tokenInfo.getClass();
                    Method getUserIdMethod = tokenInfoClass.getMethod("getUserId");
                    Method getUserNameMethod = tokenInfoClass.getMethod("getUserName");
                    Method getTenantIdMethod = tokenInfoClass.getMethod("getTenantId");

                    String userId = (String) getUserIdMethod.invoke(tokenInfo);
                    String userName = (String) getUserNameMethod.invoke(tokenInfo);
                    String tenantId = (String) getTenantIdMethod.invoke(tokenInfo);

                    logDTO.setUserId(userId)
                            .setOperator(userName)
                            .setTenantId(tenantId);

                    log.debug("获取到用户信息: userId={}, userName={}, tenantId={}", userId, userName, tenantId);
                } else {
                    logDTO.setOperator("anonymous");
                }
            } catch (ClassNotFoundException classNotFoundException) {
                log.warn("未找到TokenInfoContext类，使用匿名用户");
                logDTO.setOperator("anonymous");
            } catch (Exception exception) {
                log.warn("获取用户信息失败", exception);
                logDTO.setOperator("anonymous");
            }

            // 发布日志事件
            applicationContext.publishEvent(new SystemLogEvent(logDTO));

            log.info("系统日志事件已发布: title={}, type={}", systemLogAnn.title(), logType);
        } catch (Exception ex) {
            log.error("发布系统日志事件失败", ex);
        }
    }

    /**
     * 获取请求参数
     *
     * @param containsFile 是否包含文件
     * @param ignoreParams 是否忽略参数
     * @param args         方法参数
     * @return 参数JSON字符串
     */
    private String getParams(boolean containsFile, boolean ignoreParams, Object[] args) {
        if (ignoreParams || containsFile || ArrayUtil.isEmpty(args)) {
            return "";
        }

        try {
            // 优先从request获取参数
            HttpServletRequest request = getRequest();
            if (request != null) {
                Map<String, String> paramMap = new HashMap<>();
                Enumeration<String> paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    String paramValue = request.getParameter(paramName);
                    paramMap.put(paramName, paramValue);
                }
                if (!paramMap.isEmpty()) {
                    return objectMapper.writeValueAsString(paramMap);
                }
            }

            // 如果request没有参数，取第一个参数
            return objectMapper.writeValueAsString(args[0]);
        } catch (Exception e) {
            log.warn("序列化参数失败", e);
            return args.length > 0 ? args[0].toString() : "";
        }
    }

    /**
     * 获取HTTP请求
     *
     * @return HttpServletRequest对象
     */
    private HttpServletRequest getRequest() {
        try {
            return applicationContext.getBean(HttpServletRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取服务ID
     *
     * @return 服务ID
     */
    private String getServiceId() {
        try {
            return applicationContext.getEnvironment()
                    .getProperty("spring.application.name", "unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 解析目标方法
     *
     * @param point 切点
     * @return 目标方法
     */
    private Method resolveMethod(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();

        Method method = getDeclaredMethod(targetClass, signature.getName(),
                signature.getMethod().getParameterTypes());
        if (method == null) {
            throw new IllegalStateException("无法解析目标方法: " + signature.getMethod().getName());
        }
        return method;
    }

    /**
     * 获取声明的Method对象
     *
     * @param clazz          目标类
     * @param name           方法名
     * @param parameterTypes 参数类型
     * @return Method对象
     */
    private Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethod(superClass, name, parameterTypes);
            }
        }
        return null;
    }

}
