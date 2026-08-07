package io.github.blueprintplatform.samples.transportcoverage.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.blueprintplatform.samples.transportcoverage")
public class TransportCoverageConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransportCoverageConsumerApplication.class, args);
    }
}
