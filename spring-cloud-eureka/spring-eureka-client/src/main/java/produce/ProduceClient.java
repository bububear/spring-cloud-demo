package produce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author az6343
 * @EnableDiscoveryClient(激活Eureka中的DiscoveryClient实现，
 * 自动化配置，创建DiscoverClient接口针对Eureka客户端的EurekaDiscoveryClient实例)
 *
 * @EnableEurekaClient 当注册中心是Netflix.Eureka，可以采用此注解，通过包名可以看出
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ProduceClient {

    public static void main(String[] args) {
        SpringApplication.run(ProduceClient.class, args);
    }
}
