package edu.skku.cs.afinal

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import androidx.core.view.GravityCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class FileRemoveActivity : AppCompatActivity() {
    companion object{
        val checkedFiles = mutableSetOf<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_file_remove)
        val backBtn = findViewById<ImageButton>(R.id.removeActivityBackButton)
        val fileListView = findViewById<ListView>(R.id.removeFileListView)
        val removeBtn = findViewById<ImageButton>(R.id.removeActivityRemoveButton)
        backBtn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        removeBtn.setOnClickListener {
            val dataDir = applicationContext.dataDir
            for(fileName in checkedFiles){
                val file = File(dataDir, fileName)
                file.delete()
            }
            initRemoveFileListView(this@FileRemoveActivity, fileListView)
        }
        initRemoveFileListView(this@FileRemoveActivity, fileListView)
    }

    fun initRemoveFileListView(context : Context, fileListView : ListView){
        val dataDir = applicationContext.dataDir
        val chatFiles = dataDir.listFiles { file ->
            file.name.matches("\\d+\\.chat".toRegex())
        }
        val data = mutableListOf<FileInfoData>()
        chatFiles?.forEach { file ->
            // Load the chat data from each file
            // You can access the unique integer ID using file.name.substringBefore(".")
            val jsonString = file.readText()
            val messageData: List<ChatMessageData> = Gson().fromJson(jsonString, object:
                TypeToken<List<ChatMessageData>>(){}.type)
            //Toast.makeText(this, messageData.toString(), Toast.LENGTH_SHORT).show()
            val content = messageData[0].content.take(20) + "..."
            val fileName = file.name

            val creationTimestamp = file.lastModified()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val creationDateTime = "\n" + dateFormat.format(Date(creationTimestamp)) ?: ""
            data.add(FileInfoData(fileName, content, creationDateTime))
        }
        fileListView.adapter = RemoveFileAdapter(context, data)


    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}