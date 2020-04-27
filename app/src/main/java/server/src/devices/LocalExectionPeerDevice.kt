package server.src.devices

import adapters.Adapter
import communication.Message
import communication.MessageType
import devices.interfaces.EmulatedDevice
import server.Execution
import server.src.network.communication.NetworkCommunication
import server.src.network.serverSupport
import java.io.Serializable
import java.util.*

class LocalExecutionPeerDevice(
    id: Int,
    name: String,
    adapterBuilder: (EmulatedDevice) -> Adapter = Execution.adapter
) :
    EmulatedDevice(id, name, adapterBuilder, onResult = ::println) {

    var networkDevice: NetworkCommunication? = null

    fun setPhysicalDevice(networkCommunication: NetworkCommunication) {
        networkDevice = networkCommunication
    }

    override fun showResult(result: Serializable) {
        networkDevice?.send(Message(id, MessageType.Show, result))
    }

    override fun tell(message: Message) {
        super.tell(message)
        when (message.type) {
            MessageType.Result, MessageType.Show -> networkDevice?.send(message)
            MessageType.LeaveLightWeight -> goFullWeight()
            else -> {
            }
        }
    }

    private fun goFullWeight() {
        serverSupport.replaceHosted(
            this, PeerDevice(
                serverSupport.uuid, id, name, networkDevice
            )
        )
    }
}