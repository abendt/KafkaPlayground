package demo.consumer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class DemoConsumer(val demoService: DemoService, val errorHandler: ErrorHandler) {

    private val logger = KotlinLogging.logger {}

    private val objectMapper = jacksonObjectMapper()

    @KafkaListener(
        id = "demo_topic_listener",
        idIsGroup = false,
        topics = ["\${kafka.demotopic.topicname}"],
        containerFactory = "demoTopicListenerContainerFactory"
    )
    private fun receive(rawEvents: List<String>) {

        logger.debug { "rawEvents $rawEvents" }

        val parsedEvents: List<RawEvent> = rawEvents.mapNotNull {
            try {
                objectMapper.readValue<RawEvent>(it)
            } catch (e: Exception) {
                // invalid payload
                logger.warn(e) { "skipping invalid event $it" }

                errorHandler.handleInvalidEvent(it)

                null
            }
        }

        logger.debug { "parsedEvents $parsedEvents" }

        val actions: List<Action> = parsedEvents.mapNotNull {
            when (it.action) {
                "DELETE" -> DeleteItem(it.id)

                "ADD" ->
                    if (it.content == null) {
                        logger.warn { "skipping incomplete event" }

                        errorHandler.handleInvalidAction(it)

                        null
                    } else {
                        AddItem(it.id, it.content)
                    }

                else -> {
                    errorHandler.handleInvalidAction(it)

                    null
                }
            }
        }

        logger.debug { "Actions $actions" }

        demoService.handleActions(actions)
    }
}

@Service
class ErrorHandler {
    fun handleInvalidEvent(rawEvent: String) {
    }

    fun handleInvalidAction(rawEvent: RawEvent) {
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RawEvent(
    val id: String,
    val action: String,
    val content: String?
)
