package edu.skku.cs.afinal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

@OptIn(BetaOpenAI::class)
class ChatAdapter constructor(val context: Context, val items : List<ChatMessage>): BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }
    override fun getItem(position: Int): Any {
        return items.get(position)
    }
    override fun getItemId(position: Int): Long {
        return 0
    }
    override fun getView(i: Int, cvtView: View?, parent: ViewGroup?): View {
        val inflater : LayoutInflater =
            LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.chat, null)
        val role = items.get(i).role
        val roleText = when(role){
            ChatRole.User -> "User"
            ChatRole.Assistant -> "Assistant"
            ChatRole.System -> "System"
            else -> "Unknown"
        }
        val content = items.get(i).content
        var roleNameView = view.findViewById<TextView>(R.id.chatRoleNameTextView)
        var contentView = view.findViewById<TextView>(R.id.chatContentTextView)
        roleNameView.text = roleText
        contentView.text = content
        return view
    }

}