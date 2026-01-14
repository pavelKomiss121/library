package ru.mentee.timing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TimingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TimingAutoConfiguration.class));

    @Test
    @DisplayName("Should создать бин TimingService по умолчанию")
    void shouldCreateServiceByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TimingService.class);
        });
    }

    @Test
    @DisplayName("Should НЕ создавать бин, если timing.service.enabled=false")
    void shouldNotCreateServiceWhenDisabled() {
        contextRunner
                .withPropertyValues("timing.service.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TimingService.class);
                });
    }
}

