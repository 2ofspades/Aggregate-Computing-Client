package client.controller

import adapters.protelis.ProtelisAdapter
import adapters.protelis.ProtelisContext
import android.app.Application
import client.controller.protelis.AwareNetFactory
import client.controller.data.DataController
import client.controller.data.db.User
import client.controller.data.networkdataclass.MessageNetwork
import client.controller.data.networkdataclass.ProfileNetwork
import client.controller.protelis.AwareContext
import client.controller.protelis.UserUID
import communication.Message
import communication.MessageType
import communication.implements.LocalNetworkController
import controller.NetworkController
import devices.interfaces.Device
import it.unibo.aggregatecomputingclient.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.protelis.lang.datatype.DeviceUID

import org.protelis.lang.datatype.impl.ArrayTupleImpl
import org.protelis.vm.NetworkManager

import devices.interfaces.EmulatedDevice
import devices.interfaces.RemoteDevice
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
import java.lang.Exception
import java.net.InetAddress
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

val SERVER_ADDRESS: InetAddress = InetAddress.getLoopbackAddress()

class AppController(val app: Application) {


    val dataController = DataController(app)
    private val awareFactory = AwareNetFactory()
    private val networkController = NetworkController.createNetworkController(awareFactory)
    val name = "Pix 3"
    private val protelistFileID: Int = R.raw.awarenet
    lateinit var server: RemoteDevice
    lateinit var device: EmulatedDevice
    val ip: InetAddress = InetAddress.getLoopbackAddress()
    var port: Int? = null

    // Test
    private lateinit var localNetworkController: LocalNetworkController


    companion object {
        @Volatile
        private var appController: AppController? = null

        fun getAppController(app: Application): AppController {
            val controller = appController
            if (controller != null)
                return controller

            synchronized(this) {
                val instanceC = appController
                if (instanceC != null) {
                    return instanceC
                }
                val instanceController = AppController(app)
                appController = instanceController
                return instanceController
            }
        }

        fun getAppController(): AppController? {
            return appController
        }
    }

    fun start(port: Int = 2001) {
        val mainUser = dataController.mainUser
        localNetworkController = LocalNetworkController(networkController.support, serverAddress = SERVER_ADDRESS, serverPort = port)
        networkController.addController(localNetworkController)
        networkController.startServer()
        this.port = port
        val sem = Semaphore(-1)
        thread {
            networkController.getMainServer({
                server = it
                val message = Message(awareFactory.createIdFromUUID(mainUser.uid),
                    MessageType.Join, awareFactory.createIdFromUUID(mainUser.uid))
                server.tell(message)
            }, { message ->
                if (message.type == MessageType.ID) {
                    sem.release()
                    device = awareFactory.createEmulatedDevice(awareFactory.createIdFromUUID(mainUser.uid),
                        name, {ProtelisAdapter(it, readFromRaw(protelistFileID), ::AwareContext, server)})
                    sem.release()
                } else {
                    if (this::device.isInitialized)
                        device.tell(message)
                }
            })
        }
        thread {
            sem.acquire()
            Thread.sleep(3000)
            while (true) {
                synchronized(device){
                    device.execute()
                    System.gc()
                    Thread.sleep(1000)
                }
            }

        }
    }

    fun searchServer(address: InetAddress = InetAddress.getLoopbackAddress(), port: Int) {
        localNetworkController.serverFoundAt(address, port)
    }

    fun stop() {
        GlobalScope.launch {
            dataController.deleteAllData()
            networkController.stopAllService()
        }
        Thread.sleep(2000)
    }

    private fun objectArrived(obj: Serializable) {
        print(obj.toString())
    }

    @Suppress("SameParameterValue")
    private fun readFromRaw(fileID: Int): String {
        val inputStream = app.resources.openRawResource(fileID)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val text = bufferedReader.readText()
        bufferedReader.close()
        return text
    }

    fun userOnline(list: MutableList<UUID>) {
        GlobalScope.launch { dataController.userOnline(list) }
    }

    fun profiles(list: MutableList<ProfileNetwork>) {
        GlobalScope.launch {
            list.iterator().forEach {
                val user = dataController.getUser(it.userUUID)
                if (user == null) {
                    dataController.insert(
                        User(
                            it.userUUID,
                            isMainUser = false,
                            isOnline = true,
                            username = it.username,
                            interest = it.interest
                        )
                    )
                } else {
                    var hasToBeUpdated = false
                    if (user.username != it.username) {
                        user.username = it.username
                        hasToBeUpdated = true
                    }

                    if (user.interest != it.interest) {
                        user.interest = it.interest
                        hasToBeUpdated = true
                    }
                    // add image

                    if (hasToBeUpdated)
                        dataController.updateUser(user)
                }

            }
        }
    }

    fun messageArrived(list: MutableList<MessageNetwork>) {
        GlobalScope.launch {
            list.iterator().forEach {
                val messageBox = dataController.getMessageBox(it.sender)
                val message = dataController.getMessageFromUUID(it.uid)
                if (message == null && messageBox != null) {
                    val sender = it.sender
                    val messageId = it.uid
                    // now we assume is a text
                    val date = it.date
                    val content = it.content as String
                    dataController.insert(
                        client.controller.data.db.Message(
                            messageId, sender,
                            messageBox.uid, date, false, 0, content
                        )
                    )
                }
            }
        }
    }

    fun messageToSend(): List<MessageNetwork> {
        val list = mutableListOf<MessageNetwork>()
        dataController.getMessageToSendFromList().iterator().forEach {
            val messageNetwork =
                MessageNetwork(it.uid, dataController.mainUser.uid, it.userUUID, it.date, it.content)
            list += messageNetwork
        }
        return list
    }
}