package top.mrxiaom.kritor.adapter.onebot.standalone

import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
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
}
