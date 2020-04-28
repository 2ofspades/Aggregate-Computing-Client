package server.network.communication.local

import communication.Message
import communication.MessageType
import server.network.communication.NetworkCommunication
import server.devices.PeerDevice
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.*
import kotlin.concurrent.thread

// used by support
class LocalServerCommunication(
    override val device: PeerDevice,
    private val address: InetSocketAddress
) : NetworkCommunication(device) {

    init {
        device.setPhysicalDevice(this)
    }

    lateinit var serverSocket: ServerSocket

    override fun connect(onReceive: (Socket) -> Unit) {
        throw Exception("Server")
    }

    override fun isConnectedToClient(): Boolean {
        return false
    }

    override fun extractMessage(received: Socket): Message {
        throw Exception("ServerCommunication can't extract Message")
    }

    override fun send(message: Message) {
        throw Exception("ServerCommunication can't send Message")
    }

    override fun serverCallback(connection: Socket) {
        val objectOutputStream = ObjectOutputStream(connection.getOutputStream())
        val objectInputStream = ObjectInputStream(connection.getInputStream()) // can be blocking

        // waiting for join
        val joinMessage = objectInputStream.readObject() as Message
        if (joinMessage.type != MessageType.Join) {
            connection.close()
        } else {
            val isClient = joinMessage.content as? UUID == null
            val networkInformation = LocalNetworkInformation(
                connection,
                objectInputStream,
                objectOutputStream,
                isClient,
                joinMessage
            )
            networkInformation.server = this.device
            val newMessage = Message(joinMessage.senderUid, joinMessage.type, networkInformation)
            device.tell(newMessage)
        }

    }

    override fun startServer(onReceive: (Socket) -> Unit) {
        serverSocket = ServerSocket(address.port, 5, address.address)
        thread {
            while (!serverSocket.isClosed) {
                try {
                    val clientSocket = serverSocket.accept()
                    thread {
                        onReceive(clientSocket)
                    }
                } catch (e: SocketException) {
                    // do nothing
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun stopServer() {
        serverSocket.close()
    }
}