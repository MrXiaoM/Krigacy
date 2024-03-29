package top.mrxiaom.kritor.adapter.onebot.connection

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger

interface IAdapter {
    val logger: Logger
    val scope: CoroutineScope

    /**
     * 推送消息到 Onebot 应用端
     */
    suspend fun push(s: String)

    /**
     * 从 Onebot 应用端接收到消息
     */

    fun onReceiveMessage(message: String) {
        try {
            val json = JsonParser.parseString(message).asJsonObject
            val action = json["action"]?.asString ?: throw JsonSyntaxException("找不到请求类型 action")
            val data = json["data"]?.asJsonObject ?: throw JsonSyntaxException("$action 找不到请求数据 data")
            val echo = json["echo"] ?: throw JsonSyntaxException("$action 找不到回调标识 echo")
            scope.launch {
                action(action)?.invoke(this@IAdapter, data, echo) ?: run {
                    logger.warn("未知的 action: $action")
                }
            }
        } catch (e: JsonSyntaxException) {
            logger.error("Json语法错误: {}", message)
        }
    }

    /**
     * 添加应用端主动执行监听器
     * @param names action 名称
     * @param block 监听器内容，其中 JsonObject 为 data，JsonElement 为 echo
     */
    fun addActionListener(vararg names: String, block: suspend IAdapter.(JsonObject, JsonElement) -> Unit)

    /**
     * 获取应用端监听器
     * @param name action 名称
     */
    fun action(name: String): (suspend IAdapter.(JsonObject, JsonElement) -> Unit)?

    /**
     * 清空监听器列表
     */
    fun clearActionListener()
}
