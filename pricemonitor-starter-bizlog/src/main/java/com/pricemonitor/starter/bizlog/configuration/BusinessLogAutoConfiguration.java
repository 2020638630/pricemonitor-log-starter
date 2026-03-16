package com.pricemonitor.starter.bizlog.configuration;

import com.pricemonitor.starter.bizlog.aspect.BusinessLogAspect;
import com.pricemonitor.starter.bizlog.listener.BusinessLogAsyncListener;
import com.pricemonitor.starter.bizlog.listener.BusinessLogSyncListener;
import com.pricemonitor.starter.bizlog.processor.BusinessLogProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 业务日志自动配置类
 * 精确配置业务日志相关组件，避免全包扫描
 *
 * @author pricemonitor
 */
@Configuration
public class BusinessLogAutoConfiguration {

    /**
     * 业务日志切面
     */
    @Bean
    public BusinessLogAspect businessLogAspect(ApplicationContext applicationContext) {
        return new BusinessLogAspect(applicationContext);
    }

    /**
     * 业务日志异步监听器
     */
    @Bean
    public BusinessLogAsyncListener businessLogAsyncListener(List<BusinessLogProcessor> processors) {
        return new BusinessLogAsyncListener(processors);
    }

    /**
     * 业务日志同步监听器
     */
    @Bean
    public BusinessLogSyncListener businessLogSyncListener(List<BusinessLogProcessor> processors) {
        return new BusinessLogSyncListener(processors);
    }

    /**
     * 默认业务日志处理器
     * 如果没有自定义实现，则使用默认处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public BusinessLogProcessor defaultBusinessLogProcessor() {
        return new com.pricemonitor.starter.bizlog.listener.DefaultBusinessLogProcessor();
    }
}
