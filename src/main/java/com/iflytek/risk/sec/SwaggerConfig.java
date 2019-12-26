package com.iflytek.risk.sec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @program: lzda->SwaggerConfig
 * @description: Swagger API配置
 * @author: 黄智强
 * @create: 2019-10-25 15:22
 **/
@SuppressWarnings({"unused"})
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 自行修改为自己的包路径
                .apis(RequestHandlerSelectors.basePackage("com.iflytek.risk.interfaceController"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("法务风险管理")
                .description("法务2接口")
                .contact("黄智强，tel:1585517401, email:490479710@qq.com")
                .termsOfServiceUrl("http://localhost:9071/lawrisk/swagger-ui.html")
                .version("1.0")
                .build();
    }

}
