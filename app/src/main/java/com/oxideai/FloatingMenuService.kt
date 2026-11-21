package com.oxideai

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FloatingMenuService : Service() {
    private lateinit var windowManager: WindowManager
    private var panel: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 14, 18, 14)
            setBackgroundColor(0xEE17212B.toInt())
        }
        val title = TextView(this).apply {
            text = "OXIDE AI • помощник"
            textSize = 16f
            setTextColor(0xFF71F0A2.toInt())
        }
        val status = TextView(this).apply {
            text = "Панель активна. Автосбор ещё не подключён."
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
        }
        root.addView(title)
        root.addView(status)
        root.addView(Button(this).apply {
            text = "Задача: добыть дерево"
            setOnClickListener { status.text = "Задача «дерево» выбрана. Ожидает подключения зрения и управления." }
        })
        root.addView(Button(this).apply {
            text = "Задача: добыть камень"
            setOnClickListener { status.text = "Задача «камень» выбрана. Ожидает подключения зрения и управления." }
        })
        root.addView(Button(this).apply {
            text = "СТОП / закрыть"
            setOnClickListener { stopSelf() }
        })
        panel = root
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 18
            y = 140
        }
        windowManager.addView(root, params)
    }

    override fun onDestroy() {
        panel?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        panel = null
        super.onDestroy()
    }
}
