package server.network

import communication.Message
import server.devices.PeerDevice
import server.network.communication.NetworkCommunication

interface CommunicationControllerInterface {
    fun getRemoteServer(onServerReady: (PeerDevice) -> Unit, onMessage: (Message) -> Unit)
    fun startOfferServer()
    fun stopOfferServer()
    fun setCommunicationForServer(server: PeerDevice)
}