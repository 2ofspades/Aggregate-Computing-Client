package server

import adapters.protelis.ProtelisAdapter
import adapters.protelis.ProtelisContext
import devices.implementations.VirtualDevice
import devices.interfaces.Device

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.protelis.vm.NetworkManager
import server.network.communication.local.LocalNetworkController
import server.network.communication.local.LocalServerCommunication
import server.network.ServerSupport
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ProtelisLocalTest {

    private lateinit var localNetworkController: LocalNetworkController
    private val inetAddress = InetAddress.getLoopbackAddress()
    private val port = 2001

    @BeforeAll
    fun connectSupportToLocalNetwork(){
        localNetworkController = LocalNetworkController(ServerSupport, inetAddress, port)
        localNetworkController.setCommunicationForServer(ServerSupport)
        ServerSupport.startServer()
    }

    @AfterAll
    fun stopServer(){
        ServerSupport.stopServer()
    }

    @Test
    fun test(){
        val adapterTest = ProtelisAdapterTest()
        executeProgram(4)
    }

    internal class ProtelisAdapterTest {

        class HelloContext(private val device: Device, networkManager: NetworkManager) : ProtelisContext(device, networkManager) {
            override fun instance(): ProtelisContext =
                HelloContext(device, networkManager)

            fun announce(something: String) = device.showResult("$device - $something")
            fun getName() = device.toString()
            fun test() = device.showResult("HSHR")
            fun test2(): String = "rest"
        }

        fun readFromRaw() : String {
            val bufferedReader = File("D:\\Matteo\\Documenti\\GitHub repo\\Aggregate-Computing-Client\\app\\src\\main\\res\\raw\\hello.pt").bufferedReader()
            return bufferedReader.readText()
        }


        init {

            Execution.adapter = { ProtelisAdapter(it, readFromRaw() ,::HelloContext,
                ServerSupport
            ) }

            val numDevice = 2

            val names = listOf("Mario", "Luca")

            repeat(numDevice) { n ->
                val device = ServerSupport.deviceManager.addHostedDevice { uuid, id ->  VirtualDevice(id, names[n]) } as VirtualDevice
                if (n == 0)
                    (device.adapter as ProtelisAdapter).context.executionEnvironment.put("leader", true)
                if (n==1)
                    (device.adapter as ProtelisAdapter).context.executionEnvironment.put("luc", true)
            }

            Execution.adapter = { ProtelisAdapter(it, readFromRaw() ,::HelloContext,
                ServerSupport
            )}




        }
    }

    fun executeProgram(times: Int) {
        repeat(times) {
            ServerSupport.execute()
            Thread.sleep(5000)
        }
    }


}