package com.oxideai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var status: TextView
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 24)
            setBackgroundColor(0xFF10151B.toInt())
        }
        root.addView(TextView(this).apply {
            text = "OXIDE AI 0.2"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(0xFF71F0A2.toInt())
        })
        status = TextView(this).apply {
            text = "Голосовой помощник и плавающее меню"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 24, 0, 18)
        }
        output = TextView(this).apply {
            text = "Сначала разреши показ поверх других приложений."
            textSize = 15f
            setTextColor(0xFFB8C5D1.toInt())
            setPadding(0, 18, 0, 0)
        }
        root.addView(status)
        root.addView(button("Разрешить плавающее меню") { requestOverlayPermission() })
        root.addView(button("Показать меню поверх игры") { showFloatingMenu() })
        root.addView(button("🎙 Сказать команду") { listen() })
        root.addView(button("Остановить распознавание") {
            recognizer?.cancel()
            status.text = "Прослушивание остановлено."
        })
        root.addView(output)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 10 }
    }

    private fun requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            status.text = "Разрешение уже выдано."
            return
        }
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")))
    }

    private fun showFloatingMenu() {
        if (!Settings.canDrawOverlays(this)) {
            status.text = "Сначала выдай разрешение на показ поверх других приложений."
            requestOverlayPermission()
            return
        }
        startService(Intent(this, FloatingMenuService::class.java))
        status.text = "Плавающее меню запущено. Открой игру, чтобы проверить его."
    }

    private fun listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "На телефоне нет службы распознавания речи."
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 5)
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "Слушаю…" }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "Обрабатываю…" }
            override fun onError(error: Int) { status.text = "Ошибка распознавания: $error" }
            override fun onResults(results: Bundle?) {
                val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                output.text = "Распознано: «$phrase»"
                val command = phrase.lowercase(Locale("ru", "RU"))
                status.text = when {
                    "стоп" in command -> "Команда остановки принята."
                    "дерев" in command -> "Понял: задача «добыть дерево». Автосбор пока не подключён."
                    "кам" in command -> "Понял: задача «добыть камень». Автосбор пока не подключён."
                    else -> "Голос распознан. Это тестовый режим."
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажи задачу для OXIDE AI")
        }
        recognizer?.startListening(intent)
    }

    override fun onDestroy() {
        recognizer?.destroy()
        super.onDestroy()
    }
}
