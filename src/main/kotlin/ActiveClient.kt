package top.mrxiaom.kritor.adapter.onebot

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.kritor.authentication.AuthenticateResponse
import io.kritor.authentication.AuthenticationServiceGrpc
import io.kritor.authentication.AuthenticationServiceGrpcKt
import io.kritor.authentication.authenticateRequest
import io.kritor.common.PushMessageBody
import io.kritor.common.Scene
import io.kritor.common.Scene.*
import io.kritor.common.contactOrNull
import io.kritor.event.*
import io.kritor.event.EventStructure.EventCase
import io.kritor.message.MessageServiceGrpcKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper

class ActiveClient private constructor(
    val channel: ManagedChannel
) {
    private suspend fun init() {
        EventServiceGrpcKt.EventServiceCoroutineStub(channel).registerActiveListener(
            requestPushEvent { type = EventType.EVENT_TYPE_MESSAGE }
        ).collect { it.messageOrNull?.on() }

        EventServiceGrpcKt.EventServiceCoroutineStub(channel).registerActiveListener(
            requestPushEvent { type = EventType.EVENT_TYPE_NOTICE }
        ).collect { it.noticeOrNull?.on() }

        EventServiceGrpcKt.EventServiceCoroutineStub(channel).registerActiveListener(
            requestPushEvent { type = EventType.EVENT_TYPE_NOTICE }
        ).collect { it.requestOrNull?.on() }
    }

    fun wrap(): ChannelWrapper {
        return ChannelWrapper.wrap(channel)
    }

    suspend fun PushMessageBody.on() {
        when (contactOrNull?.scene) {
            GROUP -> {
                TODO("群聊")
            }

            FRIEND -> {
                TODO("私聊")
            }

            GUILD -> {
                TODO("频道")
            }

            STRANGER_FROM_GROUP -> {
                TODO("群临时会话")
            }

            NEARBY -> {
                TODO("附近的人")
            }

            STRANGER -> {
                TODO("陌生人")
            }

            else -> {
                TODO("未知消息事件")
            }
        }
    }

    suspend fun NoticeEvent.on() {
        friendPokeOrNull?.apply { // 好友头像戳一戳

        }
        friendRecallOrNull?.apply { // 好友消息撤回

        }
        friendFileUploadedOrNull?.apply { // 私聊文件上传

        }
        groupPokeOrNull?.apply { // 群头像戳一戳

        }
        groupCardChangedOrNull?.apply { // 群名片改变

        }
        groupMemberUniqueTitleChangedOrNull?.apply { // 群成员专属头衔改变

        }
        groupEssenceChangedOrNull?.apply { // 群精华消息改变

        }
        groupRecallOrNull?.apply { // 群消息撤回

        }
        groupMemberIncreaseOrNull?.apply { // 群成员增加

        }
        groupMemberDecreaseOrNull?.apply { // 群成员减少

        }
        groupAdminChangeOrNull?.apply { // 群管理员变动

        }
        groupMemberBanOrNull?.apply { // 群成员被禁言

        }
        groupSignInOrNull?.apply { // 群签到

        }
        groupWholeBanOrNull?.apply { // 群全员禁言

        }
        groupFileUploadedOrNull?.apply { // 群文件上传

        }
    }

    suspend fun RequestsEvent.on() {
        friendApplyOrNull?.apply { // 加好友申请

        }
        groupApplyOrNull?.apply { // 主动加群申请

        }
        invitedGroupOrNull?.apply { // 邀请机器人进群

        }
    }


    companion object {
        suspend fun connect(
            address: String,
            port: Int,
            account: String,
            ticket: String
        ): ActiveClient {
            val channel = ManagedChannelBuilder
                .forAddress(address, port)
                .usePlaintext()
                .enableRetry()
                .executor(Dispatchers.IO.asExecutor())
                .build()

            channel.auth(account, ticket)

            return ActiveClient(channel).also { it.init() }
        }
    }
}
suspend fun Channel.auth(account: String, ticket: String) {
    val stub = AuthenticationServiceGrpcKt.AuthenticationServiceCoroutineStub(this)
    runCatching {
        val rsp = stub.authenticate(authenticateRequest {
            this.account = account
            this.ticket = ticket
        })
        println(rsp)
    }.onFailure {
        val status = Status.fromThrowable(it)
        println(status)
        println(status.code)
        println(status.description)
    }
}