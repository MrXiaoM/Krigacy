package top.mrxiaom.kritor.adapter.onebot.message

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.protobuf.ByteString
import io.kritor.common.*
import io.kritor.common.Element.ElementType
import io.kritor.common.Element.ElementType.*
import io.kritor.common.ImageElement.ImageType
import io.kritor.common.MusicElement.MusicPlatform
import top.mrxiaom.kritor.adapter.onebot.utils.addJsonObject
import top.mrxiaom.kritor.adapter.onebot.utils.buildJsonArray
import top.mrxiaom.kritor.adapter.onebot.utils.putJsonArray
import java.util.*

object MessageConverter {
    fun onebotToKritor(messages: JsonElement): List<Element> {
        // 不支持 CQ 码，一律当成纯文本处理
        if (!messages.isJsonArray) return listOf(element {
            type = TEXT
            text = textElement {
                text = if (messages.isJsonPrimitive) messages.asString else messages.toString()
            }
        })
        return buildList {
            for (e in messages.asJsonArray) {
                val obj = e.asJsonObject
                val msgType = onebotMsgTypeToKritor(obj["type"].asString) ?: continue
                val data = obj["data"]?.asJsonObject ?: JsonObject()
                add(element {
                    type = msgType
                    when (type) {
                        TEXT -> text {
                            text = data["text"].asString
                        }
                        AT -> at {
                            uin = data["qq"].asString.toLong()
                        }
                        FACE -> face {
                            id = data["id"].asString.toInt()
                        }
                        BUBBLE_FACE -> bubbleFace {
                            id = data["id"].asString.toInt()
                            count = data["count"].asString.toInt()
                        }
                        REPLY -> reply {
                            messageId = data["id"].asString
                        }
                        IMAGE -> image {
                            type = when (data["type"]?.asString) {
                                "flash" -> ImageType.FLASH
                                "origin" -> ImageType.ORIGIN
                                else -> ImageType.COMMON
                            }
                            resolveOnebotFile(
                                data["file"].asString,
                                { file = it }, { filePath = it }, { fileUrl = it }
                            )
                        }
                        VOICE -> voice {
                            resolveOnebotFile(
                                data["file"].asString,
                                { file = it }, { filePath = it }, { fileUrl = it }
                            )
                            data["file_md5"]?.run { fileMd5 = asString }
                            if (data["magic"]?.asInt == 1) {
                                magic = true
                            }
                        }
                        VIDEO -> video {
                            resolveOnebotFile(
                                data["file"].asString,
                                { file = it }, { filePath = it }, { fileUrl = it }
                            )
                            data["file_md5"]?.run { fileMd5 = asString }
                        }
                        BASKETBALL -> basketball {
                            id = data["id"].asString.toInt()
                        }
                        DICE -> dice {
                            id = data["id"].asString.toInt()
                        }
                        RPS -> rps {
                            id = data["id"].asString.toInt()
                        }
                        POKE -> poke {
                            id = data["id"].asString.toInt()
                            type = data["type"].asString.toInt()
                            strength = data["strength"]?.asString?.toInt() ?: 1 // Kritor
                        }
                        MUSIC -> music {
                            platform = when(data["type"].asString) {
                                "qq" -> MusicPlatform.QQ
                                "163" -> MusicPlatform.NetEase
                                else -> MusicPlatform.Custom
                            }
                            if (platform != MusicPlatform.Custom) {
                                id = data["id"].asString
                            } else custom = customMusicData {
                                url = data["url"].asString
                                audio = data["audio"].asString
                                title = data["title"].asString
                                author = data["content"]?.asString ?: ""
                                pic = data["image"]?.asString ?: ""
                            }
                        }
                        WEATHER -> weather {
                            city = data["city"].asString
                            code = data["code"].asString
                        }
                        LOCATION -> location {
                            lat = data["lat"].asString.toFloat()
                            lon = data["lon"].asString.toFloat()
                            title = data["title"]?.asString ?: ""
                            address = data["content"]?.asString ?: ""
                        }
                        SHARE -> share {
                            url = data["url"].asString
                            title = data["title"].asString
                            content = data["content"]?.asString ?: ""
                            image = data["image"]?.asString ?: ""
                        }
                        GIFT -> gift {
                            qq = data["qq"].asString.toLong()
                            id = data["id"].asString.toInt()
                        }
                        CONTACT -> contact {
                            scene = when (data["type"].asString) {
                                "qq" -> Scene.FRIEND
                                "group" -> Scene.GROUP
                                else -> Scene.UNRECOGNIZED
                            }
                            peer = data["id"].asString
                        }
                        MARKET_FACE -> marketFace {
                            id = data["id"].asString
                        }
                        FORWARD -> forward {
                            resId = data["id"].asString
                            data["uni_seq"]?.run { uniseq = asString }
                            data["summary"]?.run { summary = asString }
                            data["description"]?.run { description = asString }
                        }
                        JSON -> json {
                            json = data["data"].asString
                        }
                        XML -> xml {
                            xml = data["data"].asString
                        }
                        FILE -> file {
                            data["name"]?.run { name = asString }
                            data["size"]?.run { size = asString.toLong() }
                            data["expire_time"]?.run { expireTime = asString.toLong() }
                            data["id"]?.run { id = asString }
                            data["url"]?.run { url = asString }
                            data["biz"]?.run { biz = asString.toInt() }
                            data["sub_id"]?.run { subId = asString }
                        }
                        MARKDOWN -> markdown {
                            markdown = data["content"].asString
                        }
                        BUTTON -> button {
                            botAppid = data["bot_appid"].asString.toLong()
                            data["rows"].asJsonArray.map { e1 ->
                                val obj1 = e1.asJsonObject
                                rows.add(buttonRow {
                                    obj1["buttons"].asJsonArray.map { e2 ->
                                        val button = e2.asJsonObject
                                        buttons.add(io.kritor.common.button {
                                            id = button["id"].asString
                                            renderData = buttonRender {
                                                label = button["label"].asString
                                                visitedLabel = button["visited_label"].asString
                                                style = button["style"].asInt
                                            }
                                            action = buttonAction {
                                                this.type = button["type"].asInt
                                                this.unsupportedTips = button["unsupport_tips"].asString
                                                this.data = button["data"].asString
                                                this.reply = button["reply"]?.asBoolean ?: false
                                                this.enter = button["enter"]?.asBoolean ?: false
                                                this.permission = buttonActionPermission {
                                                    type = button["permission_type"].asInt
                                                    roleIds.addAll(button["specify_role_ids"]!!.asJsonArray.map { it.asString })
                                                    userIds.addAll(button["specify_tinyids"]!!.asJsonArray.map { it.asString })
                                                }
                                            }
                                        })
                                    }
                                })
                            }
                        }

                        else -> {}
                    }
                })
            }
        }
    }

