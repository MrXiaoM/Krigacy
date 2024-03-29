package top.mrxiaom.kritor.adapter.onebot.connection

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import top.mrxiaom.kritor.adapter.onebot.action.IAction
import top.mrxiaom.kritor.adapter.onebot.action.execute

interface IAdapter {
    val logger: Logger
    val scope: CoroutineScope
    val channel: ChannelWrapper

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
            val type = json["action"]?.asString ?: throw JsonSyntaxException("找不到请求类型 action")
            val data = json["data"]?.asJsonObject ?: JsonObject()
            val echo = json["echo"] ?: throw JsonSyntaxException("$type 找不到回调标识 echo")
            scope.launch {
                action(type)?.execute(this@IAdapter, channel, data, echo) ?: run {
                    logger.warn("未知的 action: $type")
                }
            }
        } catch (e: JsonSyntaxException) {
            logger.error("Json语法错误: {}", message)
        }
    }

    /**
     * 添加应用端主动执行监听器
     * @param action 监听器内容
     * @param names action 名称
     */
    fun addActionListener(action: IAction, vararg names: String)

    /**
     * 获取应用端监听器
     * @param name action 名称
     */
    fun action(name: String): IAction?

    /**
     * 清空监听器列表
     */
    fun clearActionListener()
}
