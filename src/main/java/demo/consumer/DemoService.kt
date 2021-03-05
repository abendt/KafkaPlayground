package demo.consumer

import org.springframework.stereotype.Service

@Service
class DemoService {
    fun handleEvent(events: List<Action>) {
        println("handle $events")
    }
}

sealed class Action

data class AddItem(val id: String, val content: String): Action()
data class DeleteItem(val id: String): Action()