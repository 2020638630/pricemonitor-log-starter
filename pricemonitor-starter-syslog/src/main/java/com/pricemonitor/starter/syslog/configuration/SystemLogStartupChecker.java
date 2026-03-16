package com.pricemonitor.starter.syslog.configuration;

import com.pricemonitor.starter.syslog.listener.DefaultSystemLogProcessor;
import com.pricemonitor.starter.syslog.processor.SystemLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统日志启动检查器
 * 
 * <p>在应用启动时检查系统日志处理器的配置情况，并给出友好的提示。</p>
 * 
 * @author pricemonitor
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "pricemonitor.syslog",
    name = "startup-check",
    havingValue = "true",
    matchIfMissing = true
)
public class SystemLogStartupChecker {

    private final ApplicationContext applicationContext;

    public SystemLogStartupChecker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 在 Spring 容器刷新后执行检查
     *
     * @param event 容器刷新事件
     */
    @EventListener(ContextRefreshedEvent.class)
    public void checkSystemLogProcessors(ContextRefreshedEvent event) {
        // 防止在子容器中重复检查
        if (event.getApplicationContext().getParent() != null) {
            return;
        }

        try {
            List<SystemLogProcessor> processors = applicationContext.getBeansOfType(SystemLogProcessor.class)
                    .values()
                    .stream()
                    .filter(processor -> !(processor instanceof DefaultSystemLogProcessor))
                    .collect(Collectors.toList());

            if (processors.isEmpty()) {
                log.warn("═══════════════════════════════════════════════════════════════");
                log.warn("系统日志配置提示");
                log.warn("═══════════════════════════════════════════════════════════════");
                log.warn("未检测到自定义的 SystemLogProcessor 实现");
                log.warn("系统日志将使用默认处理器输出到日志文件");
                log.warn("");
                log.warn("建议：在生产环境中，请实现自定义的 SystemLogProcessor");
                log.warn("       以将系统日志持久化到数据库或其他存储系统");
                log.warn("");
                log.warn("示例实现：");
                log.warn("  @Component");
                log.warn("  public class MySystemLogProcessor implements SystemLogProcessor {");
                log.warn("      @Override");
                log.warn("      public void process(SystemLogDTO logDTO) {");
                log.warn("          // 保存到数据库");
                log.warn("      }");
                log.warn("  }");
                log.warn("═══════════════════════════════════════════════════════════════");
            } else {
                log.info("系统日志处理器配置正常，共找到 {} 个自定义处理器", processors.size());
            }
        } catch (Exception e) {
            log.info("检查系统日志处理器时出错", e);
        }
    }
}
