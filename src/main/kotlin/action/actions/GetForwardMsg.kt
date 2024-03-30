package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.common.Scene
import io.kritor.common.contact
import io.kritor.message.MessageServiceGrpcKt
import io.kritor.message.downloadForwardMessageRequest
import io.kritor.message.getMessageBySeqRequest
import io.kritor.message.getMessageRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.message.MessageConverter
import top.mrxiaom.kritor.adapter.onebot.message.MsgIdStorage
import top.mrxiaom.kritor.adapter.onebot.message.contact
import top.mrxiaom.kritor.adapter.onebot.utils.buildJsonObject
import top.mrxiaom.kritor.adapter.onebot.utils.putJsonArray
import top.mrxiaom.kritor.adapter.onebot.utils.putJsonObject

@Action("get_forward_msg", "get_forward_message")
object GetForwardMsg : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val stub = MessageServiceGrpcKt.MessageServiceCoroutineStub(wrap.channel)
        val resp = stub.downloadForwardMessage(downloadForwardMessageRequest {
            resId = data["id"].asString
        })
        ok(echo) {
            putJsonArray("message") {
                for (message in resp.messagesList) {
                    buildJsonObject {
                        put("type", "node")
                        putJsonObject("data") {
                            put("user_id", message.sender.uin)
                            put("nickname", message.sender.nick)
                            put("time", message.time)
                            put("message_id_new", message.messageId)
                            put("message_seq", message.messageSeq)
                            put("content", MessageConverter.kritorToOnebot(message.elementsList))
                        }
                    }
                }
            }
        }
    }
}
