package server.network

import communication.Message
import server.devices.PeerDevice

interface CommunicationControllerInterface {
    fun getRemoteServer(onServerReady: (PeerDevice) -> Unit, onMessage: (Message) -> Unit)
    fun startOfferServer()
    fun stopOfferServer()
    fun setCommunicationForServer(server: PeerDevice)
}