package com.iflytek.risk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@ComponentScan({
        "com.iflytek.risk.sec",
        "com.iflytek.risk.common",
        "com.iflytek.risk.controller",
        "com.iflytek.risk.interfaceController",
        "com.iflytek.risk.service",
        "com.iflytek.risk.schedule"
})
@MapperScan("com.iflytek.risk.mapper")
public class RiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskApplication.class, args);
    }
}
