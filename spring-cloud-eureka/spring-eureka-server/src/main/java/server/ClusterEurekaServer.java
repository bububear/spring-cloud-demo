package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author az6343
 */
@EnableEurekaServer
@SpringBootApplication
public class ClusterEurekaServer {

    public static void main(String[] args) {
        SpringApplication.run(ClusterEurekaServer.class, args);
    }
}
