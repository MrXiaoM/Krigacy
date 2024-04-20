package top.mrxiaom.kritor.adapter.onebot.connection

import io.grpc.Channel

class ChannelWrapper private constructor(
    internal val channel: Channel
) {
    companion object {
        fun wrap(channel: Channel): ChannelWrapper {
            return ChannelWrapper(channel)
        }
    }
}
