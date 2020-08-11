package server

import adapters.protelis.ProtelisAdapter
import adapters.protelis.ProtelisContext
import communication.Message
import communication.MessageType
import devices.implementations.VirtualDevice
import devices.interfaces.Device

import org.junit.jupiter.api.*
import org.protelis.vm.NetworkManager
import server.network.PeerDeviceManager
import server.network.communication.local.*
import server.devices.PeerDevice
import server.network.NetworkInformation
import server.network.ServerSupport
import java.io.File
import java.lang.NullPointerException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProtelisRemoteTest {

    lateinit var localNetworkController: LocalNetworkController
    private val address = InetAddress.getLoopbackAddress()
    private lateinit var adapterTest: ProtelisAdapterTest
    private lateinit var serverDummy1: ServerDummyDevice
    private lateinit var serverDummy2: ServerDummyDevice
    private lateinit var serverDummy1Controller: LocalNetworkController
    private lateinit var serverDummy2Controller: LocalNetworkController

    private val serverDummy1Address = InetSocketAddress(address, 2004)
    private val serverDummy2Address = InetSocketAddress(address, 2006)

    @BeforeEach
    fun startServer(){
        serverDummy1 = ServerDummyDevice("ServerDummy1")
        serverDummy2 = ServerDummyDevice("ServerDummy2")
        adapterTest = ProtelisAdapterTest(serverDummy1, serverDummy2)
        localNetworkController = LocalNetworkController(ServerSupport, address, 2001)
        serverDummy1Controller = LocalNetworkController(serverDummy1, address, 2004)
        serverDummy2Controller = LocalNetworkController(serverDummy2, address, 2006)

        // set Server Communication
        localNetworkController.setCommunicationForServer(ServerSupport)
        serverDummy1Controller.setCommunicationForServer(serverDummy1)
        serverDummy2Controller.setCommunicationForServer(serverDummy2)

        // start server
        ServerSupport.startServer()
        serverDummy1.startServer()
        serverDummy2.startServer()
    }

    @AfterEach
    fun stopServer(){
        ServerSupport.stopServer()
        serverDummy1.stopServer()
        serverDummy2.stopServer()
        ServerSupport.reset()
    }

    @Test
    fun testOneDummy1(){
        connectMainToDummy1()
        Thread.sleep(3000)
        executeProgram(4)
    }



    @Test
    fun testBothDummy(){
        connectEveryOne()
        Thread.sleep(3000)
        executeProgram(4)
    }


    @Test
    fun testTriangleDummy(){
        connectMainToDummy1()
        connectDummy1ToDummy2()
        Thread.sleep(3000)
        executeProgram(4)
    }

    // connect Main with Dummy1
    private fun connectMainToDummy1(){
        localNetworkController.serverFoundAt(serverDummy1Address.address, serverDummy1Address.port)
        serverDummy1Controller.serverFoundAt(address, 2001)
    }

    private fun connectDummy1ToDummy2(){
        serverDummy2Controller.serverFoundAt(address, serverDummy1Address.port)
        serverDummy1Controller.serverFoundAt(address, serverDummy2Address.port)
    }

    private fun connectEveryOne(){
        connectMainToDummy1()
        connectDummy1ToDummy2()

        //connect Main with Dummy2
        localNetworkController.serverFoundAt(address, serverDummy2Address.port)
        serverDummy2Controller.serverFoundAt(address, 2001)
    }

    fun executeProgram(times: Int) {
        val executeThread = Thread(Runnable {
            repeat(times) {
                ServerSupport.execute()
                serverDummy1.execute()
                serverDummy2.execute()
                Thread.sleep(5000)
            }
        })
        executeThread.start()
        executeThread.join()
    }

    private class ProtelisAdapterTest(private val serverDummy1: ServerDummyDevice, private val serverDummy2: ServerDummyDevice) {

        class HelloContext(private val device: Device, networkManager: NetworkManager) : ProtelisContext(device, networkManager) {
            override fun instance(): ProtelisContext =
                HelloContext(device, networkManager)

            fun announce(something: String) = device.showResult("$device - $something")
            fun getName() = device.toString()
        }

        fun readFromRaw() : String {
            val bufferedReader = File("D:\\Matteo\\Documenti\\GitHub repo\\Aggregate-Computing-Client\\app\\src\\main\\res\\raw\\hello.pt").bufferedReader()
            return bufferedReader.readText()
        }


        init {
            val namesSupport = listOf("Mario")
            val namesDummy1 = listOf("Dummy1")
            val namesDummy2 = listOf("Dummy2")

            repeat(namesSupport.size) { n ->
                val device = ServerSupport.deviceManager.addHostedDevice { uuid, id ->  VirtualDevice(id, namesSupport[n],
                    {ProtelisAdapter(it, readFromRaw(), ::HelloContext,
                        ServerSupport
                    )})} as VirtualDevice
                 if (n == 0)
                    (device.adapter as ProtelisAdapter).context.executionEnvironment.put("leader", true)
            }

            repeat(namesDummy1.size) { n ->
                val device = serverDummy1.deviceManager.addHostedDevice { uuid, id -> VirtualDevice(id+12, namesDummy1[n],
                    {ProtelisAdapter(it, readFromRaw(), ::HelloContext, serverDummy1)}) } as VirtualDevice
                //if (n==0)
                    //(device.adapter as ProtelisAdapter).context.executionEnvironment.put("leader", true)
            }

            repeat(namesDummy2.size) { n ->
                serverDummy2.deviceManager.addHostedDevice { uuid, id -> VirtualDevice(id+22, namesDummy2[n],
                    {ProtelisAdapter(it, readFromRaw(), ::HelloContext, serverDummy2)}) }
            }
        }
    }

    internal class ServerDummyDevice(override val name: String): PeerDevice(UUID.randomUUID(), -1, name) {
        val deviceManager = PeerDeviceManager()
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
                }
                MessageType.SendToNeighbours -> {
                    deviceManager.getHostedDevices().forEach {
                        if (it.id != message.senderUid){
                            synchronized(it){it.tell(message.content as Message)}
                        }
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
                else -> {}
            }
        }
        override fun execute() {
            deviceManager.getHostedDevices().forEach {
                synchronized(it){it.execute()}
            }
        }

        fun startServer(){
            networkDevice?.startServer()
        }

        fun stopServer() {
            networkDevice?.stopServer()
            networkDevice = null
        }
    }
}