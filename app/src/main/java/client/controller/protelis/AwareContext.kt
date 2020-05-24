package client.controller.protelis

import adapters.protelis.ProtelisContext
import client.controller.AppController
import client.controller.data.networkdataclass.MessageNetwork
import client.controller.data.networkdataclass.ProfileNetwork
import devices.interfaces.Device
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.impl.ArrayTupleImpl
import org.protelis.vm.NetworkManager
import java.lang.Exception
import java.util.*
import java.util.concurrent.Semaphore

class AwareContext(private val device: Device, networkManager: NetworkManager) :
    ProtelisContext(device, networkManager) {

    private val user = AppController.getAppController()!!.dataController.mainUser
    override fun instance(): ProtelisContext =
        AwareContext(device, networkManager)

    //test
    private val profileAlreadyRead = mutableSetOf<UUID>()
    private val messageAlreadyRead = mutableSetOf<UUID>()

    override fun getDeviceUID(): DeviceUID {
        return device.id
    }

    fun announce(something: String) = device.showResult("$device - $something")
    fun getName() = device.name

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
            AppController.getAppController()?.userOnline(listOfIdUser)
    }

    fun getMyProfile(): Any {
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
            AppController.getAppController()?.profiles(profiles)
    }

    fun getMessageToSend(): ArrayTupleImpl {
        var arrayTupleImpl = ArrayTupleImpl()

        // can be blocking
        val messageToSend = AppController.getAppController()?.messageToSend()
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
            AppController.getAppController()?.messageArrived(listOfMessage)
    }
}