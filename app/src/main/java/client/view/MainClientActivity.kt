package client.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import client.controller.AppController
import client.view.userlist.UserListActivity
import it.unibo.aggregatecomputingclient.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.lang.Exception
import java.net.InetAddress
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt
import kotlin.random.Random


class MainClientActivity : AppCompatActivity() {

    lateinit var myAddressTextView: TextView
    lateinit var myPortTextView: TextView
    lateinit var remoteAddressTextView: TextView
    lateinit var remotePortTextView: TextView
    lateinit var myTextUsername: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_client)
        myAddressTextView = findViewById(R.id.textViewMyAddress)
        myPortTextView = findViewById(R.id.textMyPort)
        remoteAddressTextView = findViewById(R.id.connectAddress)
        remotePortTextView = findViewById(R.id.connectPort)
        myTextUsername = findViewById(R.id.textMyUsername)
        val username = "John Doe " + (1..200).shuffled().first()
        myTextUsername.setText(username)

        val startProgramButton = findViewById<Button>(R.id.startButton)
        startProgramButton.setOnClickListener {
            startProgram(null)
        }

        val startServerButton = findViewById<Button>(R.id.startServerButton)
        startServerButton.setOnClickListener {
            startServerAction(null)
        }

        val connectButton = findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener {
            connectRemoteServer(null)
        }


    }

    private fun startServerAction(v: Bundle?) {
        val appController = AppController.getAppController(application)
        appController.name = myTextUsername.text.toString()

        val portStr = myPortTextView.text.toString()
        if (portStr.isNotEmpty() && portStr != "0") {
            try {
                val port = portStr.toInt()
                appController.start(port)
                myAddressTextView.text = appController.ip.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            appController.start()
            myAddressTextView.text = appController.ip.toString()
            myPortTextView.text = appController.port!!.toString()

        }
    }

    private fun startProgram(v: Bundle?) {
        val o = Intent(this, UserListActivity::class.java)
        val username = myTextUsername.text.toString()
        val dataController = AppController.getAppController()!!.dataController
        dataController.mainUser.username = username
        GlobalScope.launch { dataController.updateUser(dataController.mainUser) }
        startActivity(o)
    }

    private fun connectRemoteServer(v: Bundle?) {
        val str = remoteAddressTextView.text.toString()
        val remoteAddress = if (str.isNotEmpty())
            InetAddress.getByName(str)
        else
            InetAddress.getLoopbackAddress()
        val remotePort = remotePortTextView.text.toString().toInt()
        AppController.getAppController(application).searchServer(remoteAddress, remotePort)
    }

}
