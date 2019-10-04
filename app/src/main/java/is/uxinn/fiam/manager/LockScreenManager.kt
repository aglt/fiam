package `is`.uxinn.fiam.manager

import `is`.uxinn.fiam.LockScreenActivity
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit

class LockScreenManager private constructor(private var activity: AppCompatActivity) :
    DefaultLifecycleObserver {
    private var backgroundTimeStamp: Long? = null

    private fun isSessionAlive(): Boolean {
        val backgroundTimeMillis =
            SystemClock.elapsedRealtime() - (backgroundTimeStamp ?: 0)
        Log.i(
            TAG,
            "isSessionAlive() - sleep time ${TimeUnit.MILLISECONDS.toSeconds(backgroundTimeMillis)} SEC"
        )

        return backgroundTimeMillis < DELAY_MILLIS
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        backgroundTimeStamp = SystemClock.elapsedRealtime()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (!isSessionAlive()) {
            launchLockScreen()
        } else {
            backgroundTimeStamp = null
        }
    }

    private fun launchLockScreen() {
        val intent = Intent(activity, LockScreenActivity::class.java)
        activity.startActivityForResult(intent, LOCK_SCREEN_REQUEST, null)
    }

    companion object {
        fun from(activity: AppCompatActivity): LockScreenManager {
            return LockScreenManager(activity)
        }

        const val LOCK_SCREEN_REQUEST: Int = 10
        const val DELAY_MILLIS = 10000L //10 sec
        const val TAG: String = "LockScreenManager"
    }
}