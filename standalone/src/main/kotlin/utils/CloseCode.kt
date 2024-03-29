package top.mrxiaom.kritor.adapter.onebot.standalone.utils

/**
 * CloseCode 来自 [Overflow](https://github.com/MrXiaoM/Overflow/blob/main/onebot/src/main/kotlin/cn/evolvefield/onebot/client/connection/ConnectFactory.kt)
 */
enum class CloseCode(val code: Int) {
    NORMAL(1000),
    GOING_AWAY(1001),
    PROTOCOL_ERROR(1002),
    REFUSE(1003),
    NO_CODE(1005),
    ABNORMAL_CLOSE(1006),
    NO_UTF8(1007),
    POLICY_VALIDATION(1008),
    TOO_BIG(1009),
    EXTENSION(1010),
    UNEXPECTED_CONDITION(1011),
    SERVICE_RESTART(1012),
    TRY_AGAIN_LATER(1013),
    BAD_GATEWAY(1014),
    TLS_ERROR(1015),
    NEVER_CONNECTED(-1),
    BUGGY_CLOSE(-2),
    FLASH_POLICY(-3);

    override fun toString(): String {
        return name.uppercase()
    }

    companion object {
        fun valueOf(code: Int): CloseCode? {
            return entries.firstOrNull { it.code == code }
        }
    }
}
