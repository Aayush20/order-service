//package org.example.orderservice.configs;
//
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.containers.MySQLContainer;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.utility.DockerImageName;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//public abstract class IntegrationTestConfig {
//
//    static final MySQLContainer<?> mysql =
//            new MySQLContainer<>("mysql:8.0")
//                    .withDatabaseName("orders")
//                    .withUsername("test")
//                    .withPassword("test");
//
//    static final GenericContainer<?> redis =
//            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
//                    .withExposedPorts(6379);
//
//    static final KafkaContainer kafka =
//            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));
//
//    @BeforeAll
//    static void startContainers() {
//        mysql.start();
//        redis.start();
//        kafka.start();
//    }
//
//    @DynamicPropertySource
//    static void overrideProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", mysql::getJdbcUrl);
//        registry.add("spring.datasource.username", mysql::getUsername);
//        registry.add("spring.datasource.password", mysql::getPassword);
//
//        registry.add("spring.data.redis.host", redis::getHost);
//        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
//
//        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//    }
//}
package org.example.orderservice.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestConfig {

    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.0.34");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.2.4-alpine");
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.5.0");

    public static final MySQLContainer<?> mysql = new MySQLContainer<>(MYSQL_IMAGE)
            .withDatabaseName("orders")
            .withUsername("test")
            .withPassword("test");

    public static final GenericContainer<?> redis = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    public static final KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

    @BeforeAll
    static void startContainers() {
        if (!mysql.isRunning()) mysql.start();
        if (!redis.isRunning()) redis.start();
        if (!kafka.isRunning()) kafka.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // ✅ MySQL
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // ✅ Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // ✅ Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // ✅ Optional overrides
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.sql.init.mode", () -> "never");
    }
}
