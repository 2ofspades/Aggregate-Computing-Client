package client.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import client.controller.AppController
import client.view.userlist.UserListActivity
import it.unibo.aggregatecomputingclient.R

import java.lang.Exception
import java.net.InetAddress


class MainClientActivity : AppCompatActivity() {

    lateinit var myAddressTextView: TextView
    lateinit var myPortTextView: TextView
    lateinit var remoteAddressTextView: TextView
    lateinit var remotePortTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_client)
        myAddressTextView = findViewById(R.id.textViewMyAddress)
        myPortTextView = findViewById(R.id.textMyPort)
        remoteAddressTextView = findViewById(R.id.connectAddress)
        remotePortTextView = findViewById(R.id.connectPort)

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
