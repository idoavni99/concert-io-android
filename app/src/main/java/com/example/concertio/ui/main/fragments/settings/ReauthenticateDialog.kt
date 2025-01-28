package com.example.concertio.ui.main.fragments.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.concertio.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReauthenticateDialog(val onReauthenticated: () -> Unit, val onError: () -> Unit) :
    DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it).run {
                val view = it.layoutInflater.inflate(R.layout.dialog_reauthenticate, null)
                setView(view)
                view.findViewById<MaterialButton>(R.id.reauthenticate_cancel).setOnClickListener {
                    dialog?.cancel()
                }
                view.findViewById<MaterialButton>(R.id.reauthenticate_sign_in).setOnClickListener {
                    val email =
                        view.findViewById<EditText>(R.id.reauthenticate_email)
                    val password =
                        view.findViewById<EditText>(R.id.reauthenticate_password)
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                                    email.text.toString(),
                                    password.text.toString()
                                )
                            }
                            onReauthenticated()
                            dialog?.dismiss()
                        } catch (e: Exception) {
                            onError()
                            dialog?.cancel()
                        }
                    }
                }
                create()
            }
        } ?: throw IllegalStateException("Cant render dialog under no activity")
    }
}