    fun resolveOnebotFile(onebotFile: String, file: (ByteString) -> Unit, filePath: (String) -> Unit, fileUrl: (String) -> Unit) {
        when {
            onebotFile.startsWith("base64://") -> {
                file(ByteString.copyFrom(
                    Base64.getDecoder().decode(onebotFile.removePrefix("base64://"))
                ))
            }
            onebotFile.startsWith("file:///") -> {
                filePath(onebotFile.removePrefix("file:///"))
            }
            onebotFile.startsWith("http://") || onebotFile.startsWith("https://") -> {
                fileUrl(onebotFile)
            }
        }
    }

    fun onebotMsgTypeToKritor(type: String): ElementType? = when (type) {
        "text" -> TEXT
        "face" -> FACE
        "bubble_face" -> BUBBLE_FACE // Kritor
        "image" -> IMAGE
        "record" -> VOICE
        "video" -> VIDEO
        "at" -> AT
        "rps" -> RPS
        "dice" -> DICE
        "basketball" -> BASKETBALL // Kritor
        "poke" -> POKE
        "share" -> SHARE
        "contact" -> CONTACT
        "location" -> LOCATION
        "weather" -> WEATHER // Kritor
        "gift" -> GIFT // go-cqhttp
        "music" -> MUSIC
        "reply" -> REPLY
        "forward" -> FORWARD
        // "node" -> TODO()
        "xml" -> XML
        "json" -> JSON
        "file" -> FILE // Kritor
        "markdown" -> MARKDOWN // Shamrock, Gensokyo
        "inline_keyboard" -> BUTTON // Shamrock, Gensokyo
        else -> null
    }

