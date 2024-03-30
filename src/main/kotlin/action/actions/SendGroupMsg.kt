package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.common.Scene
import io.kritor.common.contact
import io.kritor.core.CoreServiceGrpcKt
import io.kritor.core.getCurrentAccountRequest
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.friend.getUidRequest
import io.kritor.friend.setProfileCardRequest
import io.kritor.message.MessageServiceGrpcKt
import io.kritor.message.sendMessageRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.message.MessageConverter

@Action("send_group_msg", "send_group_message")
object SendGroupMsg : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        send(this, wrap, echo,
            data["group_id"].asString,
            data["message"],
            data["retry_count"]?.asInt
        )
    }

    suspend fun send(
        adapter: IAdapter,
        wrap: ChannelWrapper,
        echo: JsonElement,
        groupId: String,
        message: JsonElement,
        retryCount: Int? = null
    ) = adapter.apply {
        val stub = MessageServiceGrpcKt.MessageServiceCoroutineStub(wrap.channel)
        val resp = stub.sendMessage(sendMessageRequest {
            contact {
                scene = Scene.GROUP
                peer = groupId
            }
            if (retryCount != null) this.retryCount = retryCount
            elements.addAll(MessageConverter.onebotToKritor(message))
        })
        val messageId = resp.messageId.toIntOrNull() ?: throw IllegalStateException("Kritor 返回的消息ID类型不为 Int32")
        ok(echo) {
            put("message_id", messageId)
        }
    }
}