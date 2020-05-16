package client.controller

import adapters.protelis.ProtelisAdapter
import adapters.protelis.ProtelisContext
import android.app.Application
import client.controller.data.DataController
import client.controller.data.db.User
import client.controller.data.networkdataclass.MessageNetwork
import client.controller.data.networkdataclass.ProfileNetwork
import communication.Message
import communication.MessageType
import devices.implementations.VirtualDevice
import devices.interfaces.Device
import it.unibo.aggregatecomputingclient.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.protelis.lang.datatype.impl.ArrayTupleImpl
import org.protelis.vm.NetworkManager

import server.devices.PeerDevice
import server.network.NetworkController
import server.network.ServerSupport
import server.network.communication.local.LocalNetworkController
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
    private val networkController: NetworkController = NetworkController { ServerSupport }
    val name = "Pix 3"
    private val protelistFileID: Int = R.raw.awarenet
    lateinit var server: PeerDevice
    lateinit var device: VirtualDevice
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
        localNetworkController = LocalNetworkController(ServerSupport, SERVER_ADDRESS, port)
        networkController.addController(localNetworkController)
        networkController.startServer()
        this.port = port
        val sem = Semaphore(-1)
        thread {
            networkController.getMainServer({
                server = it
                val message = Message(-1, MessageType.Join)
                server.tell(message)
            }, { message ->
                if (message.type == MessageType.ID) {
                    GlobalScope.launch {
                        val mainUser = User(message.content as Int, true, true, name)
                        dataController.insert(mainUser)
                        dataController.mainUser = mainUser
                        sem.release()
                    }
                    device = VirtualDevice(message.content as Int, name,
                        {
                            ProtelisAdapter(
                                it,
                                readFromRaw(protelistFileID),
                                ::AwareContext,
                                server
                            )
                        }, {
                            objectArrived(it)
                        })
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
                ServerSupport.execute()
                System.gc()
                Thread.sleep(1000)
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

    fun userOnline(list: MutableList<Int>) {
        GlobalScope.launch { dataController.userOnline(list) }
    }

    fun profiles(list: MutableList<ProfileNetwork>) {
        GlobalScope.launch {
            list.iterator().forEach {
                val user = dataController.getUser(it.userID)
                if (user == null) {
                    dataController.insert(
                        User(
                            it.userID,
                            false,
                            true,
                            it.username,
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
                if (message == null) {
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
                MessageNetwork(it.uid, dataController.mainUser.uid, it.userId, it.date, it.content)
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
        private val profileAlreadyRead = mutableSetOf<Int>()
        private val messageAlreadyRead = mutableSetOf<UUID>()
        private val iteration = Semaphore(1)

        fun announce(something: String) = device.showResult("$device - $something")
        fun getName() = device.toString()

        fun getID(): Int {
            return device.id
        }

        fun test(testArrayProfiles: ArrayTupleImpl) {
            val size = testArrayProfiles.size()
            val elem = testArrayProfiles.get(0) as ProfileNetwork
            print(size)
        }

        fun userOnline(arrayUserOnline: ArrayTupleImpl) {
            val listOfIdUser = mutableListOf<Int>()
            try {
                arrayUserOnline.iterator().forEach {
                    if (it as Int != device.id)
                        listOfIdUser += it
                }
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

            if (listOfIdUser.isNotEmpty())
                getAppController()?.userOnline(listOfIdUser)
        }

        fun getMyProfile(): Any {
            iteration.acquire()
            return ProfileNetwork(device.id, user.username, user.interest, null)
        }

        fun getProfiles(arrayTupleImpl: ArrayTupleImpl) {
            val profiles = mutableListOf<ProfileNetwork>()
            arrayTupleImpl.iterator().forEach {
                try {
                    val user = it as ProfileNetwork
                    //val user = it.get(0) as ProfileNetwork
                    if (!profileAlreadyRead.contains(user.userID)) {
                        profileAlreadyRead.add(user.userID)
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
                val destination = it.destination
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
                    it as ArrayTupleImpl
                    val message = it.get(0) as ArrayTupleImpl
                    if (message.get(0) == device.id) {
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