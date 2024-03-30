package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.common.Element
import io.kritor.common.Scene
import io.kritor.common.contact
import io.kritor.message.MessageServiceGrpcKt
import io.kritor.message.sendMessageRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.message.MessageConverter
import top.mrxiaom.kritor.adapter.onebot.message.MsgIdStorage
import top.mrxiaom.kritor.adapter.onebot.message.newMsgId

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
    ) = send(adapter, wrap, echo, groupId, MessageConverter.onebotToKritor(message), retryCount)

    suspend fun send(
        adapter: IAdapter,
        wrap: ChannelWrapper,
        echo: JsonElement,
        groupId: String,
        message: List<Element>,
        retryCount: Int? = null
    ) = adapter.apply {
        val stub = MessageServiceGrpcKt.MessageServiceCoroutineStub(wrap.channel)
        val req = sendMessageRequest {
            contact {
                scene = Scene.GROUP
                peer = groupId
            }
            if (retryCount != null) this.retryCount = retryCount
            elements.addAll(message)
        }
        val resp = stub.sendMessage(req)
        val messageId = MsgIdStorage.INSTANCE.put(newMsgId(req.contact, resp.messageId))
        ok(echo) {
            put("message_id", messageId)
        }
    }
}