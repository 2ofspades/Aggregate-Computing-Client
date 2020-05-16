package client.view.userlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import client.controller.AppController
import client.controller.data.DataController
import client.controller.data.db.User

class UserListViewModel(application: Application) : AndroidViewModel(application) {

    private val dataController: DataController =
        AppController.getAppController(application).dataController
    private val allUser = dataController.getAllUser()

    fun getAllUser(): LiveData<List<User>> = allUser

}