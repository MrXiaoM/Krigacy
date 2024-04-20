package top.mrxiaom.kritor.adapter.onebot.standalone

import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import top.mrxiaom.krigacy.BuildConstants
import top.mrxiaom.kritor.adapter.onebot.ActiveClient
import top.mrxiaom.kritor.adapter.onebot.standalone.OnebotType.*
import java.io.File
import java.net.InetSocketAddress

suspend fun main() {
    val scope = CoroutineScope(Dispatchers.IO)
    val logger = LoggerFactory.getLogger("Krigacy")
    val gson = GsonBuilder().setPrettyPrinting().create()
    val configFile = File("krigacy.json")
    val config = configFile.run {
        if (exists()) gson.fromJson(configFile.readText(), KrigacyConfig::class.java) ?: KrigacyConfig()
        else KrigacyConfig()
    }
    configFile.writeText(gson.toJson(config))

    logger.info("Krigacy v${BuildConstants.VERSION} 正在启动")
    logger.info("连接到 Kritor...")

    val client = ActiveClient.connect(
        config.kritor.address, config.kritor.port,
        config.kritor.account, config.kritor.ticket
    )
    logger.info("连接成功")
    val address = InetSocketAddress(config.onebot.port)
    when (config.onebot.type) {
        WS_POSITIVE -> {
            WSPositiveServer.start(address, logger, client.wrap(), config, scope)
            logger.info("正向 WebSocket 服务器已开启于 ws://${address.hostName}:${address.port}")
        }
        WS_REVERSED -> {
            TODO("反向 WebSocket 服务器")
        }
    }
}
