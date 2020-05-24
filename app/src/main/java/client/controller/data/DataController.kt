package client.controller.data

import android.app.Application
import androidx.lifecycle.LiveData
import client.controller.AppController
import client.controller.data.db.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class DataController(app: Application) {

    lateinit var db: AwareNetDatabase
    lateinit var userDao: UserDao
    lateinit var messageBoxDao: MessageBoxDao
    lateinit var messageDao: MessageDao

    lateinit var mainUser: User

    private val messageToSend: MutableList<Message> = mutableListOf()

    init {
        GlobalScope.launch {
            db = AwareNetDatabase.getDatabase(app, this)
            userDao = db.userDao()
            messageBoxDao = db.messageBoxDao()
            messageDao = db.messageDao()
            deleteAllData() // test
            insertMainUser()

        }
    }

    suspend fun deleteAllData() {
        userDao.deleteAll()
        messageDao.deleteAll()
        messageBoxDao.deleteAll()
    }

    // Only for testing
    private suspend fun insertMainUser() {
        mainUser =
            User(uid = UUID.randomUUID(), isMainUser = true, username = AppController.getAppController()!!.name)
        insert(mainUser)
    }

    suspend fun insert(user: User) {
        // need to make sure the user doesn't already exist in the table
        userDao.insert(user)
        val messageBox = MessageBox(userUUID = user.uid)
        messageBoxDao.insert(messageBox)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun getUser(userID: UUID): User? {
        return userDao.suspendGetUserById(userID)
    }

    suspend fun userOnline(listOfID: List<UUID>) {
        val userOnline = userDao.getUserById(listOfID)
        userOnline.iterator().forEach {
            it.isOnline = true
            userDao.update(it)
        }
        // need to set the previous online offline
    }

    suspend fun getMessageBox(userId: UUID): MessageBox? {
        return messageBoxDao.getMessageBoxByUserId(userId)
    }

    suspend fun insert(messageInsert: Message) {
        val mes = messageDao.getThisMessage(messageInsert.uid)
        if (mes == null) {
            messageDao.insert(messageInsert)
        }
    }

    fun getAllUser(): LiveData<List<User>> {
        return userDao.getAllUser()
    }

    fun getMessageToSendFromList(): List<Message> {
        val list = mutableListOf<Message>()
        synchronized(messageToSend) {
            messageToSend.iterator().forEach {
                list.add(it)
            }
            messageToSend.clear()
        }
        return list
    }

    fun getMessage(user_uid: UUID): LiveData<List<Message>> {
        return messageBoxDao.getMessageFromThisUser(user_uid)
    }

    suspend fun sendMessage(content: String, userId: UUID) {
        val messageBox = getMessageBox(userId)
        if (messageBox != null) {
            val message = Message(
                userUUID = userId, content = content, date = Date(System.currentTimeMillis()),
                messageBoxId = messageBox.uid, typeContent = 1, isSentByMainUser = true
            )
            insert(message)
            messageToSend += message
        }
    }

    suspend fun getMessageFromUUID(uid: UUID): Message? {
        return messageDao.getThisMessage(uid)
    }
}