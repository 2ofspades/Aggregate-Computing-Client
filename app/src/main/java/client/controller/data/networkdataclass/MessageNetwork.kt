package client.controller.data.networkdataclass

import java.io.Serializable
import java.util.*

class MessageNetwork(
    val uid: UUID, val sender: UUID, val destination: UUID, val date: Date,
    val content: Serializable
) : Serializable {

    override fun equals(other: Any?): Boolean {
        val otherMessage = other as? MessageNetwork
        return if (otherMessage != null)
            uid == otherMessage.uid
        else
            super.equals(other)
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}