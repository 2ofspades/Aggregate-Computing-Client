package server.devices

import communication.Message
import communication.MessageType
import devices.interfaces.AbstractDevice
import server.network.communication.NetworkCommunication
import server.network.serverSupport
import java.util.*


open class PeerDevice(
    val uuid: UUID?,
    id: Int,
    name: String = "",
    var networkDevice: NetworkCommunication? = null
) : AbstractDevice(id, name, ::println) {

    fun setPhysicalDevice(networkCommunication: NetworkCommunication) {
        networkDevice = networkCommunication
    }

    override fun execute() {
        networkDevice?.send(Message(id, MessageType.Execute))
    }

    override fun tell(message: Message) {
        super.tell(message)
        when (message.type) {
            MessageType.Execute -> {
            }
            MessageType.GoLightWeight -> {
                if (message.content as Boolean)
                    networkDevice?.send(message)
                else
                    goLightWeight()
            }
            else -> networkDevice?.send(message)
        }
    }

    private fun goLightWeight() {
        serverSupport.replaceHosted(
            this, LocalExecutionPeerDevice(
                id, name
            )
        )
    }

    fun isClient(): Boolean {
        if (networkDevice != null) {
            return networkDevice!!.isConnectedToClient()
        }
        return false
    }
}