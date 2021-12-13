package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var binding: ActivityAuthenticationBinding

    private val authenticationViewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)

        observeAuthenticationState(binding.root)

        with(binding) {
            btnSignInWithGoogle.setOnClickListener { launchSignInFlow() }
            btnSignInWithEmail.setOnClickListener { launchSignInFlow() }
        }

            // Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

            // If the user was authenticated, send him to RemindersActivity

            // A bonus is to customize the sign in flow to look nice using :
            // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}"
                )
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun observeAuthenticationState(view: View) {

        authenticationViewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    Navigation.findNavController(view).navigate(R.id.remindersActivity);
                }
                else -> {
                    with(binding) {
                        btnSignInWithEmail.visibility = GONE
                        btnSignInWithGoogle.visibility = GONE
                        tvSignUpForANewAccount.visibility = GONE
                        linearLayoutSignUp.visibility = VISIBLE
                    }
                }
            }
        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }
}
