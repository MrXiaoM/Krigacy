package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.core.CoreServiceGrpcKt
import io.kritor.core.getCurrentAccountRequest
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.friend.extOrNull
import io.kritor.friend.getStrangerProfileCardRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter

@Action("get_stranger_info")
object GetStrangerInfo : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val stub = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)
        val userId = data["user_id"].asLong
        val resp = stub.getStrangerProfileCard(getStrangerProfileCardRequest {
            targetUins.add(userId)
        })
        if (resp.strangersProfileCardCount < 1) throw IllegalStateException("无法获取 $userId 的资料卡")
        val profileCard = resp.getStrangersProfileCard(0)
        ok(echo) {
            put("user_id", profileCard.uin)
            put("nickname", profileCard.nick)
            put("remark", profileCard.remark ?: "")
            put("sex", "unknown") // TODO: Kritor 无法获取陌生人性别
            put("birthday", profileCard.birthday)
            val age = 0 // TODO: 根据未知格式的 profileCard.birthday 计算年龄
            put("age", age)
            put("qid", profileCard.qid)
            put("level", profileCard.level)
            put("login_days", profileCard.loginDay)

            put("uid", profileCard.uid)
            put("vote_count", profileCard.voteCnt)

            profileCard.extOrNull?.also {
                put("big_vip", it.bigVip)
                put("hollywood_vip", it.hollywoodVip)
                put("qq_vip", it.qqVip)
                put("super_vip", it.superVip)
                put("voted", it.voted)
            }
        }
    }
}
