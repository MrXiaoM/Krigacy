package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.common.*
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.friend.getUidRequest
import io.kritor.message.MessageServiceGrpcKt
import io.kritor.message.uploadForwardMessageRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.message.MessageConverter
import top.mrxiaom.kritor.adapter.onebot.message.forward

@Action("send_group_forward_msg", "send_group_forward_message")
object SendGroupForwardMsg : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        send(this, wrap, echo,
            data["group_id"].asString,
            data["messages"].asJsonArray,
            data["uni_seq"]?.asString,
            data["summary"]?.asString,
            data["description"]?.asString,
            data["retry_count"]?.asInt
        )
    }

    suspend fun send(
        adapter: IAdapter,
        wrap: ChannelWrapper,
        echo: JsonElement,
        groupId: String,
        nodes: JsonArray,
        uniSeq: String? = null,
        summary: String? = null,
        description: String? = null,
        retryCount: Int? = null
    ) = adapter.apply {
        val stub0 = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)

        val stub = MessageServiceGrpcKt.MessageServiceCoroutineStub(wrap.channel)

        // 参考 OpenShamrock 的实现，PushMessageBody 忽略参数 time、message_id、message_seq、contact
        val reqUpload = uploadForwardMessageRequest {
            contact = contact {
                scene = Scene.GROUP
                peer = groupId
            }
            for (e in nodes) {
                val data = e.asJsonObject["data"].asJsonObject
                messages.add(forwardMessageBody {
                    message = pushMessageBody {
                        data["user_id"]?.asString?.also {
                            val userId = it.toLong()
                            val resp0 = stub0.getUidByUin(getUidRequest {
                                targetUins.add(userId)
                            })
                            val uid = resp0.uidMapMap[userId] ?: throw IllegalStateException("无法获取 $userId 的 uid")
                            sender = sender {
                                this.uid = uid
                                data["nickname"]?.run { nick = asString }
                            }
                        }
                        elements.addAll(MessageConverter.onebotToKritor(data["message"]))
                        // 忽略以下参数
                        time = 0
                        messageId = "krigacy"
                        messageSeq = 0
                        contact = this@uploadForwardMessageRequest.contact
                    }
                })
            }
            if (retryCount != null) this.retryCount = retryCount
        }
        val resId = stub.uploadForwardMessage(reqUpload).resId

        SendGroupMsg.send(adapter, wrap, echo, groupId, listOf(
            element {
                type = Element.ElementType.FORWARD
                forward {
                    this.resId = resId
                    if (uniSeq != null) this.uniseq = uniSeq
                    if (summary != null) this.summary = summary
                    if (description != null) this.description = description
                }
            }
        ), retryCount)
    }
}