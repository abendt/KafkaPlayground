package demo.consumer

import org.springframework.stereotype.Service

@Service
class DemoService {
    fun handleActions(actions: List<Action>) {
        println("handle $actions")
    }
}

sealed class Action
data class AddItem(val id: String, val content: String): Action()
data class DeleteItem(val id: String): Action()
