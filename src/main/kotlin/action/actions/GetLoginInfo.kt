package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.core.CoreServiceGrpcKt
import io.kritor.core.getCurrentAccountRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter

@Action("get_login_info")
object GetLoginInfo : IAction {
    override suspend fun IAdapter.execute(channel: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val stub = CoreServiceGrpcKt.CoreServiceCoroutineStub(channel.channel)
        val resp = stub.getCurrentAccount(getCurrentAccountRequest {})
        push(echo) {
            put("user_id", resp.accountUin)
            put("user_uid", resp.accountUid)
            put("nickname", resp.accountName)
        }
    }
}
