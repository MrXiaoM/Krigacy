package top.mrxiaom.kritor.adapter.onebot.message

import io.kritor.common.Contact
import io.kritor.common.Scene
import io.kritor.common.contact

interface MsgIdStorage {
    fun put(msgId: MsgId): Int
    fun put(id32: Int, msgId: MsgId)
    operator fun get(id32: Int): MsgId
    fun getOrNull(id32: Int): MsgId?

    companion object {
        private lateinit var _instance: MsgIdStorage
        val INSTANCE: MsgIdStorage
            get() = _instance
        val registered: Boolean
            get() = Companion::_instance.isInitialized
        fun register(instance: MsgIdStorage) {
            Int.MAX_VALUE
            if (registered) throw IllegalStateException("MsgIdStorage is already registered")
            _instance = instance
        }
    }
}
class MsgId(
    val contactType: String,
    val contactId: String,
    val contactSubId: String,
    val messageId: String
)

val MsgId.contact: Contact
    get() = contact {
        scene = when (contactType) {
            "group" -> Scene.GROUP
            "friend" -> Scene.FRIEND
            "guild" -> Scene.GUILD
            "temp" -> Scene.STRANGER_FROM_GROUP
            "nearby" -> Scene.NEARBY
            "stranger" -> Scene.STRANGER
            else -> Scene.UNRECOGNIZED
        }
        peer = contactId
        if (contactSubId.isNotEmpty()) subPeer = contactSubId
    }

fun newMsgId(contact: Contact, messageId: String): MsgId = MsgId(
    contactType = when (contact.scene) {
        Scene.GROUP -> "group"
        Scene.FRIEND -> "friend"
        Scene.GUILD -> "guild"
        Scene.STRANGER_FROM_GROUP -> "temp"
        Scene.NEARBY -> "nearby"
        Scene.STRANGER -> "stranger"
        else -> "unknown"
    },
    contactId = contact.peer,
    contactSubId = contact.subPeer,
    messageId = messageId
)
