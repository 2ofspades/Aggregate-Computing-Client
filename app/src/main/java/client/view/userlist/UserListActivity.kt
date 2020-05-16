package client.view.userlist

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import client.view.messagelist.UserMessageListActivity
import it.unibo.aggregatecomputingclient.R

class UserListActivity : AppCompatActivity() {

    lateinit var userListViewModel: UserListViewModel

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerviewUserList)
        val userListAdapter = UserListAdapter(this) { startMessageActivity(it) }
        recyclerView.adapter = userListAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        userListViewModel = ViewModelProvider(this).get(UserListViewModel::class.java)
        userListViewModel.getAllUser().observe(this, Observer {
            userListAdapter.setCachedProfile(it)
        })
        supportActionBar!!.setDefaultDisplayHomeAsUpEnabled(true)
    }

    private fun startMessageActivity(uid: Int) {
        val intent = Intent(this, UserMessageListActivity::class.java)
        val bundle = Bundle()
        bundle.putInt("uid", uid)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

}
