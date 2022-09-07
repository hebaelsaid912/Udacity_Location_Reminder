package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import java.util.*


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
private const val TAG = "AuthenticationActivity"
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)


//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        binding.loginBtn.setOnClickListener {
            startSignIn()
        }
//          TODO: If the user was authenticated, send him to RemindersActivity
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Toast.makeText(this, "This email was already authenticated", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, RemindersActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "You need to login", Toast.LENGTH_LONG).show()
        }
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult? ->
        Log.d(TAG, "registerForActivityResult: result: $result")
        Log.d(TAG, "registerForActivityResult: result.code: ${result?.resultCode}")
        Toast.makeText(this, "Login Successfully", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

    private fun startSignIn() {
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                listOf(
                    GoogleBuilder().build(),
                    EmailBuilder().build()
                )
            )
            .build()
        signInLauncher.launch(signInIntent)
    }
}
