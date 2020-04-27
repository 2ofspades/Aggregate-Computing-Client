package server.src.network

import communication.Message
import communication.MessageType
import devices.interfaces.Device
import server.SUPPORT_ID
import server.src.devices.PeerDevice
import java.lang.NullPointerException
import java.util.*

object serverSupport : PeerDevice(UUID.randomUUID(), SUPPORT_ID, "serverSupport") {

    val deviceManager = PeerDeviceManager()

    @Suppress("NAME_SHADOWING")
    override fun tell(message: Message) {

        when (message.type) {
            MessageType.Join -> {
                val networkInformation = message.content as NetworkInformation
                val message = networkInformation.getContent() as Message
                if (networkInformation.isClient()) {
                    val device = deviceManager.addHostedDevice { uuid, id ->
                        PeerDevice(
                            uuid,
                            id
                        )
                    } as PeerDevice
                    networkInformation.setPhysicalDevice(device)
                    device.tell(Message(this.id, MessageType.ID, device.id))
                } else {
                    val device =
                        deviceManager.addRemoteDevice(message.content as UUID) { uuid, id ->
                            PeerDevice(
                                uuid,
                                id
                            )
                        } as PeerDevice
                    networkInformation.setPhysicalDevice(device)
                    // uncomment this if you want reply with a Join (bi-directional)
                    //device.tell(Message(this.id, MessageType.Join, this.uuid))
                }

            }
            MessageType.SendToNeighbours -> {
                deviceManager.getHostedDevices().forEach {
                    if (it.id != message.senderUid)
                        synchronized(it) { it.tell(message.content as Message) }
                }
                if (message.senderUid == -1)
                    return
                val device = deviceManager.getDeviceByID(message.senderUid) as? PeerDevice
                // if isn't a peer device then is a local peer, if not can be a local peer or a server
                if (device == null || device.isClient()) {
                    //the message came from a hosted client
                    val list = deviceManager.getRemoteDevices()
                    list.forEach {
                        try {
                            synchronized(it) {
                                it.tell(
                                    Message(
                                        this.id,
                                        MessageType.SendToNeighbours,
                                        message.content
                                    )
                                )
                            }
                        } catch (exc: NullPointerException) {
                            exc.printStackTrace()
                        }
                    }
                }
            }
            MessageType.GoLightWeight,
            MessageType.LeaveLightWeight -> {
                deviceManager.getHostedDevices().single { it.id == message.senderUid }
                    .tell(Message(message.senderUid, message.type, false))
            }
            else -> {
            }
        }

    }

    override fun execute() {
        deviceManager.getHostedDevices().forEach { synchronized(it) { it.execute() } }
    }

    fun replaceHosted(replace: Device, with: Device) {
        with.status = replace.status
        deviceManager.removeDevice(replace)
        deviceManager.addHostedDevice(with)
    }

    fun reset() {
        deviceManager.reset()
    }

}