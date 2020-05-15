package server.network.communication.local

import communication.Message
import communication.MessageType
import server.devices.PeerDevice
import server.network.CommunicationControllerInterface
import server.network.ServerSupport
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread



class LocalNetworkController(
    val server: PeerDevice? = ServerSupport,
    private val serverAddress: InetAddress,
    private val serverPort: Int
) :
    CommunicationControllerInterface {


    // connect to lookup server socket, instance the serverPeer, subscribe to server
    // and call the server Ready, this is called by the client
    override fun getRemoteServer(
        onServerReady: (PeerDevice) -> Unit,
        onMessage: (Message) -> Unit
    ) {
        val serverPeer = PeerDevice(null, -1, "server")
        val localCommunication = LocalClientCommunication(
            serverPeer, InetSocketAddress(
                serverAddress, serverPort
            )
        )
        serverPeer.setPhysicalDevice(localCommunication)
        try {
            localCommunication.connect(onReceive = {
                while (it.isConnected) {
                    val message = localCommunication.extractMessage(it)
                    onMessage(message)
                }
            }) // can trow exception
            onServerReady(serverPeer)
        } catch (error: Exception) {
            // cant connect to server, try again or abort
            error.printStackTrace()
        }

    }

    override fun startOfferServer() {
        //need to implement a protocol for discovering server in the local network, when found the
        //address calls serverFoundAt(address)
        TODO("Not yet implemented")
    }

    override fun stopOfferServer() {
        // stop the protocol above
        TODO("Not yet implemented")
    }

    override fun setCommunicationForServer(server: PeerDevice) {
        server.setPhysicalDevice(
            LocalServerCommunication(
                server,
                InetSocketAddress(serverAddress, serverPort)
            )
        )
    }

    // When the network found a neighbour server to send a Join and wait for message
    // the support doesn't add the peer
    fun serverFoundAt(address: InetAddress, port: Int) {
        if (server != null) {
            thread {
                try {
                    val connection = Socket(address, port)
                    val joinSendMessage = Message(-1, MessageType.Join, ServerSupport.uuid)
                    val outputStream = ObjectOutputStream(connection.getOutputStream())
                    outputStream.writeObject(joinSendMessage)
                    val objectInput = ObjectInputStream(connection.getInputStream())
                    while (connection.isConnected) {
                        val receiveMessage = objectInput.readObject() as Message
                        server.tell(receiveMessage)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


}