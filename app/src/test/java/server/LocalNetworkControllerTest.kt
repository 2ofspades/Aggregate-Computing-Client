package server

import communication.Message
import communication.MessageType
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*
import server.network.PeerDeviceManager
import server.devices.PeerDevice
import server.network.NetworkInformation
import server.network.communication.local.*
import server.network.ServerSupport
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LocalNetworkControllerTest {

    private lateinit var localNetworkController: LocalNetworkController
    private val inetAddress = InetAddress.getLoopbackAddress()
    private val port = 2001

    @BeforeAll
    fun setUp() {
        localNetworkController = LocalNetworkController(ServerSupport, inetAddress, port)
        localNetworkController.setCommunicationForServer(ServerSupport)
        ServerSupport.startServer()
    }

    @AfterAll
    fun stopServer() {
        ServerSupport.stopServer()
    }

    @Test
    fun getRemoteServer() {
        val semaphore = Semaphore(-1)
        localNetworkController.getRemoteServer({
            it.tell(Message(-2, MessageType.Join))
            semaphore.release()
        }) {
            assertTrue(it.type == MessageType.ID)
            semaphore.release()
        }
        assertTrue(semaphore.tryAcquire(8, TimeUnit.SECONDS))

    }

    @Test
    fun connectToRemoteServer(){
        val serverAddressDummy = InetSocketAddress(inetAddress, 2005)
        val remoteServer = ServerDummy
        val remotePhysical =
            LocalServerCommunication(
                remoteServer,
                serverAddressDummy
            )
        remoteServer.setPhysicalDevice(remotePhysical)
        remotePhysical.startServer()

        assertTrue(ServerDummy.deviceManager.getRemoteDevices().isEmpty())
        localNetworkController!!.serverFoundAt(serverAddressDummy.address, serverAddressDummy.port)

        ServerDummy.joinReceivedSemaphore.acquire()
        assertTrue(ServerDummy.deviceManager.getRemoteDevices().isNotEmpty())
        val remoteDevice = ServerDummy.deviceManager.getRemoteDevices().first() as PeerDevice
        assertTrue(remoteDevice.uuid == ServerSupport.uuid)
        remoteServer.stopServer()
    }

    object ServerDummy: PeerDevice(UUID.randomUUID(), -1, "serverDummy") {
        val deviceManager = PeerDeviceManager()
        var joinReceivedSemaphore = Semaphore(0)
        override fun tell(message: Message) {
            when (message.type){
                MessageType.Join -> {
                    val networkInformation = message.content as NetworkInformation
                    val message = networkInformation.getContent() as Message
                    if (networkInformation.isClient()){
                        val device = deviceManager.addHostedDevice { uuid, id -> PeerDevice(uuid, id) } as PeerDevice
                        device.tell(Message(this.id, MessageType.ID, device.id))
                        networkInformation.setPhysicalDevice(device)
                    } else {
                        val device = deviceManager.addRemoteDevice(message.content as UUID) { uuid, id -> PeerDevice(uuid, id) } as PeerDevice
                        networkInformation.setPhysicalDevice(device)
                        //device.tell(Message(this.id, MessageType.Join, this.uuid))
                    }
                    joinReceivedSemaphore.release()
                }
                else -> {}
            }
        }

        fun stopServer(){networkDevice?.stopServer()}

    }
}