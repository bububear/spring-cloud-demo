package consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author az6343
 */
@EnableEurekaClient
@SpringBootApplication
public class RibbonConsumerClient {

    private final Logger logger = LoggerFactory.getLogger(RibbonConsumerClient.class);

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        logger.info(String.valueOf(restTemplate.hashCode()));
        return restTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(RibbonConsumerClient.class, args);
    }
}
