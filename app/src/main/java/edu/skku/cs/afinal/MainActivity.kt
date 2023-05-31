package edu.skku.cs.afinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText


class MainActivity : AppCompatActivity() {
    companion object{
        const val EXT_KEY = "extra_key_for_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        val startBtn = findViewById<Button>(R.id.startButton)
        val apiEditText = findViewById<EditText>(R.id.apiEditText)
        startBtn.setOnClickListener {
            val key =  apiEditText.text.toString()
            val intent = Intent(this, ChatRoomActivity::class.java).apply {
                putExtra(EXT_KEY, key)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}

