package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
    }

    private lateinit var binding: ActivityAuthenticationBinding

    // ActivityResultLauncher for FirebaseUI
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        binding.authButton.setOnClickListener {
            startFirebaseUIAuth()
        }

        // Register for activity result for FirebaseUI
        signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            onSignInResult(result)
        }

    }

    private fun startFirebaseUIAuth() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.layout_firebase_ui_login)
            .setEmailButtonId(R.id.tv_login_btn_email)
            .setGoogleButtonId(R.id.tv_login_btn_google)
            .build()
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(customLayout)
            .build()

        signInLauncher.launch(signInIntent)
    }

    // Handles the result from FirebaseUI authentication flow
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.d(TAG, "Sign-in successful: ${user?.uid}")
            startActivity(Intent(applicationContext, RemindersActivity::class.java))
        } else {
            // Sign in failed. If response is null the user canceled the sign-in flow.
            // Otherwise, check response.getError().getErrorCode() and handle the error.
            if (response == null) {
                Toast.makeText(this, "Sign-in cancelled.", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Sign-in cancelled by user")
            } else {
                Toast.makeText(this, "Sign-in failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Sign-in error: ${response.error?.errorCode} - ${response.error?.message}")
            }
        }
    }

}