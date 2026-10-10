package foodcards.adapter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("foodcards.adapter.mapper")
@EnableAsync
public class AppAdapterApplication {
    public static void main(String[] args) {

        SpringApplication.run(AppAdapterApplication.class, args);
    }
}