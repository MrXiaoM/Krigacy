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
import top.mrxiaom.kritor.adapter.onebot.message.MsgIdStorage
import top.mrxiaom.kritor.adapter.onebot.message.newMsgId

@Action("send_private_msg", "send_private_message", "send_friend_msg", "send_friend_message")
object SendPrivateMsg : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        send(this, wrap, echo,
            data["user_id"].asLong,
            data["message"],
            data["group_id"]?.asString,
            data["retry_count"]?.asInt
        )
    }

    suspend fun send(
        adapter: IAdapter,
        wrap: ChannelWrapper,
        echo: JsonElement,
        userId: Long,
        message: JsonElement,
        groupId: String? = null,
        retryCount: Int? = null
    ) = adapter.apply {
        val stub0 = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)
        val resp0 = stub0.getUidByUin(getUidRequest {
            targetUins.add(userId)
        })
        val uid = resp0.uidMapMap[userId] ?: throw IllegalStateException("无法获取 $userId 的 uid")
        val stub = MessageServiceGrpcKt.MessageServiceCoroutineStub(wrap.channel)
        val req = sendMessageRequest {
            contact = contact {
                peer = uid
                groupId?.run {
                    subPeer = this
                    scene = Scene.STRANGER_FROM_GROUP
                } ?: run {
                    scene = Scene.FRIEND
                    // TODO: STRANGER
                }
            }
            if (retryCount != null) this.retryCount = retryCount
            elements.addAll(MessageConverter.onebotToKritor(message))
        }
        val resp = stub.sendMessage(req)
        val messageId = MsgIdStorage.INSTANCE.put(newMsgId(req.contact, resp.messageId))
        ok(echo) {
            put("message_id", messageId)
        }
    }
}
