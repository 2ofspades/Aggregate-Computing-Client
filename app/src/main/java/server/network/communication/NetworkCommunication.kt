package server.network.communication

import communication.Communication
import server.devices.PeerDevice
import java.net.Socket

abstract class NetworkCommunication(override val device: PeerDevice) : Communication<Socket> {

    // chain of responsibility pattern
    private var nextCommunication: NetworkCommunication? = null

    fun addCommunication(networkCommunication: NetworkCommunication) {
        if (nextCommunication == null) {
            nextCommunication = networkCommunication
        } else {
            nextCommunication!!.addCommunication(networkCommunication)
        }
    }

    abstract fun connect(onReceive: (Socket) -> Unit = ::serverCallback)

    abstract fun isConnectedToClient(): Boolean
}