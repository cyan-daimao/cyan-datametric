package com.cyan.datametric.infra.config;

import com.cyan.arch.common.mapstruct.MapstructConvert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MapStruct 转换工具配置
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Configuration
public class MapstructConvertConfig {

    @Bean
    public MapstructConvert mapstructConvert() {
        return new MapstructConvert();
    }
}
