package top.mrxiaom.kritor.adapter.onebot.action.actions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kritor.core.CoreServiceGrpcKt
import io.kritor.core.getVersionRequest
import top.mrxiaom.kritor.adapter.onebot.action.Action
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.connection.ChannelWrapper
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter

@Action("get_version_info")
object GetVersionInfo : IAction {
    override suspend fun IAdapter.execute(wrap: ChannelWrapper, data: JsonObject, echo: JsonElement) {
        val stub = CoreServiceGrpcKt.CoreServiceCoroutineStub(wrap.channel)
        val resp = stub.getVersion(getVersionRequest {})
        ok(echo) {
            put("app_name", resp.appName)
            put("app_version", resp.version)
            put("app_full_name", "${resp.appName} ${resp.version}")
            put("version", resp.version)
        }
    }
}
