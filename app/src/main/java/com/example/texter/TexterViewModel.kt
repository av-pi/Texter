package com.example.texter

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.texter.data.COLLECTION_USER
import com.example.texter.data.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class TexterViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val signedIn = mutableStateOf(false)

    fun onSignup(name: String, number: String, email: String, password: String) {
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true

        db.collection(COLLECTION_USER).whereEqualTo("number", number).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                // TODO: Create user profile
                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                        }
                } else {
                    handleException(customMessage = "Number already registered")
                }
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it)
            }

    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("Texter", "Something went wrong!", exception)
        exception?.printStackTrace()

        val errorMessage = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMessage else "$customMessage: $errorMessage"

        popupNotification.value = Event(message)
        inProgress.value = false

    }
}