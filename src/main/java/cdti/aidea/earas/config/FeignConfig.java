package cdti.aidea.earas.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC; // Change to FULL for detailed debugging
  }

  @Bean
  public Request.Options requestOptions() {
    return new Request.Options(5000, 10000); // 5s connect, 10s read timeout
  }

  @Bean
  public Retryer feignRetryer() {
    return new Retryer.Default(1000, 2000, 3); // Retry 3 times with backoff
  }
}
