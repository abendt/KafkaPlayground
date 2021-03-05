package demo

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import demo.consumer.AddItem
import demo.consumer.DeleteItem
import demo.consumer.DemoService
import demo.consumer.ErrorHandler
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import strikt.api.expectThat
import strikt.assertions.isTrue

@SpringBootTest
class KafkaConsumerIT : KafkaTestcase() {

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @MockkBean
    lateinit var demoService: DemoService

    @SpykBean
    lateinit var errorHandler: ErrorHandler

    @Test
    fun kafkaIsStarted() {
        expectThat(kafkaContainer.isRunning).isTrue()
    }

    @Test
    fun canSendSomethingToKafka() {
        kafkaTemplate.send("myTopic", "hello")

        verify(timeout = 5000) { demoService.handleActions(any()) }
    }

    @Test
    fun canSendDelete() {
        kafkaTemplate.send("myTopic", """{"action": "DELETE", "id": "myId"}""")

        verify(timeout = 5000) { demoService.handleActions(withArg { list -> list.find { it is DeleteItem } }) }
    }

    @Test
    fun canSendAdd() {
        kafkaTemplate.send("myTopic", """{"action": "ADD", "id": "myId", "content", "myContent"}""")

        verify(timeout = 5000) { demoService.handleActions(withArg { list -> list.find { it is AddItem } }) }
    }

    @Test
    fun canHandleWrongFormat() {
        kafkaTemplate.send("myTopic", """{"action": "ADD fooBar """)

        verify(timeout = 5000) { demoService.handleActions(emptyList()) }
        verify(timeout = 5000) { errorHandler.handleInvalidEvent(any()) }
    }

    @Test
    fun canHandleWrongContract() {
        kafkaTemplate.send("myTopic", """{"action": "ADD", "id": "myId"}""")

        verify(timeout = 5000) { demoService.handleActions(emptyList()) }
        verify(timeout = 5000) { errorHandler.handleInvalidAction(any()) }
    }
}
