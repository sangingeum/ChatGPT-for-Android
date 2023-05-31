package edu.skku.cs.afinal

import android.widget.Toast
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

@OptIn(BetaOpenAI::class)
class ChatHelper(api_key : String) {
    private val openai = OpenAI(
        token = api_key,
        timeout = Timeout(socket = 60.seconds),
    )
    private var chatMessages = mutableListOf<ChatMessage>()
    private val model = ModelId("gpt-3.5-turbo")
    private var errorMessage = ChatMessage(
        role = ChatRole.Assistant,
        content = "An error occurred while executing the OpenAI API."
    )
    fun AddMessage(message : ChatMessage){
        chatMessages.add(message)
    }
    suspend fun GetResponse(userMessage : ChatMessage?): String {
        // append userMessage to chatMessages
        if(userMessage != null)
            chatMessages.add(userMessage)
        // create request
        val chatCompletionRequest = ChatCompletionRequest(
            model = model,
            messages = chatMessages
        )
        // get response
        var response = "Default text"
        try {
            withContext(Dispatchers.Default) {
                val completion: ChatCompletion = openai.chatCompletion(chatCompletionRequest)
                val assistantMessage = completion.choices[0].message
                if (assistantMessage != null) {
                    chatMessages.add(assistantMessage)
                    response = assistantMessage.content
                }
                else{
                    throw error("API Error")
                }
            }
        } catch (e: Exception) {
            response = "An error occurred: ${e.message}"
            chatMessages.add(ChatMessage(
                role = ChatRole.Assistant,
                content = response
            ))
        }
        return response
    }

    fun resetChatHistory(){
        chatMessages.clear()
    }
    fun getChatHistory(): List<ChatMessage> {
        return chatMessages as List<ChatMessage>
    }

    fun toChatData() : List<ChatMessageData>{
        val messageList = mutableListOf<ChatMessageData>()
        for(message in chatMessages){
            val roleText = when(message.role){
                ChatRole.User -> "User"
                ChatRole.Assistant -> "Assistant"
                ChatRole.System -> "System"
                else -> "Unknown"
            }
            val content = message.content
            messageList.add(ChatMessageData(roleText, content))
        }
        return messageList
    }
    fun fromChatData(messageData : List<ChatMessageData>){
        chatMessages.clear()
        for(message in messageData){
            val role = when(message.role){
                "User" -> ChatRole.User
                "Assistant" -> ChatRole.Assistant
                "System" -> ChatRole.System
                else -> ChatRole.User
            }
            chatMessages.add(ChatMessage(role=role, content=message.content))
        }
    }

}