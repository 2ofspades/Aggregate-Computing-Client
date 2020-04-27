package server.src.network

import server.src.devices.PeerDevice
import java.io.Serializable

interface NetworkInformation : Serializable {

    fun isClient(): Boolean
    fun getContent(): Serializable?
    fun setPhysicalDevice(peer: PeerDevice)
}