package server.src.network.communication.local

import communication.Message
import server.src.devices.PeerDevice
import server.src.network.NetworkInformation
import server.src.network.serverSupport
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.Socket

class LocalNetworkInformation(
    val socket: Socket,
    val inputStream: ObjectInputStream,
    val outputStream: ObjectOutputStream,
    private val fromClient: Boolean,
    private val message: Message
) : NetworkInformation {

    //only for testing
    var server: PeerDevice = serverSupport

    override fun isClient(): Boolean {
        return fromClient
    }

    override fun getContent(): Serializable? {
        return message
    }

    override fun setPhysicalDevice(peer: PeerDevice) {
        val physicalLocalCommunication = LocalClientCommunication(peer, this)
        physicalLocalCommunication.server = this.server
        //val physicalLocalCommunication = LocalCommunication(peer, InetSocketAddress(address, port))
        peer.setPhysicalDevice(physicalLocalCommunication)
    }
}