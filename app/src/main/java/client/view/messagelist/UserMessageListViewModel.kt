package client.view.messagelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import client.controller.AppController
import client.controller.data.db.Message
import java.util.*


class UserMessageListViewModel(application: Application) : AndroidViewModel(application) {

    val dataController = AppController.getAppController(application).dataController
    lateinit var listOfMessage: LiveData<List<Message>>

    fun setList(uid: UUID) {
        listOfMessage = dataController.getMessage(uid)
    }

    fun getAllMessage(): LiveData<List<Message>> = listOfMessage
}