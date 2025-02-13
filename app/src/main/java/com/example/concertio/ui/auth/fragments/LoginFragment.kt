package com.example.concertio.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.concertio.R
import com.example.concertio.ui.auth.AuthViewModel
import com.example.concertio.ui.main.MainActivity
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (authViewModel.isUserLoggedIn()) {
            toApp()
        }

        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupGoogleSignIn(view)
        setupEmailSignIn(view)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupGoogleSignIn(view: View) {
        val credentialManager = CredentialManager.create(requireContext())
        view.findViewById<SignInButton>(R.id.google_sign_in_button).setOnClickListener {
            val idOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .setNonce(getString(R.string.app_name))
                .build()

            context?.let { context ->
                authViewModel.signInWithIdToken(idOption, credentialManager, context, ::toApp) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupEmailSignIn(view: View) {
        view.findViewById<MaterialButton>(R.id.email_sign_in_button).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun toApp() {
        activity?.apply {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}