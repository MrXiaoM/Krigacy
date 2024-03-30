package top.mrxiaom.kritor.adapter.onebot.action

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import top.mrxiaom.kritor.adapter.onebot.action.actions.*
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.utils.buildJsonArray
import top.mrxiaom.kritor.adapter.onebot.utils.buildJsonObject
import top.mrxiaom.kritor.adapter.onebot.utils.putJsonObject

object ActionsCollector {
    fun IAdapter.addActionListeners() {
        // 按 go-cqhttp 文档分类
        // https://docs.go-cqhttp.org/api
        for (action in listOf(
            // Bot 账号
            GetLoginInfo, SetQQProfile,
            // _get_model_show, _set_model_show, get_online_clients

            // 好友信息
            GetStrangerInfo, GetFriendList, GetFriendInfo,
            // get_unidirectional_friend_list

            // 好友操作
            // delete_friend, delete_unidirectional_friend

            // 消息
            SendPrivateMsg, SendGroupMsg, SendMsg, GetMsg, DeleteMsg, GetForwardMsg, SendGroupForwardMsg, SendPrivateForwardMsg,
            // mark_msg_as_read

            // go-cqhttp 相关
            GetVersionInfo,
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
    suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement)
    suspend fun IAdapter.ok(echo: JsonElement, block: MutableMap<String, Any>.() -> Unit = {}) {
        pushActionResponse(echo, 0, null, block)
    }suspend fun IAdapter.okArray(echo: JsonElement, block: MutableList<Any>.() -> Unit = {}) {
        pushActionResponseArray(echo, 0, null, block)
    }
    suspend fun IAdapter.failed(echo: JsonElement, retCode: Int, message: String, block: MutableMap<String, Any>.() -> Unit = {}) {
        pushActionResponse(echo, retCode, message, block)
    }
}
suspend fun IAdapter.pushActionResponse(echo: JsonElement, retCode: Int, message: String? = null, block: MutableMap<String, Any>.() -> Unit = {}) {
    pushActionResponse(echo, retCode, message, buildJsonObject(block))
}
suspend fun IAdapter.pushActionResponseArray(echo: JsonElement, retCode: Int, message: String? = null, block: MutableList<Any>.() -> Unit = {}) {
    pushActionResponse(echo, retCode, message, buildJsonArray(block))
}
suspend fun IAdapter.pushActionResponse(echo: JsonElement, retCode: Int, message: String? = null, data: JsonElement) {
    push(buildJsonObject {
        put("status", if (retCode == 0) "ok" else "failed")
        put("retcode", retCode)
        if (message != null) put("msg", message)
        put("data", data)
        put("echo", echo)
    }.toString())
}

suspend fun IAction.execute(adapter: IAdapter, type: String, channel: ChannelWrapper, data: JsonObject, echo: JsonElement) = runCatching {
    adapter.execute(channel, data, echo)
}.onFailure {
    adapter.logger.error("执行 $type 时出现错误", it)
    adapter.pushActionResponse(echo, 1500, it.toString())
}
