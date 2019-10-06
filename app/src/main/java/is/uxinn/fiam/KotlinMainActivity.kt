package `is`.uxinn.fiam

import `is`.uxinn.fiam.manager.AppSessionObserver
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import kotlinx.android.synthetic.main.activity_main.*

class KotlinMainActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseIam: FirebaseInAppMessaging
    private lateinit var appSessionObserver: AppSessionObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseIam = FirebaseInAppMessaging.getInstance()

        firebaseIam.setMessagesSuppressed(true)

        appSessionObserver = AppSessionObserver()
            .onSessionExpired {
                firebaseIam.setMessagesSuppressed(true)
                launchLockScreen()
            }
            .onSessionStart {
                firebaseAnalytics.logEvent(TRIGGER_APP_UNLOCKED, Bundle())
                firebaseIam.setMessagesSuppressed(false)
            }

        lifecycle.addObserver(appSessionObserver)

        firebaseIam.isAutomaticDataCollectionEnabled = true

        eventTriggerButton.text = getString(R.string.trigger_event_button_text, TRIGGER_EVENT)

        eventTriggerButton.setOnClickListener { view ->
            firebaseIam.triggerEvent(TRIGGER_EVENT)
            firebaseAnalytics.logEvent(TRIGGER_EVENT, Bundle())

            Snackbar.make(view, "Triggering '$TRIGGER_EVENT'!", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        // Get and display/log the Instance ID
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                val instanceId = instanceIdResult.id
                instanceIdText.text = getString(R.string.instance_id_fmt, instanceId)
                Log.d(TAG, "InstanceId: $instanceId")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error getting InstanceId", it)
            }
    }

    override fun onResume() {
        super.onResume()
        iamSuppressedText.text = getString(
            R.string.iam_suppressed, if (firebaseIam.areMessagesSuppressed()) "YES" else "NO"
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCK_SCREEN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                appSessionObserver.notifyAppUnlock()
            } else {
                finish()
            }
        }
    }

    private fun launchLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java)
        startActivityForResult(intent, LOCK_SCREEN_REQUEST, null)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        const val LOCK_SCREEN_REQUEST: Int = 10
        const val TRIGGER_EVENT = "trigger_event_1"
        const val TRIGGER_APP_UNLOCKED = "trigger_unlock"
        private const val TAG = "FIAM-Quickstart"
    }
}