package consumer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author az6343
 */
@RestController
public class HelloController {

    private final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/ribbon-hello", method = RequestMethod.GET)
    public String hello() {
        logger.info(String.valueOf(restTemplate.hashCode()));
        return restTemplate.getForEntity("http://PRODUCE-SERVICE/hello", String.class).getBody();
    }
}
