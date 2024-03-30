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

@Action("send_private_msg", "send_private_message", "send_friend_msg", "send_friend_message")
object SendPrivateMsg : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val uin = data["user_id"].asLong
        val stub0 = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)
        val resp0 = stub0.getUidByUin(getUidRequest {
            targetUins.add(uin)
        })
        val uid = resp0.uidMapMap[uin] ?: throw IllegalStateException("无法获取 $uin 的 uid")
        val stub = MessageServiceGrpcKt.MessageServiceCoroutineStub(wrap.channel)
        val resp = stub.sendMessage(sendMessageRequest {
            contact {
                peer = uid
                data["group_id"]?.run {
                    subPeer = asString
                    scene = Scene.STRANGER_FROM_GROUP
                } ?: run {
                    scene = Scene.FRIEND
                    // TODO: STRANGER
                }
            }
            data["retry_count"]?.run { retryCount = asInt }
            elements.addAll(MessageConverter.onebotToKritor(data["message"]))
        })
        val messageId = resp.messageId.toIntOrNull() ?: throw IllegalStateException("Kritor 返回的消息ID类型不为 Int32")
        ok(echo) {
            put("message_id", messageId)
        }
    }
}
