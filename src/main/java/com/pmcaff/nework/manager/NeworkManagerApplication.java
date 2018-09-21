package com.pmcaff.nework.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.pmcaff.nework.*")
@MapperScan("com.pmcaff.nework.manager.mapper")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class NeworkManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeworkManagerApplication.class, args);
    }
}
