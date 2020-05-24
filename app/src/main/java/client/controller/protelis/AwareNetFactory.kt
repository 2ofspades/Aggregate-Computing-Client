package client.controller.protelis

import adapters.Adapter
import devices.implementations.PeerDevice
import devices.implementations.SupportDevice
import devices.implementations.VirtualDevice
import devices.interfaces.EmulatedDevice
import devices.interfaces.RemoteDevice
import org.protelis.lang.datatype.DeviceUID
import server.interfaces.ServerFactory
import java.io.Serializable
import java.util.*

class AwareNetFactory: ServerFactory {
    override fun createEmulatedDevice(
        uid: DeviceUID,
        name: String,
        adapter: (EmulatedDevice) -> Adapter,
        onResult: (Serializable) -> Any
    ): EmulatedDevice {
        return VirtualDevice(uid, name, adapter, onResult)
    }

    override fun createNewID(): DeviceUID {
        return UserUID()
    }

    override fun createRemoteDevice(uid: DeviceUID): RemoteDevice {
        return PeerDevice(uid)
    }

    override fun createSupport(): SupportDevice? {
        return SupportDevice(createNewID(), name = "server support", factory = this)
    }

    fun createIdFromUUID(uuid: UUID): DeviceUID{
        return UserUID(uuid)
    }
}