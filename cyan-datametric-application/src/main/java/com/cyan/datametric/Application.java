package com.cyan.datametric;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据资产指标平台应用入口
 *
 * @author cy.Y
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.cyan")
@EnableDiscoveryClient
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
