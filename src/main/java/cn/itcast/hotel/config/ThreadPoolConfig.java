package cn.itcast.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {


    @Bean(name = "asyncTaskExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // 设置核心线程池大小
        executor.setMaxPoolSize(40); // 设置最大线程池大小
        executor.setQueueCapacity(200); // 设置队列容量
        executor.setThreadNamePrefix("CustomThreadPool-"); // 设置线程名称前缀
        executor.initialize();
        return executor;
    }
}