package `is`.uxinn.fiam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import kotlinx.android.synthetic.main.activity_lock_screen.*
import kotlinx.android.synthetic.main.activity_lock_screen.iamSuppressedText
import kotlinx.android.synthetic.main.activity_main.*

class LockScreenActivity : AppCompatActivity() {

    private lateinit var firebaseIam: FirebaseInAppMessaging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        firebaseIam = FirebaseInAppMessaging.getInstance()

        iamSuppressedText.text = getString(
            R.string.iam_suppressed, if(firebaseIam.areMessagesSuppressed()) "YES" else "NO"
        )

        unlockButton.setOnClickListener {
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}
