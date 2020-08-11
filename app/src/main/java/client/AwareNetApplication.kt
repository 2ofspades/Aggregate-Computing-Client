package client

import android.app.Application
import client.controller.AppController

class AwareNetApplication : Application() {

    lateinit var appController: AppController

    override fun onCreate() {
        super.onCreate()
        appController = AppController.getAppController(this)
    }

}