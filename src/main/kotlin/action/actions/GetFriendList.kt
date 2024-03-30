package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.core.CoreServiceGrpcKt
import io.kritor.core.getCurrentAccountRequest
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.friend.extOrNull
import io.kritor.friend.getFriendListRequest
import io.kritor.friend.getStrangerProfileCardRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.utils.addJsonObject

@Action("get_friend_list")
object GetFriendList : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val stub = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)
        val resp = stub.getFriendList(getFriendListRequest { refresh = data["no_cache"]?.asBoolean ?: true })

        okArray(echo) {
            for (friendInfo in resp.friendsInfoList) addJsonObject {
                put("user_id", friendInfo.uin)
                put("nickname", friendInfo.nick)
                put("remark", friendInfo.remark)

                put("uid", friendInfo.uid)
                put("qid", friendInfo.qid)
                put("level", friendInfo.level)
                put("age", friendInfo.age)
                put("vote_count", friendInfo.voteCnt)
                put("sex", when(friendInfo.gender) {
                    // TODO: 数值对应性别未说明
                    else -> "unknown"
                })
                put("friend_group_id", friendInfo.groupId)

                friendInfo.extOrNull?.also {
                    put("big_vip", it.bigVip)
                    put("hollywood_vip", it.hollywoodVip)
                    put("qq_vip", it.qqVip)
                    put("super_vip", it.superVip)
                    put("voted", it.voted)
                }
            }
        }
    }
}
