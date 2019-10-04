package `is`.uxinn.fiam.manager

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit

class AppSessionObserver : DefaultLifecycleObserver {

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

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        backgroundTimeStamp = SystemClock.elapsedRealtime()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
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