package top.mrxiaom.kritor.adapter.onebot.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun buildJsonObject(block: MutableMap<String, Any>.() -> Unit): JsonObject {
    return buildMap(block).toJsonObject()
}
fun MutableMap<String, Any>.putJsonObject(key: String, block: MutableMap<String, Any>.() -> Unit) {
    put(key, buildJsonObject(block))
}
fun MutableList<Any>.addJsonObject(block: MutableMap<String, Any>.() -> Unit) {
    add(buildJsonObject(block))
}
fun buildJsonArray(block: MutableList<Any>.() -> Unit): JsonArray {
    return buildList(block).toJsonArray()
}
fun MutableMap<String, Any>.putJsonArray(key: String, block: MutableList<Any>.() -> Unit) {
    put(key, buildJsonArray(block))
}
fun MutableList<Any>.addJsonArray(block: MutableList<Any>.() -> Unit) {
    add(buildJsonArray(block))
}
fun Map<String, Any>.toJsonObject(): JsonObject {
    return JsonObject().apply {
        for ((key, value) in this@toJsonObject) {
            when (value) {
                is JsonElement -> add(key, value)
                is Map<*, *> -> add(key, value.jsonObject())
                is Iterable<*> -> value.toJsonArray()
                is String -> addProperty(key, value)
                is Char -> addProperty(key, value)
                is Number -> addProperty(key, value)
                is Boolean -> addProperty(key, value)
                else -> addProperty(key, value.toString())
            }
        }
    }
}
fun <V> Iterable<V>.toJsonArray(): JsonArray {
    return JsonArray().apply {
        for (value in this@toJsonArray) {
            when (value) {
                is JsonElement -> add(value)
                is Map<*, *> -> add(value.jsonObject())
                is Iterable<*> -> add(value.toJsonArray())
                is String -> add(value)
                is Char -> add(value)
                is Number -> add(value)
                is Boolean -> add(value)
                else -> add(value.toString())
            }
        }
    }
}
private fun Map<*, *>.jsonObject(): JsonObject {
    return cast<Map<String, Any>>().toJsonObject()
}
private inline fun <reified T : Any> Any.cast(): T = this as T
