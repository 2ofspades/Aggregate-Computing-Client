package server.network

import communication.Message
import server.devices.PeerDevice
import java.util.*


class NetworkController(getServerSupport: () -> ServerSupport?) {

    private val serverSupport: ServerSupport? = getServerSupport()

    private val listOfController =
        Collections.synchronizedList(mutableListOf<CommunicationControllerInterface>())

    fun startServer() {
        if (serverSupport != null) {
            listOfController.iterator().forEach {
                it.setCommunicationForServer(serverSupport)
            }
            serverSupport.startServer()
        }
    }

    fun getMainServer(serverReady: (PeerDevice) -> Unit, onMessage: (Message) -> Unit) {
        listOfController.iterator().forEach {
            it.getRemoteServer(serverReady, onMessage)
        }
    }


    fun startOfferServer() {
        listOfController.iterator().forEach {
            it.startOfferServer()
        }
    }

    fun stopOfferServer() {
        listOfController.iterator().forEach {
            it.stopOfferServer()
        }
    }

    fun stopAllService() {
        stopOfferServer()
        serverSupport?.stopServer()
    }

    fun addController(controllerInterface: CommunicationControllerInterface) {
        listOfController.add(controllerInterface)
    }


}