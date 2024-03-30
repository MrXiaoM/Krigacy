package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.common.Element
import io.kritor.common.Scene
import io.kritor.common.contact
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.friend.getUidRequest
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
    ) {
        val uid = getUid(wrap, userId)
        send(adapter, wrap, echo, uid, MessageConverter.onebotToKritor(message), groupId, retryCount)
    }
    suspend fun send(
        adapter: IAdapter,
        wrap: ChannelWrapper,
        echo: JsonElement,
        uid: String,
        message: List<Element>,
        groupId: String? = null,
        retryCount: Int? = null
    ) = adapter.apply {
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
            elements.addAll(message)
        }
        val resp = stub.sendMessage(req)
        val messageId = MsgIdStorage.INSTANCE.put(newMsgId(req.contact, resp.messageId))
        ok(echo) {
            put("message_id", messageId)
        }
    }

    suspend fun getUid(wrap: ChannelWrapper, uin: Long): String {
        val stub0 = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)
        val resp0 = stub0.getUidByUin(getUidRequest {
            targetUins.add(uin)
        })
        return resp0.uidMapMap[uin] ?: throw IllegalStateException("无法获取 $uin 的 uid")
    }
}
