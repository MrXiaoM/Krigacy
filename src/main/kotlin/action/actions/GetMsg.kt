package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.common.Scene
import io.kritor.common.contact
import io.kritor.message.MessageServiceGrpcKt
import io.kritor.message.getMessageBySeqRequest
import io.kritor.message.getMessageRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.message.MessageConverter
import top.mrxiaom.kritor.adapter.onebot.message.MsgIdStorage
import top.mrxiaom.kritor.adapter.onebot.message.contact
import top.mrxiaom.kritor.adapter.onebot.utils.putJsonObject

@Action("get_msg", "get_message")
object GetMsg : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val id32 = data["message_id"].asInt
        val msgId = MsgIdStorage.INSTANCE.getOrNull(id32) ?: throw IllegalStateException("没有储存旧版消息ID $id32 对应的消息ID" )
        val stub = MessageServiceGrpcKt.MessageServiceCoroutineStub(wrap.channel)
        val resp = stub.getMessage(getMessageRequest {
            contact = msgId.contact
            messageId = msgId.messageId
        })
        val message = MessageConverter.kritorToOnebot(resp.message.elementsList)
        ok(echo) {
            put("time", resp.message.time)
            put("message_type", when(resp.message.contact.scene) {
                Scene.GROUP -> "group"
                else -> "private"
            })
            put("message_id", id32)
            put("real_id", id32)
            putJsonObject("sender") {
                put("user_id", resp.message.sender.uin)
                put("nickname", resp.message.sender.nick)
            }
            put("message", message)
        }
    }
}
