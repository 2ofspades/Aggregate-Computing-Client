package server.network.communication.local

import communication.Message
import server.devices.PeerDevice
import server.network.NetworkInformation
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

    lateinit var server: PeerDevice

    override fun isClient(): Boolean {
        return fromClient
    }

    override fun getContent(): Serializable? {
        return message
    }

    override fun setPhysicalDevice(peer: PeerDevice) {
        val localCommunication = LocalClientCommunication(peer, this)
        localCommunication.server = this.server
        peer.setPhysicalDevice(localCommunication)
    }
}