package server

import adapters.protelis.ProtelisAdapter
import adapters.protelis.ProtelisContext
import client.controller.data.db.Message
import client.controller.data.db.User
import devices.implementations.VirtualDevice
import devices.interfaces.Device
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.protelis.lang.datatype.impl.ArrayTupleImpl
import org.protelis.vm.NetworkManager
import server.network.ServerSupport
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AwareNetTest {

    @BeforeAll
    fun beforeAll() {

    }

    @AfterAll
    fun afterAll() {

    }

    @Test
    fun test1() {
        val adapterTest = ProtelisAdapterTest()
        executeProgram(3)
    }

    internal class ProtelisAdapterTest {

        class AwareContext(private val device: Device, networkManager: NetworkManager) :
            ProtelisContext(device, networkManager) {
            private val user = User(isMainUser = true, username = "$device", uid = 10)
            private var messageAlreadySent = false
            private var messageAlreadySentTo2 = false
            override fun instance(): ProtelisContext =
                AwareContext(device, networkManager)

            fun announce(something: String) = device.showResult("$device - $something")
            fun getName() = device.toString()

            fun getID(): Int {
                return device.id
            }

            fun userOnline(arrayUserOnline: ArrayTupleImpl) {
                var idFound = "$device userOnline: "
                arrayUserOnline.iterator().forEach {
                    idFound += "id: $it  "
                }
                device.showResult(idFound)
            }

            fun getMyProfile(): Any {
                return user
            }

            fun getProfiles(arrayTupleImpl: ArrayTupleImpl) {
                var profilesFound = "$device profiles: "
                arrayTupleImpl.iterator().forEach {
                    it as ArrayTupleImpl
                    profilesFound += "profile: ${(it.get(1) as User).username} \t"
                }
                device.showResult(profilesFound)
            }

            fun checkDistance(arrayTupleImpl: ArrayTupleImpl) {
                var str = "$device distanceKnown: "
                arrayTupleImpl.iterator().forEach {
                    str += "$it "
                }
                device.showResult(str)
            }

            fun getMessageToSend(): ArrayTupleImpl {
                var e = ArrayTupleImpl()
                if (device.id == 0 && !messageAlreadySent) {
                    val message = Message(
                        userId = 2,
                        content = "Hello from Mario",
                        typeContent = 1,
                        isSentByMainUser = true,
                        date = Date(System.currentTimeMillis()),
                        messageBoxId = -1
                    )
                    val dest = 1
                    val messageid = 5
                    var messageArray = ArrayTupleImpl()
                    messageArray = messageArray.append(dest) as ArrayTupleImpl
                    messageArray = messageArray.append(messageid) as ArrayTupleImpl
                    messageArray = messageArray.append(message) as ArrayTupleImpl
                    e = e.append(messageArray) as ArrayTupleImpl
                    messageAlreadySent = true
                }
                if (device.id == 2 && !messageAlreadySentTo2) {
                    val message = Message(
                        userId = 2,
                        content = "Hello From Arturito",
                        typeContent = 1,
                        isSentByMainUser = true,
                        date = Date(System.currentTimeMillis()),
                        messageBoxId = -1
                    )
                    val dest = 3
                    val messageid = 5
                    var messageArray = ArrayTupleImpl()
                    messageArray = messageArray.append(dest) as ArrayTupleImpl
                    messageArray = messageArray.append(messageid) as ArrayTupleImpl
                    messageArray = messageArray.append(message) as ArrayTupleImpl
                    e = e.append(messageArray) as ArrayTupleImpl
                    messageAlreadySentTo2 = true
                }
                return e
            }

            fun returnMessageToDevice(messageToReturn: ArrayTupleImpl) {
                var flag = false
                var str = "$device: message arrived"
                messageToReturn.iterator().forEach {
                    it as ArrayTupleImpl
                    val message = it.get(0) as ArrayTupleImpl
                    if (message.get(0) == device.id) {
                        flag = true
                        str += message.get(2).toString()
                        str += " - "
                    }
                }
                if (messageToReturn.size() > 0 && flag)
                    device.showResult(str)
            }
        }

        fun readFromRaw(): String {
            val bufferedReader =
                File("D:\\Matteo\\Documenti\\GitHub repo\\Aggregate-Computing-Client\\app\\src\\main\\res\\raw\\awarenet.pt").bufferedReader()
            return bufferedReader.readText()
        }


        init {

            Execution.adapter = {
                ProtelisAdapter(
                    it, readFromRaw(), ::AwareContext,
                    ServerSupport
                )
            }

            val numDevice = 3

            val names = listOf("Mario", "Luca", "Arturito")

            repeat(numDevice) { n ->
                val device = ServerSupport.deviceManager.addHostedDevice { uuid, id ->
                    VirtualDevice(
                        id,
                        names[n]
                    )
                } as VirtualDevice
                if (n == 0)
                    (device.adapter as ProtelisAdapter).context.executionEnvironment.put(
                        "leader",
                        true
                    )
                if (n == 1)
                    (device.adapter as ProtelisAdapter).context.executionEnvironment.put(
                        "luc",
                        true
                    )
            }

            Execution.adapter = {
                ProtelisAdapter(
                    it, readFromRaw(), ::AwareContext,
                    ServerSupport
                )
            }


        }
    }

    fun executeProgram(times: Int) {
        repeat(times) {
            println("Starting ${it + 1} iteration ***")
            ServerSupport.execute()
            println("\n")
            Thread.sleep(2000)
        }
    }
}