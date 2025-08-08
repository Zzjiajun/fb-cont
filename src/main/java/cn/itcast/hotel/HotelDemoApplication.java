package cn.itcast.hotel;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import cn.itcast.hotel.web.FbVpnStockController;
import cn.itcast.hotel.web.HotelController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan("cn.itcast")
@MapperScan("cn.itcast.**.dao")
@SpringBootApplication
@EnableAsync
public class HotelDemoApplication {

    
    @Autowired
    private HotelController hotelController;

    public static void main(String[] args) {
        SpringApplication.run(HotelDemoApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用启动完成，开始智能预热Redis数据...");
        try {
            // 智能预热：调用现有的数据更新接口来预热Redis
            // 这样可以确保数据是最新的，而不是使用可能过期的数据
            hotelController.dmCenterRedis();
            hotelController.dmConditionRedis();
            log.info("Redis数据智能预热完成 - 使用最新数据");
        } catch (Exception e) {
            log.error("Redis数据智能预热失败", e);
        }
    }
}
