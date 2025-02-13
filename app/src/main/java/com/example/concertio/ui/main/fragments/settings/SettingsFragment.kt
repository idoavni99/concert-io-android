package com.example.concertio.ui.main.fragments.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.credentials.CredentialManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.concertio.R
import com.example.concertio.extensions.FileUploadingFragment
import com.example.concertio.extensions.loadProfilePicture
import com.example.concertio.extensions.showProgress
import com.example.concertio.storage.FileCacheManager
import com.example.concertio.ui.auth.AuthViewModel
import com.example.concertio.ui.main.UserProfileViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.net.URL

class SettingsFragment : FileUploadingFragment() {
    private val profilePictureView by lazy { view?.findViewById<ImageView>(R.id.user_profile_picture) }
    private val nameField by lazy { view?.findViewById<EditText>(R.id.edit_profile_name) }
    private val emailField by lazy { view?.findViewById<EditText>(R.id.edit_profile_email) }
    private val passwordField by lazy { view?.findViewById<EditText>(R.id.edit_profile_password) }
    private val saveButton by lazy { view?.findViewById<MaterialButton>(R.id.save_profile_changes) }
    private val sensitiveFieldsButton by lazy { view?.findViewById<MaterialButton>(R.id.change_sensitive_fields) }

    private val userProfileViewModel by activityViewModels<UserProfileViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var profilePictureUri: Uri
    private val selectMediaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    profilePictureView?.loadProfilePicture(
                        requireContext(),
                        it
                    )
                    profilePictureUri = it
                }
            }
        }

    override fun onFileAccessGranted() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        selectMediaLauncher.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initFields()
        observeMyProfile()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initFields() {
        profilePictureView?.setOnClickListener {
            requestFileAccess()
        }
        saveButton?.setOnClickListener {
            saveButton?.showProgress()
            userProfileViewModel.updateProfile(
                nameField?.text.toString(),
                profilePictureUri
            ) {
                if (sensitiveFieldsButton?.isVisible != true) {
                    userProfileViewModel.updateAuth(
                        emailField?.text?.toString()!!,
                        passwordField?.text?.toString()!!,
                        ::toUserProfile
                    )
                } else {
                    toUserProfile()
                }
            }
        }
        sensitiveFieldsButton?.setOnClickListener {
            val hasSignedInWithGoogle = FirebaseAuth.getInstance().currentUser?.providerData?.any {
                it.providerId == GoogleAuthProvider.PROVIDER_ID
            } ?: false
            if (hasSignedInWithGoogle) {
                emailField?.isVisible = false
                CredentialManager.create(requireContext()).apply {
                    val idOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setAutoSelectEnabled(true)
                        .setNonce(getString(R.string.app_name))
                        .build()
                    authViewModel.signInWithIdToken(
                        idOption,
                        credentialManager = this,
                        requireContext(),
                        ::showSensitiveFields
                    ) {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                activity?.let {
                    val dialog = ReauthenticateDialog(::showSensitiveFields) {
                        Toast.makeText(context, "Unexpected Error", Toast.LENGTH_SHORT).show()
                    }
                    dialog.show(it.supportFragmentManager, "reauthenticate")
                }
            }
        }
    }

    private fun showSensitiveFields() {
        sensitiveFieldsButton?.isVisible = false
        emailField?.isVisible = true
        passwordField?.isVisible = true
    }

    private fun observeMyProfile() {
        userProfileViewModel.observeMyProfile().observe(viewLifecycleOwner) {
            it?.run {
                nameField?.setText(name)
                lifecycleScope.launch {
                    profilePicture?.run {
                        profilePictureUri = FileCacheManager.getFileLocalUri(URL(this))
                        profilePictureView?.loadProfilePicture(
                            requireContext(),
                            profilePictureUri,
                        )
                    }
                }
                emailField?.setText(email)
            }
        }
    }

    private fun toUserProfile() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToUserProfileFragment())
    }
}