package com.pricemonitor.starter.syslog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 系统日志注解
 * 用于标记需要记录系统操作日志的方法
 *
 * <p><strong>使用步骤：</strong></p>
 * <ol>
 *   <li>在需要记录日志的方法上添加此注解</li>
 *   <li>实现 {@link com.pricemonitor.starter.syslog.processor.SystemLogProcessor} 接口来处理日志
 *   </li>
 * </ol>
 *
 * <p><strong>示例：</strong></p>
 * <pre>
 * // 1. 添加注解
 * &#64;SystemLog(title = "用户登录", description = "用户使用账号密码登录系统")
 * public void login(LoginRequest request) {
 *     // 业务逻辑
 * }
 *
 * // 2. 实现处理器（可选，不实现则使用默认处理器输出到日志文件）
 * &#64;Component
 * public class MySystemLogProcessor implements SystemLogProcessor {
 *     &#64;Override
 *     public void process(SystemLogDTO logDTO) {
 *         // 将日志保存到数据库
 *         logMapper.insert(logDTO);
 *     }
 * }
 * </pre>
 *
 * <p><strong>注意事项：</strong></p>
 * <ul>
 *   <li>如果未实现自定义处理器，日志将输出到日志文件（通过默认处理器）</li>
 *   <li>建议生产环境实现自定义处理器以持久化日志</li>
 * </ul>
 *
 * @author pricemonitor
 * @see com.pricemonitor.starter.syslog.processor.SystemLogProcessor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemLog {

    /**
     * 日志标题
     * 简短的业务操作标题，如 "用户登录"、"数据导出"
     *
     * @return 日志标题
     */
    String title() default "";

    /**
     * 操作描述
     * 详细的操作说明，如 "用户使用账号密码登录系统"
     *
     * @return 操作描述
     */
    String description() default "";

    /**
     * 是否包含文件参数
     * true - 包含文件参数（文件内容将不被记录）
     * false - 不包含文件参数
     *
     * @return 是否包含文件参数
     */
    boolean containsFile() default false;

    /**
     * 是否忽略参数记录
     * true - 忽略参数记录（仅记录操作信息）
     * false - 记录参数信息
     *
     * @return 是否忽略参数记录
     */
    boolean ignoreParams() default false;

    /**
     * 业务模块
     * 用于区分不同业务模块的日志
     *
     * @return 业务模块
     */
    String module() default "";
}
