package top.mrxiaom.kritor.adapter.onebot.action

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.utils.buildJsonObject

object ActionsCollector {
    fun IAdapter.addActionListeners() {
        for (action in listOf<IAction>(
        )) {
            val anno = action.findAnnotation<Action>() ?: continue
            addActionListener(action, *anno.value)
        }
    }

    private inline fun <reified T : Annotation> Any.findAnnotation(): T? {
        return this::class.java.annotations.firstNotNullOfOrNull { it as? T }
    }
}

annotation class Action(
    vararg val value: String
)

interface IAction {
    suspend fun IAdapter.execute(channel: ChannelWrapper, data: JsonObject, echo: JsonElement)

    suspend fun IAdapter.push(echo: JsonElement, block: MutableMap<String, Any>.() -> Unit) {
        val json = buildJsonObject(block)
        json.add("echo", echo)
        push(json.toString())
    }
}

suspend fun IAction.execute(adapter: IAdapter, channel: ChannelWrapper, data: JsonObject, echo: JsonElement) {
    adapter.execute(channel, data, echo)
}
