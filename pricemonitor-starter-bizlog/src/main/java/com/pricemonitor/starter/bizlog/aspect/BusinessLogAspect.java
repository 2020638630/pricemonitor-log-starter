package com.pricemonitor.starter.bizlog.aspect;

import cn.hutool.core.util.ArrayUtil;
import com.pricemonitor.starter.bizlog.annotation.BusinessLog;
import com.pricemonitor.starter.bizlog.dto.BusinessLogDTO;
import com.pricemonitor.starter.bizlog.event.BusinessLogEvent;
import com.pricemonitor.starter.bizlog.event.BusinessLogSyncEvent;
import com.pricemonitor.starter.bizlog.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务日志切面
 * 拦截带有@BusinessLog注解的方法，记录业务日志
 *
 * @author pricemonitor
 */
@Slf4j
@Aspect
@Order(10)
public class BusinessLogAspect {

    private final ApplicationContext applicationContext;

    public BusinessLogAspect(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 切点：拦截所有带有@BusinessLog注解的方法
     */
    @Around("@annotation(com.pricemonitor.starter.bizlog.annotation.BusinessLog)")
    public Object recordBusinessLog(ProceedingJoinPoint point) throws Throwable {
        log.info("开始处理业务日志切面...");
        Method targetMethod = resolveMethod(point);
        Object[] args = point.getArgs();

        try {
            // 执行目标方法
            Object result = point.proceed();

            // 方法执行成功后发布日志事件
            publishEvent(args, targetMethod, result, null);

            return result;
        } catch (Throwable e) {
            // 方法执行失败时发布日志事件
            publishEvent(args, targetMethod, null, e);
            throw e;
        }
    }

    /**
     * 发布日志事件
     *
     * @param args         方法参数
     * @param targetMethod 目标方法
     * @param result       方法执行结果
     * @param throwable    异常对象
     */
    private void publishEvent(Object[] args, Method targetMethod, Object result, Throwable throwable) {
        try {
            BusinessLog bizLogAnn = targetMethod.getAnnotation(BusinessLog.class);

            // 构建日志DTO
            BusinessLogDTO logDTO = new BusinessLogDTO();
            logDTO.setCode(bizLogAnn.code())
                    .setDescription(bizLogAnn.description())
                    .setParams(getArgs(bizLogAnn.fullArgs(), args, targetMethod))
                    .setResult(result)
                    .setCreateTime(LocalDateTime.now());

            // 从 TokenInfo 获取用户信息（使用反射访问bssc-cloud-common的TokenInfoContext）
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

                    logDTO.setTenantId(tenantId)
                            .setUserId(userId)
                            .setUserName(userName)
                            .setUserType("用户");

                    log.debug("获取到用户信息: userId={}, userName={}, tenantId={}", userId, userName, tenantId);
                } else {
                    logDTO.setTenantName("系统")
                            .setUserName("系统")
                            .setUserType("系统");
                }
            } catch (ClassNotFoundException classNotFoundException) {
                log.warn("未找到TokenInfoContext类，使用系统默认用户");
                logDTO.setTenantName("系统")
                        .setUserName("系统")
                        .setUserType("系统");
            } catch (Exception exception) {
                log.warn("获取用户信息失败", exception);
                logDTO.setTenantName("系统")
                        .setUserName("系统")
                        .setUserType("系统");
            }

            // 根据注解配置决定同步或异步处理
            if (bizLogAnn.sync()) {
                applicationContext.publishEvent(new BusinessLogSyncEvent(logDTO));
            } else {
                applicationContext.publishEvent(new BusinessLogEvent(logDTO));
            }

            log.info("业务日志事件已发布: code={}, sync={}", bizLogAnn.code(), bizLogAnn.sync());
        } catch (Exception ex) {
            log.error("发布业务日志事件失败", ex);
        }
    }

    /**
     * 获取方法参数
     *
     * @param fullArgs   是否获取完整参数
     * @param args       方法参数数组
     * @param targetMethod 目标方法
     * @return 参数JSON字符串
     */
    private String getArgs(boolean fullArgs, Object[] args, Method targetMethod) {
        // 如果参数为空，返回空字符串
        if (ArrayUtil.isEmpty(args)) {
            return "";
        }

        // 尝试从请求中获取参数
        HttpServletRequest request = extractHttpServletRequest(args);
        if (request != null && !fullArgs) {
            Map<String, String> paramsMap = buildQueryParamsMap(request);
            if (!paramsMap.isEmpty()) {
                try {
                    return JsonUtil.toJson(paramsMap);
                } catch (Exception e) {
                    log.warn("请求参数转JSON失败", e);
                }
            }
        }

        // 将参数转换为JSON
        return convertArgsToJson(args, targetMethod);
    }

    /**
     * 从参数数组中提取HttpServletRequest对象
     *
     * @param args 参数数组
     * @return HttpServletRequest对象，如果不存在则返回null
     */
    private HttpServletRequest extractHttpServletRequest(Object[] args) {
        if (ArrayUtil.isEmpty(args)) {
            return null;
        }

        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        return null;
    }

    /**
     * 构建请求参数Map
     *
     * @param request HTTP请求对象
     * @return 参数Map
     */
    private Map<String, String> buildQueryParamsMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        return params;
    }

    /**
     * 将参数转换为JSON字符串
     *
     * @param args  参数数组
     * @param method 目标方法
     * @return JSON字符串
     */
    private String convertArgsToJson(Object[] args, Method method) {
        try {
            // 如果有多个参数，转换为Map
            if (args.length > 1) {
                Parameter[] parameters = method.getParameters();
                Map<String, Object> params = new HashMap<>();
                for (int i = 0; i < args.length; i++) {
                    params.put(parameters[i].getName(), args[i]);
                }
                return JsonUtil.toJson(params);
            }

            // 单个参数直接转换
            return JsonUtil.toJson(args[0]);
        } catch (Exception e) {
            log.error("参数转JSON失败", e);
            // 如果JSON转换失败，返回toString结果
            if (args.length == 1) {
                return args[0].toString();
            }
            return args.toString();
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
        Method method = getDeclaredMethod(targetClass, signature.getName(), signature.getMethod().getParameterTypes());

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
            return superClass != null ? getDeclaredMethod(superClass, name, parameterTypes) : null;
        }
    }
}
