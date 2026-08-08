// Flowtune Music by abhiram79
// github.com/abhiram79

package com.flowtune.music.utils

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat








abstract class InvincibleService : Service() {
    protected val handler = Handler(Looper.getMainLooper())
    protected abstract val isInvincibilityEnabled: Boolean
    protected abstract val notificationId: Int
    private var invincibility: Invincibility? = null

    private val isAllowedToStartForegroundServices: Boolean
        get() = !isAtLeastAndroid12 || isIgnoringBatteryOptimizations

    override fun onBind(intent: Intent?): Binder? {
        invincibility?.stop()
        invincibility = null
        return null
    }

    override fun onRebind(intent: Intent?) {
        invincibility?.stop()
        invincibility = null
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (isInvincibilityEnabled && isAllowedToStartForegroundServices) invincibility =
            Invincibility()
        return true
    }

    override fun onDestroy() {
        invincibility?.stop()
        invincibility = null
        super.onDestroy()
    }

    protected fun makeInvincible(isInvincible: Boolean = true) {
        if (isInvincible) invincibility?.start()
        else invincibility?.stop()
    }

    protected abstract fun shouldBeInvincible(): Boolean

    protected abstract fun notification(): Notification?

    private inner class Invincibility : BroadcastReceiver(), Runnable {
        private var isStarted = false
        private val intervalMs = 30_000L

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> handler.post(this)
                Intent.ACTION_SCREEN_OFF -> notification()?.let { notification ->
                    handler.removeCallbacks(this)
                    startForeground(notificationId, notification)
                }
            }
        }

        @Synchronized
        fun start() {
            if (!isStarted) {
                isStarted = true
                handler.postDelayed(this, intervalMs)

                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_SCREEN_OFF)
                }

                ContextCompat.registerReceiver(
                    this@InvincibleService,
                    this,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            }
        }

        @Synchronized
        fun stop() {
            if (isStarted) {
                handler.removeCallbacks(this)
                unregisterReceiver(this)
                isStarted = false
            }
        }

        override fun run() {
            if (shouldBeInvincible() && isAllowedToStartForegroundServices) {
                notification()?.let { notification ->
                    startForeground(notificationId, notification)
                    @Suppress("DEPRECATION")
                    stopForeground(false)
                    handler.postDelayed(this, intervalMs)
                }
            }
        }
    }
}
