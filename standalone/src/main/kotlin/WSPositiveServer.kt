package top.mrxiaom.kritor.adapter.onebot.standalone

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import org.java_websocket.WebSocket
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import top.mrxiaom.kritor.adapter.onebot.connection.IAdapter
import top.mrxiaom.kritor.adapter.onebot.standalone.utils.CloseCode
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.TreeMap

class WSPositiveServer(
    address: InetSocketAddress,
    override val logger: Logger,
    private val token: String, // TODO: 该参数不稳定
    scope: CoroutineScope,
) : WebSocketServer(address), IAdapter {
    override val scope: CoroutineScope = scope + CoroutineName("KrigacyWebSocketServer")
    private val listeners = TreeMap<String, suspend IAdapter.(JsonObject, JsonElement) -> Unit>(String.CASE_INSENSITIVE_ORDER)

    override suspend fun push(s: String) {
        broadcast(s)
    }

    override fun addActionListener(vararg names: String, block: suspend IAdapter.(JsonObject, JsonElement) -> Unit) {
        for (name in names) {
            listeners[name] = block
        }
    }

    override fun action(name: String): (suspend IAdapter.(JsonObject, JsonElement) -> Unit)? {
        return listeners[name]
    }

    override fun clearActionListener() {
        listeners.clear()
    }

    override fun onOpen(client: WebSocket, handshake: ClientHandshake) {
        if (token.isNotBlank()) {
            if (handshake.hasFieldValue("Authorization")) {
                val param = handshake.getFieldValue("Authorization").run {
                    if (lowercase().startsWith("bearer ")) substring(7) else this
                }
                if (param != token) {
                    client.close(CloseFrame.NORMAL, "客户端提供的 token 错误")
                    return
                }
            } else if (handshake.resourceDescriptor.contains("access_token=")) {
                val param = handshake.resourceDescriptor.substringAfter("access_token=").substringBefore("&")
                if (param != token) {
                    client.close(CloseFrame.NORMAL, "客户端提供的 token 错误")
                    return
                }
            } else {
                client.close(CloseFrame.NORMAL, "客户端未提供 token")
                return
            }
        }
        logger.info("▌ 正向 WebSocket 客户端 ${client.remoteSocketAddress} 已连接 ┈━═☆")
        // TODO: 连接成功
    }

    override fun onClose(client: WebSocket, code: Int, reason: String, remote: Boolean) {
        logger.info(
            "▌ 正向 WebSocket 客户端连接因 {} 已关闭 (关闭码: {})",
            reason.ifEmpty { "未知原因" },
            CloseCode.valueOf(code) ?: code
        )
    }

    override fun onMessage(client: WebSocket, message: String) = onReceiveMessage(message)

    override fun onError(client: WebSocket, ex: Exception) {
        logger.error("▌ 正向 WebSocket 客户端连接出现错误 {} 或未连接 ┈━═☆", ex.localizedMessage)
    }

    override fun onStart() {
        logger.info("▌ 正向 WebSocket 服务端已在 $address 启动")
        logger.info("▌ 正在等待客户端连接...")
    }
}
