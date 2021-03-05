package demo.consumer

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.BatchErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import javax.annotation.PreDestroy

@Configuration
@EnableKafka
class KafkaConsumerConfig(

    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapAddress: String,

    @Value("\${kafka.group-id:myGroupId}")
    private val groupId: String,

    private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry

) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = KotlinLogging.logger {}

    private fun kafkaConsumerFactory(): ConsumerFactory<String, RawEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,

            ConsumerConfig.GROUP_ID_CONFIG to groupId,

            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",

            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",

            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to "50"
        )

        val deserializer = ErrorHandlingDeserializer(JsonDeserializer(RawEvent::class.java))
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), deserializer)
    }

    @Bean
    fun demoTopicListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, RawEvent> =
        ConcurrentKafkaListenerContainerFactory<String, RawEvent>().apply {
            consumerFactory = kafkaConsumerFactory()
            isBatchListener = true
            containerProperties.ackMode = ContainerProperties.AckMode.BATCH
            setConcurrency(1)
            setBatchErrorHandler(kafkaErrorHandler())
            setAutoStartup(false)
        }

    private fun kafkaErrorHandler() = BatchErrorHandler { ex, data ->

        data.iterator().forEach {
            logger.warn(ex) { "kafka error: ${it}" }
        }

      /*
        val rootCause = Throwables.getRootCause(ex)

        if (rootCause is InterruptedException) {
            logger.info("kafka event handling was interrupted (possibly shutdown). BatchSize {}", data!!.count())
        } else {
            if (data == null) {
                logger.error("error handling kafka event", ex)
            } else {
                logger.error("error handling kafka event batch size {}", data.count(), ex)
            }
        }

       */
    }

    @PreDestroy
    internal fun shutdownKafkaConsumers() {
        kafkaListenerEndpointRegistry.getListenerContainer("demo_topic_listener").stop()
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        startKafkaConsumers()
    }

    private fun startKafkaConsumers() {
        Thread {
            try {
                kafkaListenerEndpointRegistry.getListenerContainer("demo_topic_listener").start()
                logger.info("started Kafka listeners {}", kafkaListenerEndpointRegistry.listenerContainerIds)
            } catch (e: Exception) {
                logger.error("unable to start Kafka consumer", e)
            }
        }.start()
    }
}
