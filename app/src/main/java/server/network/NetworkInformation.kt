package server.network

import server.devices.PeerDevice
import java.io.Serializable

interface NetworkInformation : Serializable {

    fun isClient(): Boolean
    fun getContent(): Serializable?
    fun setPhysicalDevice(peer: PeerDevice)
}