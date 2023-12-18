package com.example.texter

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.texter.data.COLLECTION_USER
import com.example.texter.data.Event
import com.example.texter.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
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
    val userData = mutableStateOf<UserData?>(null)

    init {
        auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid->
            getUserData(uid = uid)
        }
    }

    /**
     * Listens to the document of user with uid and
     * retrieves the user's stored data if logged in
     */
    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTION_USER).document(uid)
            .addSnapshotListener { value, error ->
                if (error != null) handleException(error, "Cannot retrieve user data")
                if (value != null) {
                    val user = value.toObject<UserData>()
                    userData.value = user
                    inProgress.value = false
                }
            }
    }

    /**
     * Handles all possible paths of success and
     * failure after user has entered their details
     */
    fun onSignup(name: String, number: String, email: String, password: String) {
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true

        db.collection(COLLECTION_USER).whereEqualTo("number", number).get()
            .addOnSuccessListener {
                // Result empty i.e. new user
                if (it.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(
                                    name = name,
                                    number = number
                                )
                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                        }
                    // A user already associated with this number
                } else {
                    handleException(customMessage = "Number already registered")
                }
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    /**
     * Creates a new user or updates existing
     * user in the Firebase database
     */
    private fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(uid, name, number, imageUrl)

        uid?.let { uid ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        // Update user
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Could not update user")
                            }
                    } else {
                        // Create user
                        db.collection(COLLECTION_USER).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid)
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot retrieve user")
                }
        }
    }

    /**
     * Centralised function to handle any kind of exception gracefully
     *
     * @param exception A specific exception thrown. Could be null
     * @param customMessage A descriptive message to be shown to the user
     */
    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("Texter", "Something went wrong!", exception)
        exception?.printStackTrace()

        val errorMessage = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMessage else "$customMessage: $errorMessage"

        popupNotification.value = Event(message)
        inProgress.value = false

    }
}