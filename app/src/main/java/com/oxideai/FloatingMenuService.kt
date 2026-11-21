package com.oxideai

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
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
            text = "OXIDE AI • управление"
            textSize = 16f
            setTextColor(0xFF71F0A2.toInt())
        }
        val status = TextView(this).apply {
            text = "Подключи службу специальных возможностей в настройках Android."
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
        }
        root.addView(title)
        root.addView(status)
        root.addView(Button(this).apply {
            text = "Тест: свайп камеры"
            isAllCaps = false
            setOnClickListener {
                val controller = GameControlAccessibilityService.instance
                status.text = when {
                    controller == null -> "Служба управления не включена. Открой OXIDE AI и включи её в настройках."
                    controller.performTestCameraSwipe() -> "Тестовый свайп отправлен. Проверь реакцию игры."
                    else -> "Android не принял жест. Проверь разрешение службы."
                }
            }
        })
        root.addView(Button(this).apply {
            text = "Задача: добыть дерево"
            isAllCaps = false
            setOnClickListener {
                status.text = "Автодобыча дерева пока не готова: сначала нужно добавить захват экрана, распознавание дерева и настройку зоны управления."
            }
        })
        root.addView(Button(this).apply {
            text = "Задача: добыть камень"
            isAllCaps = false
            setOnClickListener {
                status.text = "Автодобыча камня пока не готова: сначала нужно добавить захват экрана, распознавание камня и настройку зоны управления."
            }
        })
        root.addView(Button(this).apply {
            text = "СТОП / закрыть меню"
            isAllCaps = false
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
