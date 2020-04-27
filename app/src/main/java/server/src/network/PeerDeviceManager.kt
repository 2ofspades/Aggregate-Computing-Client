package server.src.network

import devices.interfaces.Device
import java.util.*

class PeerDeviceManager {

    private val hostedDevices: MutableList<Device> = mutableListOf()
    private val remoteDevices: MutableList<Device> = mutableListOf()
    private var intId = 0

    private fun supportUUID(): UUID {
        return serverSupport.uuid!!
    }

    private fun generateIntID(): Int {
        // is not thread safe!
        var int: Int?
        synchronized(intId) {
            int = intId++
        }
        return int!!
    }

    fun reset() {
        intId = 0
        hostedDevices.clear()
        remoteDevices.clear()
    }

    private fun createDevice(
        deviceUUID: UUID = supportUUID(),
        device: (UUID, Int) -> Device
    ): Device = device(deviceUUID, generateIntID())

    fun addHostedDevice(device: (UUID, Int) -> Device): Device {
        val created = createDevice(device = device)
        hostedDevices += created
        return created
    }

    fun addRemoteDevice(remoteUUID: UUID, device: (UUID, Int) -> Device): Device {
        val created = createDevice(remoteUUID, device)
        synchronized(remoteDevices) {
            remoteDevices += created
        }
        return created
    }

    fun getHostedDevices(): List<Device> = hostedDevices.toList()

    fun getRemoteDevices(): List<Device> = remoteDevices.toList()

    fun getDeviceByID(id: Int): Device? {
        val device = hostedDevices.firstOrNull { it.id == id }
        if (device != null)
            return device
        return remoteDevices.firstOrNull { it.id == id }
    }

    fun removeDevice(device: Device) {
        hostedDevices.removeIf { it.id == device.id }
        remoteDevices.removeIf { it.id == device.id }
    }

    fun addHostedDevice(device: Device) {
        hostedDevices += device
    }
}