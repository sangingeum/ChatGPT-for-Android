package edu.skku.cs.afinal

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.HttpHeaders.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

@OptIn(BetaOpenAI::class)
class ChatRoomActivity : AppCompatActivity() {
    companion object{
        var DL = null as DrawerLayout?
        var counter = 10000
        var helper = null as ChatHelper?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_chat_room)
        val chatListView = findViewById<ListView>(R.id.chatListView)
        val key = intent.getStringExtra(MainActivity.EXT_KEY) ?: ""
        helper = ChatHelper(api_key = key)
        val sendBtn = findViewById<Button>(R.id.sendButton)
        val chatEditText = findViewById<EditText>(R.id.chatEditText)
        val backBtn = findViewById<ImageButton>(R.id.backButton)
        val listBtn = findViewById<ImageButton>(R.id.listButton)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        DL = drawerLayout
        var readyToSend = true

        sendBtn.setOnClickListener {
            val prompt = chatEditText.text.toString()
            if(prompt != "" && readyToSend){
                readyToSend = false
                chatEditText.text.clear()
                CoroutineScope(Dispatchers.Main).launch {
                    val userMessage = ChatMessage(
                        role = ChatRole.User,
                        content = prompt
                    )
                    helper!!.AddMessage(userMessage)
                    chatListView.adapter = ChatAdapter(applicationContext, helper!!.getChatHistory())
                    helper!!.GetResponse(null)
                    chatListView.adapter = ChatAdapter(applicationContext, helper!!.getChatHistory())
                    readyToSend = true
                }
            }
        }

