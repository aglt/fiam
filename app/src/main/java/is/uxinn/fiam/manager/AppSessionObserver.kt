package `is`.uxinn.fiam.manager

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.TimeUnit

class AppSessionObserver : LifecycleObserver {

    private lateinit var onRenewedCallback: (Unit) -> Unit
    private lateinit var onExpiredCallback: (Unit) -> Unit

    private var backgroundTimeStamp: Long? = SystemClock.elapsedRealtime() - DELAY_MILLIS

    private fun isSessionAlive(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val backgroundTimeMillis = now - (backgroundTimeStamp ?: now)
        Log.i(
            TAG,
            "isSessionAlive() - sleep time ${TimeUnit.MILLISECONDS.toSeconds(backgroundTimeMillis)} SEC"
        )

        return backgroundTimeMillis < DELAY_MILLIS
    }

    fun onSessionExpired(onSessionExpired: (Unit) -> Unit): AppSessionObserver {
        this.onExpiredCallback = onSessionExpired
        return this
    }

    fun onSessionStart(onSessionRenewed: (Unit) -> Unit): AppSessionObserver {
        this.onRenewedCallback = onSessionRenewed
        return this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        backgroundTimeStamp = SystemClock.elapsedRealtime()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (!isSessionAlive()) {
            onExpiredCallback(Unit)
        } else {
            backgroundTimeStamp = null
        }
    }

    fun notifyAppUnlock() {
        backgroundTimeStamp = null
        onRenewedCallback(Unit)
    }

    companion object {
        const val DELAY_MILLIS = 10000L //10 sec
        const val TAG: String = "AppSessionObserver"
    }
}