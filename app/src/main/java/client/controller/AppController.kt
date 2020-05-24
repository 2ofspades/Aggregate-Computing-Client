package client.controller

import adapters.protelis.ProtelisAdapter
import adapters.protelis.ProtelisContext
import android.app.Application
import client.controller.data.protelis.AwareNetFactory
import client.controller.data.DataController
import client.controller.data.db.User
import client.controller.data.networkdataclass.MessageNetwork
import client.controller.data.networkdataclass.ProfileNetwork
import client.controller.data.protelis.UserUID
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
    val name = "Pix 1"
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

    class AwareContext(private val device: Device, networkManager: NetworkManager) :
        ProtelisContext(device, networkManager) {

        private val user = getAppController()!!.dataController.mainUser
        override fun instance(): ProtelisContext =
            AwareContext(device, networkManager)

        //test
        private val profileAlreadyRead = mutableSetOf<UUID>()
        private val messageAlreadyRead = mutableSetOf<UUID>()
        private val iteration = Semaphore(1)

        override fun getDeviceUID(): DeviceUID {
            return device.id
        }

        fun announce(something: String) = device.showResult("$device - $something")
        fun getName() = device.toString()

        fun testMessage(arrayTupleImpl: ArrayTupleImpl){
            val size = arrayTupleImpl.size()
        }

        fun userOnline(arrayUserOnline: ArrayTupleImpl) {
            val listOfIdUser = mutableListOf<UUID>()
            try {
                arrayUserOnline.iterator().forEach {
                    if (it as DeviceUID != deviceUID){
                        if (it is UserUID)
                            listOfIdUser += it.uuid
                    }
                }
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

            if (listOfIdUser.isNotEmpty())
                getAppController()?.userOnline(listOfIdUser)
        }

        fun getMyProfile(): Any {
            iteration.acquire()
            val myUserUID = device.id as UserUID
            return ProfileNetwork(myUserUID.uuid, user.username, user.interest, null)
        }

        fun getProfiles(arrayTupleImpl: ArrayTupleImpl) {
            val profiles = mutableListOf<ProfileNetwork>()
            arrayTupleImpl.iterator().forEach {
                try {
                    val user = it as ProfileNetwork
                    //val user = it.get(0) as ProfileNetwork
                    if (!profileAlreadyRead.contains(user.userUUID)) {
                        profileAlreadyRead.add(user.userUUID)
                        profiles += user
                    }
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }

            }
            if (profiles.isNotEmpty())
                getAppController()?.profiles(profiles)
        }

        // only for testing
        fun checkDistance(arrayTupleImpl: ArrayTupleImpl) {
            var str = "$device distanceKnown: "
            arrayTupleImpl.iterator().forEach {
                str += "$it "
            }
            device.showResult(str)
        }

        fun getMessageToSend(): ArrayTupleImpl {
            var arrayTupleImpl = ArrayTupleImpl()

            // can be blocking
            val messageToSend = getAppController()?.messageToSend()
            messageToSend?.iterator()?.forEach {
                var arrayMessage = ArrayTupleImpl()
                val destination = UserUID(it.destination)
                val messageId = it.uid
                arrayMessage = arrayMessage.append(destination) as ArrayTupleImpl
                arrayMessage = arrayMessage.append(messageId) as ArrayTupleImpl
                arrayMessage = arrayMessage.append(it) as ArrayTupleImpl
                arrayTupleImpl = arrayTupleImpl.append(arrayMessage) as ArrayTupleImpl
            }
            return arrayTupleImpl
        }

        fun returnMessageToDevice(messageToReturn: ArrayTupleImpl) {
            val listOfMessage = mutableListOf<MessageNetwork>()

            messageToReturn.iterator().forEach {
                try {
                    val myUserUID = device.id as UserUID
                    it as ArrayTupleImpl
                    val message = it.get(0) as ArrayTupleImpl
                    if (message.get(0) == myUserUID) {
                        val messageNetwork = message.get(2) as MessageNetwork
                        if (!messageAlreadyRead.contains(messageNetwork.uid)) {
                            messageAlreadyRead.add(messageNetwork.uid)
                            listOfMessage += messageNetwork
                        }
                    }
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }
            if (listOfMessage.isNotEmpty())
                getAppController()?.messageArrived(listOfMessage)
            iteration.release()
        }
    }
}