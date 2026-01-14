package ru.mentee.timing;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(TimingProperties.class)
@ConditionalOnProperty(name = "timing.service.enabled", havingValue = "true", matchIfMissing = true)
public class TimingAutoConfiguration {

    @Bean
    public TimingService timingService() {
        return new TimingService();
    }
}

