package com.mj.assistant

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private val apiKey = "YOUR_GEMINI_API_KEY_HERE" // Apna Gemini API Key daalein

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this).apply {
            text = "MJ Assistant Active\nJARVIS & EDITH Protocol Online"
            textSize = 22f
            setPadding(40, 40, 40, 40)
        }
        setContentView(textView)

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
            speakWithEmotion("[EXCITED] Main MJ hoon, aapka personal assistant! EDITH system active ho chuka hai boss.")
        }
    }

    // 8) EMOTIONS ENGINE - Real-time Pitch & Speed Adjustment
    fun speakWithEmotion(text: String) {
        var cleanText = text
        var pitch = 1.0f
        var speed = 1.0f

        if (text.contains("[EXCITED]")) {
            cleanText = text.replace("[EXCITED]", "")
            pitch = 1.3f; speed = 1.2f
        } else if (text.contains("[SAD]")) {
            cleanText = text.replace("[SAD]", "")
            pitch = 0.8f; speed = 0.8f
        } else if (text.contains("[CONFIDENT]")) {
            cleanText = text.replace("[CONFIDENT]", "")
            pitch = 1.0f; speed = 1.1f
        }

        tts.setPitch(pitch)
        tts.setSpeechRate(speed)
        tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // 9) GEMINI API WORLD KNOWLEDGE BRAIN
    fun askGemini(userPrompt: String) {
        thread {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val prompt = "Aapka naam MJ hai. Direct Hinglish response do with mood tags [EXCITED], [SAD], or [CONFIDENT]. Command: $userPrompt"
                val body = "{\"contents\":[{\"parts\":[{\"text\":\"$prompt\"}]}]}"

                conn.outputStream.write(body.toByteArray())
                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val reply = json.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

                runOnUiThread { speakWithEmotion(reply) }
            } catch (e: Exception) {
                runOnUiThread { speakWithEmotion("[SAD] Boss, network connection check karein.") }
            }
        }
    }

    // 4) APP LAUNCHER
    fun openApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
            speakWithEmotion("[EXCITED] App khol raha hoon!")
        }
    }

    // 4) DIRECT AUTO CALL
    fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        startActivity(intent)
    }
}

