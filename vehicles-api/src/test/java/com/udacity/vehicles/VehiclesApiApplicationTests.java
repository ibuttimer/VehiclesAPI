package com.udacity.vehicles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "job.autorun.enabled=false" // don't run preload of addresses into database
})
public class VehiclesApiApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    public void contextLoads() {
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.info(
                String.format("Active profiles: %s", Arrays.toString(environment.getActiveProfiles()))
        );
        System.getenv().forEach((key, value) -> System.out.println(key+" - "+value));
    }

}
