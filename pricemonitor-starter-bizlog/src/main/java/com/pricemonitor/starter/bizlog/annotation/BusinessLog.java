package com.pricemonitor.starter.bizlog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务日志注解
 * 用于标记需要记录业务日志的方法
 *
 * <p><strong>使用步骤：</strong></p>
 * <ol>
 *   <li>在需要记录日志的方法上添加此注解</li>
 *   <li>实现 {@link com.pricemonitor.starter.bizlog.processor.BusinessLogProcessor} 接口来处理日志
 *   </li>
 * </ol>
 *
 * <p><strong>示例：</strong></p>
 * <pre>
 * // 1. 添加注解
 * &#64;BusinessLog(code = "USER_CREATE", description = "创建用户")
 * public void createUser(UserDTO user) {
 *     // 业务逻辑
 * }
 *
 * // 2. 实现处理器（可选，不实现则使用默认处理器输出到日志文件）
 * &#64;Component
 * public class MyBusinessLogProcessor implements BusinessLogProcessor {
 *     &#64;Override
 *     public void process(BusinessLogDTO logDTO) {
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
 * @see com.pricemonitor.starter.bizlog.processor.BusinessLogProcessor
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessLog {

    /**
     * 业务编码/类型
     * 用于标识不同类型的业务操作，如 "USER_CREATE", "ORDER_UPDATE" 等
     *
     * @return 业务编码
     */
    String code();

    /**
     * 是否打印完整参数
     * true - 打印完整参数（包含所有字段）
     * false - 简化参数（仅显示关键信息）
     *
     * @return 是否打印完整参数
     */
    boolean fullArgs() default false;

    /**
     * 是否同步处理
     * true - 同步处理（阻塞等待日志处理完成）
     * false - 异步处理（不阻塞业务流程）
     *
     * @return 是否同步处理
     */
    boolean sync() default false;

    /**
     * 业务描述（可选）
     * 用于日志的可读性描述
     *
     * @return 业务描述
     */
    String description() default "";
}