    fun kritorToOnebot(messages: List<Element>): JsonElement = buildJsonArray {
        for (element in messages) element.apply {
            val msgType = kritorMsgTypeToOnebot(type) ?: return@apply
            addJsonObject {
                put("type", msgType)
                when (type) {
                    TEXT -> text.apply {
                        put("text", text)
                    }
                    AT -> at.apply {
                        put("qq", uin)
                    }
                    FACE -> face.apply {
                        put("id", id)
                    }
                    BUBBLE_FACE -> bubbleFace.apply {
                        put("id", id)
                        put("count", count)
                    }
                    REPLY -> reply.apply {
                        put("id", messageId)
                    }
                    IMAGE -> image.apply {
                        when (type) {
                            ImageType.FLASH -> put("type","flash")
                            ImageType.ORIGIN -> put("type", "origin")
                            else -> {}
                        }
                        resolveKritorFile(file, filePath, fileUrl)?.also {
                            put("file", it)
                        }
                    }
                    VOICE -> voice.apply {
                        resolveKritorFile(file, filePath, fileUrl)?.also {
                            put("file", it)
                        }
                        if (hasFileMd5()) put("file_md5", fileMd5)
                        if (hasMagic() && magic) put("magic", 1)
                    }
                    VIDEO -> video.apply {
                        resolveKritorFile(file, filePath, fileUrl)?.also {
                            put("file", it)
                        }
                        if (hasFileMd5()) put("file_md5", fileMd5)
                    }
                    BASKETBALL -> basketball.apply {
                        put("id", id)
                    }
                    DICE -> dice.apply {
                        put("id", id)
                    }
                    RPS -> rps.apply {
                        put("id", id)
                    }
                    POKE -> poke.apply {
                        put("id", id)
                        put("type", type)
                        put("strength", strength)
                    }
                    MUSIC -> music.apply {
                        if (platform != MusicPlatform.Custom) {
                            put("id", id)
                        } else custom.apply {
                            put("url", url)
                            put("audio", audio)
                            put("title", title)
                            put("content", author)
                            put("image", pic)
                        }
                    }
                    WEATHER -> weather.apply {
                        put("city", city)
                        put("code", code)
                    }
                    LOCATION -> location.apply {
                        put("lat", lat)
                        put("lon", lon)
                        put("title", title)
                        put("content", address)
                    }
                    SHARE -> share.apply {
                        put("url", url)
                        put("title", title)
                        put("content", content)
                        put("image", image)
                    }
                    GIFT -> gift.apply {
                        put("qq", qq)
                        put("id", id)
                    }
                    CONTACT -> contact.apply {
                        when (scene) {
                            Scene.FRIEND -> put("type", "qq")
                            Scene.GROUP -> put("type", "group")
                            else -> {}
                        }
                        put("id", peer)
                    }
                    MARKET_FACE -> marketFace.apply {
                        put("id", id)
                    }
                    FORWARD -> forward.apply {
                        put("id", resId)
                        put("uni_seq", uniseq)
                        put("summary", summary)
                        put("description", description)
                    }
                    JSON -> json.apply {
                        put("data", json)
                    }
                    XML -> xml.apply {
                        put("data", xml)
                    }
                    FILE -> file.apply {
                        if (hasName()) put("name", name)
                        if (hasSize()) put("size", size)
                        if (hasExpireTime()) put("expire_time", expireTime)
                        if (hasId()) put("id", id)
                        if (hasUrl()) put("url", url)
                        if (hasBiz()) put("biz", biz)
                        if (hasSubId()) put("sub_id", subId)
                    }
                    MARKDOWN -> markdown.apply {
                        put("content", markdown)
                    }
                    BUTTON -> button.apply {
                        put("bot_appid", botAppid)
                        putJsonArray("rows") {
                            rowsList.forEach { row ->
                                addJsonObject row@ {
                                    putJsonArray("buttons") {
                                        row.buttonsList.forEach { button ->
                                            addJsonObject {
                                                put("id", button.id)

                                                put("label", button.renderData.label)
                                                put("visited_label", button.renderData.visitedLabel)
                                                put("style", button.renderData.style)

                                                put("type", button.action.type)
                                                put("unsupport_tips", button.action.unsupportedTips)
                                                put("data", button.action.data)

                                                put("permission_type", button.action.permission.type)
                                                putJsonArray("specify_role_ids") {
                                                    button.action.permission.roleIdsList.forEach { add(it) }
                                                }
                                                putJsonArray("specify_tinyids") {
                                                    button.action.permission.userIdsList.forEach { add(it) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun resolveKritorFile(file: ByteString, filePath: String, fileUrl: String): String? {
        if (!file.isEmpty) {
            return "base64://" + Base64.getEncoder().encodeToString(file.toByteArray())
        }
        if (filePath.isNotBlank()) {
            return "file:///" + filePath.removePrefix("/")
        }
        if (fileUrl.isNotBlank()) {
            return fileUrl
        }
        return null
    }
    
    fun kritorMsgTypeToOnebot(type: ElementType): String? = when (type) {
        TEXT -> "text"
        FACE -> "face"
        BUBBLE_FACE -> "bubble_face" // Kritor
        IMAGE -> "image"
        VOICE -> "record"
        VIDEO -> "video"
        AT -> "at"
        RPS -> "rps"
        DICE -> "dice"
        BASKETBALL -> "basketball"// Kritor
        POKE -> "poke"
        SHARE -> "share"
        CONTACT -> "contact"
        LOCATION -> "location"
        WEATHER -> "weather" // Kritor
        GIFT -> "gift" // go-cqhttp
        MUSIC -> "music"
        REPLY -> "reply"
        MARKET_FACE -> "market_face" // Kritor
        FORWARD -> "forward"
        XML -> "xml"
        JSON -> "json"
        FILE -> "file" // Kritor
        MARKDOWN -> "markdown"// Shamrock, Gensokyo
        BUTTON -> "inline_keyboard" // Shamrock, Gensokyo
        else -> null
    }
}

fun ElementKt.Dsl.text(block: TextElementKt.Dsl.() -> Unit) = textElement(block).also { text = it }
fun ElementKt.Dsl.at(block: AtElementKt.Dsl.() -> Unit) = atElement(block).also { at = it }
fun ElementKt.Dsl.face(block: FaceElementKt.Dsl.() -> Unit) = faceElement(block).also { face = it }
fun ElementKt.Dsl.bubbleFace(block: BubbleFaceElementKt.Dsl.() -> Unit) = bubbleFaceElement(block).also { bubbleFace = it }
fun ElementKt.Dsl.reply(block: ReplyElementKt.Dsl.() -> Unit) = replyElement(block).also { reply = it }
fun ElementKt.Dsl.image(block: ImageElementKt.Dsl.() -> Unit) = imageElement(block).also { image = it }
fun ElementKt.Dsl.voice(block: VoiceElementKt.Dsl.() -> Unit) = voiceElement(block).also { voice = it }
fun ElementKt.Dsl.video(block: VideoElementKt.Dsl.() -> Unit) = videoElement(block).also { video = it }
fun ElementKt.Dsl.basketball(block: BasketballElementKt.Dsl.() -> Unit) = basketballElement(block).also { basketball = it }
fun ElementKt.Dsl.dice(block: DiceElementKt.Dsl.() -> Unit) = diceElement(block).also { dice = it }
fun ElementKt.Dsl.rps(block: RpsElementKt.Dsl.() -> Unit) = rpsElement(block).also { rps = it }
fun ElementKt.Dsl.poke(block: PokeElementKt.Dsl.() -> Unit) = pokeElement(block).also { poke = it }
fun ElementKt.Dsl.music(block: MusicElementKt.Dsl.() -> Unit) = musicElement(block).also { music = it }
fun ElementKt.Dsl.weather(block: WeatherElementKt.Dsl.() -> Unit) = weatherElement(block).also { weather = it }
fun ElementKt.Dsl.location(block: LocationElementKt.Dsl.() -> Unit) = locationElement(block).also { location = it }
fun ElementKt.Dsl.share(block: ShareElementKt.Dsl.() -> Unit) = shareElement(block).also { share = it }
fun ElementKt.Dsl.gift(block: GiftElementKt.Dsl.() -> Unit) = giftElement(block).also { gift = it }
fun ElementKt.Dsl.marketFace(block: MarketFaceElementKt.Dsl.() -> Unit) = marketFaceElement(block).also { marketFace = it }
fun ElementKt.Dsl.forward(block: ForwardElementKt.Dsl.() -> Unit) = forwardElement(block).also { forward = it }
fun ElementKt.Dsl.contact(block: ContactElementKt.Dsl.() -> Unit) = contactElement(block).also { contact = it }
fun ElementKt.Dsl.json(block: JsonElementKt.Dsl.() -> Unit) = jsonElement(block).also { json = it }
fun ElementKt.Dsl.xml(block: XmlElementKt.Dsl.() -> Unit) = xmlElement(block).also { xml = it }
fun ElementKt.Dsl.file(block: FileElementKt.Dsl.() -> Unit) = fileElement(block).also { file = it }
fun ElementKt.Dsl.markdown(block: MarkdownElementKt.Dsl.() -> Unit) = markdownElement(block).also { markdown = it }
fun ElementKt.Dsl.button(block: ButtonElementKt.Dsl.() -> Unit) = buttonElement(block).also { button = it }
