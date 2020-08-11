package client.view.messagelist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import client.controller.AppController
import client.view.userlist.UserListActivity
import it.unibo.aggregatecomputingclient.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class UserMessageListActivity : AppCompatActivity() {

    lateinit var userMessageListViewModel: UserMessageListViewModel
    lateinit var editTextToSend: EditText
    lateinit var sendButton: Button
    lateinit var uid: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_message_list)

        editTextToSend = findViewById(R.id.editTextSend)
        sendButton = findViewById(R.id.sendButton)

        val extra = intent.extras
        if (extra != null)
            uid = extra.getSerializable("uid") as UUID
        else
            throw Exception("can't get the uid")

        val recyclerView: RecyclerView = findViewById(R.id.recyclerviewUserChat)
        val userMessageAdapter = UserMessageListAdapter(this)
        recyclerView.adapter = userMessageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        userMessageListViewModel = ViewModelProvider(this).get(UserMessageListViewModel::class.java)
        userMessageListViewModel.setList(uid)

        userMessageListViewModel.getAllMessage().observe(this, Observer {
            userMessageAdapter.setCachedMessage(it)
        })

        editTextToSend.setOnFocusChangeListener { view, b ->
            if (b)
                recyclerView.smoothScrollToPosition(userMessageAdapter.itemCount)

        }


        sendButton.setOnClickListener {
            recyclerView.smoothScrollToPosition(userMessageAdapter.itemCount)
            sendMessage()
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        val o = Intent(this, UserListActivity::class.java)
        startActivity(o)
        finish()
        return super.onSupportNavigateUp()
    }

    private fun sendMessage() {
        val messageToSend = editTextToSend.text.toString()
        if (messageToSend.isNotEmpty()) {
            GlobalScope.launch {
                AppController.getAppController()!!.dataController.sendMessage(messageToSend, uid)
            }
            editTextToSend.setText("")
        }
    }

}
