package org.example.orderservice.jobs;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.orderservice.kafka.KafkaPublisher;
import org.example.orderservice.models.Order;
import org.example.orderservice.repositories.OrderRepository;
import org.example.orderservice.repositories.RetryDeadLetterLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

class OrderRetryConsumerTest {

    @Mock private KafkaPublisher publisher;
    @Mock private OrderRepository orderRepository;
    @Mock private RetryDeadLetterLogRepository deadLetterRepo;

    @InjectMocks private OrderRetryConsumer retryConsumer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldPublishOrderIfRecordContainsOrder() {
        Order mockOrder = new Order();
        mockOrder.setId(101L);

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("retry.topic", 0, 0L, "101", mockOrder);

        retryConsumer.retryFailedEvent(record);

        verify(publisher).publishOrderPlaced(mockOrder);
        verifyNoInteractions(deadLetterRepo);
    }

    @Test
    void shouldSkipIfUnknownTypeProvided() {
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("retry.topic", 0, 0L, "key", new Object());

        retryConsumer.retryFailedEvent(record);

        verifyNoInteractions(publisher);
        verifyNoInteractions(deadLetterRepo);
    }

    @Test
    void shouldLogToDeadLetterIfRetryFails() {
        Order badOrder = new Order();
        badOrder.setId(999L);

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("retry.topic", 0, 0L, "999", badOrder);

        doThrow(new RuntimeException("Kafka failure")).when(publisher).publishOrderPlaced(badOrder);

        retryConsumer.retryFailedEvent(record);

        verify(deadLetterRepo).save(argThat(log ->
                log.getKey().equals("999") &&
                        log.getPayload().contains("999") &&
                        log.getErrorMessage().contains("Kafka failure")
        ));
    }
}
