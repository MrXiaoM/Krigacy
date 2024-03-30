package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.core.CoreServiceGrpcKt
import io.kritor.core.getCurrentAccountRequest
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.friend.extOrNull
import io.kritor.friend.getFriendProfileCardRequest
import io.kritor.friend.getStrangerProfileCardRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter

@Action("get_friend_info")
object GetFriendInfo : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val stub = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)
        val userId = data["user_id"].asLong
        val resp = stub.getFriendProfileCard(getFriendProfileCardRequest {
            targetUins.add(userId)
        })
        if (resp.friendsProfileCardCount < 1) throw IllegalStateException("无法获取好友 $userId 的资料卡")
        val profileCard = resp.getFriendsProfileCard(0)
        ok(echo) {
            putProfileCard(profileCard)
        }
    }
}
