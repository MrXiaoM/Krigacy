package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.core.CoreServiceGrpcKt
import io.kritor.core.getCurrentAccountRequest
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.friend.setProfileCardRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter

@Action("set_qq_profile")
object SetQQProfile : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val stub = FriendServiceGrpcKt.FriendServiceCoroutineStub(wrap.channel)
        stub.setProfileCard(setProfileCardRequest {
            data["nickname"]?.run { nickName = asString }
            data["company"]?.run { company = asString }
            data["email"]?.run { email = asString }
            data["college"]?.run { college = asString }
            data["personal_note"]?.run { personalNote = asString }
            data["birthday"]?.run { birthday = asInt }
            data["age"]?.run { age = asInt }
        })
        ok(echo)
    }
}