        backBtn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        listBtn.setOnClickListener {
            val isOpen = DL?.isDrawerOpen(GravityCompat.START) ?: false
            if (isOpen) {
                DL?.closeDrawer(GravityCompat.START)
            }
            else{
                DL?.openDrawer(GravityCompat.START)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val chatListView = findViewById<ListView>(R.id.chatListView)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu: Menu = navigationView.menu
        menu.clear()
        initChatItems(context = applicationContext, menu=menu, chatListView=chatListView)
        val saveMenu = menu.add(1, 10001, 10001, "Save").setIcon(R.drawable.baseline_file_upload_24)
        val removeMenu = menu.add(1, 10002, 10002, "Remove").setIcon(R.drawable.baseline_remove_24)
        val resetMenu = menu.add(1, 10003, 10003, "Reset").setIcon(R.drawable.baseline_autorenew_24)

        saveMenu.setOnMenuItemClickListener {
            // Create a confirmation dialog
            val builder = AlertDialog.Builder(this@ChatRoomActivity)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to save the current chat?")
            // Set up the buttons for the dialog
            builder.setPositiveButton("Yes") { dialog, which ->
                // User clicked the Yes button, perform save operation here
                val fileName = getCurrentTimeAsString()
                val messageData = helper!!.toChatData()
                val jsonData = Gson().toJson(messageData)
                val file = File(applicationContext.dataDir,  fileName + ".chat")
                file.writeText(jsonData)

                val title = messageData[0].content.take(20) + "..."
                val creationTimestamp = file.lastModified()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val creationDateTime = "\n" + dateFormat.format(Date(creationTimestamp)) ?: ""
                val curItem = menu.add(0, counter, counter, title + creationDateTime)
                curItem.setIcon(R.drawable.baseline_chat_24)
                //menu.add(1, counter, counter, title + creationDateTime)
                curItem.setOnMenuItemClickListener {
                    helper!!.fromChatData(messageData)
                    CoroutineScope(Dispatchers.Main).launch{
                        chatListView.adapter = ChatAdapter(applicationContext, helper!!.getChatHistory())
                    }
                    true
                }
                counter -= 1
            }
            builder.setNegativeButton("No") { dialog, which ->
            }
            // Show the dialog
            val dialog = builder.create()
            dialog.show()
            true
        }
        removeMenu.setOnMenuItemClickListener {
            val intent = Intent(this, FileRemoveActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            true
        }
        resetMenu.setOnMenuItemClickListener {
            val builder = AlertDialog.Builder(this@ChatRoomActivity)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to reset the chat?")
            // Set up the buttons for the dialog
            builder.setPositiveButton("Yes") { dialog, which ->
                helper!!.resetChatHistory()
                chatListView.adapter = ChatAdapter(applicationContext, helper!!.getChatHistory())
            }
            builder.setNegativeButton("No"){dialog, which ->
            }
            val dialog = builder.create()
            dialog.show()
            true
        }
    }

    override fun onBackPressed() {
        val isOpen = DL?.isDrawerOpen(GravityCompat.START) ?: false
        if (isOpen) {
            DL?.closeDrawer(GravityCompat.START)
        } else {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }


    fun getCurrentTimeAsString(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return currentDateTime.format(formatter)
    }
    fun initChatItems(context : Context, menu: Menu, chatListView: ListView){
        val dataDir = applicationContext.dataDir
        val chatFiles = dataDir.listFiles { file ->
            file.name.matches("\\d+\\.chat".toRegex())
        }

        chatFiles?.forEach { file ->
            // Load the chat data from each file
            // You can access the unique integer ID using file.name.substringBefore(".")
            val jsonString = file.readText()
            val messageData: List<ChatMessageData> = Gson().fromJson(jsonString, object:TypeToken<List<ChatMessageData>>(){}.type)
            //Toast.makeText(this, messageData.toString(), Toast.LENGTH_SHORT).show()
            val title = messageData[0].content.take(20) + "..."

            val creationTimestamp = file.lastModified()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val creationDateTime = "\n" + dateFormat.format(Date(creationTimestamp)) ?: ""

            val curItem = menu.add(0, counter, counter, title + creationDateTime)
            curItem.setIcon(R.drawable.baseline_chat_24)
            //menu.add(1, counter, counter, title + creationDateTime)
            curItem.setOnMenuItemClickListener {
                helper!!.fromChatData(messageData)
                CoroutineScope(Dispatchers.Main).launch{
                    chatListView.adapter = ChatAdapter(context, helper!!.getChatHistory())
                }
                true
            }
            counter -= 1
        }
    }

}


/*
helper.AddMessage(ChatMessage(role=ChatRole.User, content = "Hi"))
        helper.AddMessage(ChatMessage(role=ChatRole.Assistant, content = "good morning"))
        helper.AddMessage(ChatMessage(role=ChatRole.User, content = "Hi2"))
        helper.AddMessage(ChatMessage(role=ChatRole.Assistant, content = "good morning2"))
        val jsonData = Gson().toJson(helper.toChatData())
        val file = File(applicationContext.dataDir, "0.chat")
        Toast.makeText(this, applicationContext.dataDir.toString(), Toast.LENGTH_SHORT).show()
        file.writeText(jsonData)
//menu.add(Menu.NONE, 0, 1, "New Item")
val messageData = helper.toChatData()
            val jsonData = Gson().toJson(messageData)
            val file = File(applicationContext.dataDir, "chat_messages.json")
            Toast.makeText(this, applicationContext.dataDir.toString(), Toast.LENGTH_SHORT).show()
            file.writeText(jsonData)

            val jsonString = file.readText()
            Toast.makeText(this, jsonString, Toast.LENGTH_SHORT).show()


companion object{
        var DL = null as DrawerLayout?
    }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        DL = drawerLayout
override fun onBackPressed() {
        val isOpen = DL?.isDrawerOpen(GravityCompat.START) ?: false
        if (isOpen) {
            DL?.closeDrawer(GravityCompat.START)
        } else {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

val key = "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"
        val helper = ChatHelper(api_key = key)
        val tv = findViewById<TextView>(R.id.textView)
        CoroutineScope(Dispatchers.Main).launch {
            val response = helper.GetResponse("How are you doing?")
            tv.text = response
        }
 */