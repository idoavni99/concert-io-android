package com.example.concertio.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.concertio.R
import com.example.concertio.ui.auth.AuthViewModel
import com.example.concertio.ui.main.MainActivity
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

class LoginFragment : Fragment() {
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupGoogleSignIn(view)
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
            authViewModel.signInWithIdToken(idOption, credentialManager, requireContext()) {
                toApp()
            }
        }
    }

    private fun toApp() {
        activity?.apply {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}