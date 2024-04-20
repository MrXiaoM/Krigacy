package top.mrxiaom.kritor.adapter.onebot.standalone

import com.google.gson.annotations.SerializedName

class KrigacyConfig {
    @SerializedName("kritor")
    var kritor: Kritor = Kritor()
    @SerializedName("onebot")
    var onebot: Onebot = Onebot()
    class Kritor {
        @SerializedName("address")
        var address: String = "127.0.0.1"

        @SerializedName("port")
        var port: Int = 5700

        @SerializedName("account")
        var account: String = "114514"

        @SerializedName("ticket")
        var ticket: String = ""
    }

    class Onebot {
        @SerializedName("type")
        var type: OnebotType = OnebotType.WS_POSITIVE

        @SerializedName("port")
        var port: Int = 5300

        @SerializedName("token")
        var token: String = ""
    }
}

enum class OnebotType {
    WS_POSITIVE, WS_REVERSED
}
