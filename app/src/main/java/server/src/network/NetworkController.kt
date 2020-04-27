package server.src.network

import communication.Message
import server.src.network.communication.local.LocalNetworkController
import server.src.devices.PeerDevice

class NetworkController(getServerSupport: () -> serverSupport?) {

    private val serverSupport: serverSupport? = getServerSupport()

    private val listOfController: MutableList<CommunicationControllerInterface> = mutableListOf()

    // only for test
    private val localNetworkController =
        LocalNetworkController()

    fun getMainServer(serverReady: (PeerDevice) -> Unit, onMessage: (Message) -> Unit) {
        if (serverSupport != null) {
            // test
            localNetworkController.getRemoteServer(serverReady, onMessage)
        } else {
            // this client cannot host a server, need to search one
            localNetworkController.getRemoteServer(serverReady, onMessage)
        }
    }

    fun addController(controllerInterface: CommunicationControllerInterface) {
        synchronized(listOfController) {
            listOfController += controllerInterface
        }
    }


}