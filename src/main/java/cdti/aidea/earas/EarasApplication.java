package cdti.aidea.earas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class EarasApplication {
  public static void main(String[] args) {
    SpringApplication.run(EarasApplication.class, args);
  }
}
