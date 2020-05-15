package server.network

import communication.Message
import server.network.communication.local.LocalNetworkController
import server.devices.PeerDevice
import server.network.communication.local.LocalServerCommunication
import server.network.communication.local.SERVER_ADDRESS
import server.network.communication.local.SERVER_PORT
import java.net.InetAddress
import java.net.InetSocketAddress

class NetworkController(getServerSupport: () -> serverSupport?) {

    private val serverSupport: serverSupport? = getServerSupport()

    private val listOfController: MutableList<CommunicationControllerInterface> = mutableListOf()

    // only for test
    private lateinit var localNetworkController: LocalNetworkController

    // only for test
    fun startServer(serverAddress: InetAddress = SERVER_ADDRESS, serverPort: Int = SERVER_PORT) {
        localNetworkController = LocalNetworkController(serverSupport, serverAddress, serverPort)
        serverSupport!!.networkDevice = LocalServerCommunication(
            serverSupport,
            InetSocketAddress(
                localNetworkController.serverAddress,
                localNetworkController.serverPort
            )
        )
        serverSupport.networkDevice!!.startServer()
    }



    fun getMainServer(serverReady: (PeerDevice) -> Unit, onMessage: (Message) -> Unit) {
        if (serverSupport != null) {
            // test
            localNetworkController.getRemoteServer(serverReady, onMessage)
        } else {
            // this client cannot host a server, need to search one
            localNetworkController.getRemoteServer(serverReady, onMessage)
        }
    }

    //Test
    fun searchServer(address: InetAddress, port: Int) {
        localNetworkController.offerServiceToNeighbourServer(address, port)
    }

    fun addController(controllerInterface: CommunicationControllerInterface) {
        synchronized(listOfController) {
            listOfController += controllerInterface
        }
    }


}