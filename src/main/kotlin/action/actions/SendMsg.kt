package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.action.actions.SendGroupMsg.send
import top.mrxiaom.kritor.adapter.onebot.action.actions.SendPrivateMsg.send
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter

@Action("send_msg", "send_message")
object SendMsg : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val acceptTypes = listOf("private", "group")
        var type = data["type"]?.asString?.lowercase() ?: ""
        if (acceptTypes.contains(type)) {
            if (data.has("user_id")) {
                type = "private"
            } else if (data.has("group_id")) {
                type = "group"
            }
        }
        if (!acceptTypes.contains(type)) throw IllegalStateException("无法判断消息类型 $type")
        if (type == "private") {
            send(this, wrap, echo,
                data["user_id"].asLong,
                data["message"],
                data["group_id"]?.asString,
                data["retry_count"]?.asInt
            )
        }
        if (type == "group") {
            send(this, wrap, echo,
                data["group_id"].asString,
                data["message"],
                data["retry_count"]?.asInt
            )
        }
    }
}
