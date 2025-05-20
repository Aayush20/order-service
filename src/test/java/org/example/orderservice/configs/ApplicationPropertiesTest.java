package org.example.orderservice.configs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "order.kafka-topic-placed=order.placed",
                    "order.kafka-topic-cancelled=order.cancelled"
            );

    @Test
    void shouldLoadPropertiesSuccessfully() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ApplicationProperties.class);
            ApplicationProperties props = context.getBean(ApplicationProperties.class);
            assertThat(props.getKafkaTopicPlaced()).isEqualTo("order.placed");
            assertThat(props.getKafkaTopicCancelled()).isEqualTo("order.cancelled");
        });
    }

    private static class TestConfig {
        @org.springframework.context.annotation.Bean
        public ApplicationProperties applicationProperties() {
            return new ApplicationProperties();
        }
    }
}
