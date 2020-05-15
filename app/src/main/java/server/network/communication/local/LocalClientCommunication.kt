package server.network.communication.local

import communication.Message
import server.network.communication.NetworkCommunication
import server.devices.PeerDevice
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class LocalClientCommunication(device: PeerDevice, private val address: InetSocketAddress) :
    NetworkCommunication(device) {

    private lateinit var connection: Socket
    private lateinit var inputStream: ObjectInputStream
    private lateinit var outputStream: ObjectOutputStream
    private var isClient = false
    private val semaphoreObjectStream: Semaphore = Semaphore(0)
    private val executors = Executors.newFixedThreadPool(3)

    lateinit var server: PeerDevice

    // is called by the Network Information (the socket is already open)
    constructor(device: PeerDevice, localNetworkInformation: LocalNetworkInformation) :
            this(
                device,
                InetSocketAddress(
                    localNetworkInformation.socket.inetAddress,
                    localNetworkInformation.socket.port
                )
            ) {
        this.connection = localNetworkInformation.socket
        this.inputStream = localNetworkInformation.inputStream
        this.outputStream = localNetworkInformation.outputStream
        semaphoreObjectStream.release()
        this.isClient = localNetworkInformation.isClient()
        thread {
            while (connection.isConnected) {
                try {
                    val message = extractMessage(connection)
                    server.tell(message)
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }
        }
    }

    override fun connect(onReceive: (Socket) -> Unit) {
        connection = Socket(address.address, address.port)
        thread {
            outputStream = ObjectOutputStream(connection.getOutputStream())
            semaphoreObjectStream.release()
            inputStream = ObjectInputStream(connection.getInputStream()) // can be blocking
            onReceive(connection)
        }
    }

    override fun isConnectedToClient(): Boolean {
        return isClient
    }

    override fun extractMessage(received: Socket): Message {
        return inputStream.readObject() as Message
    }

    override fun send(message: Message) {
        executors.submit {
            try {
                semaphoreObjectStream.acquire()
                outputStream.writeObject(message)
                //outputStream.flush()
                semaphoreObjectStream.release()
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }
    }

    override fun serverCallback(connection: Socket) {
        while (connection.isConnected) {
            val message = extractMessage(connection)
            server.tell(message)
        }

    }

    override fun startServer(onReceive: (Socket) -> Unit) {
        throw Exception("A client must not start a server")
    }

    override fun stopServer() {
        throw Exception("A client must not stop a server")
    }
}