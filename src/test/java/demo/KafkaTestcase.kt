package demo

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@ContextConfiguration(initializers = [KafkaTestcase.Initializer::class])
@DirtiesContext
@TestPropertySource(properties = ["kafka.demotopic.topicname=myTopic"])
open class KafkaTestcase {

    companion object {
        @Container
        @JvmStatic
        internal val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.0"))
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(context: ConfigurableApplicationContext) {
            val kafkaUrl =
                "spring.kafka.bootstrap-servers=${kafkaContainer.bootstrapServers}"

            TestPropertyValues.of(
                kafkaUrl
            ).applyTo(context.environment)
        }
    }
}