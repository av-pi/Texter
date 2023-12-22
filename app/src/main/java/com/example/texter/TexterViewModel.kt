package com.example.texter

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.texter.data.COLLECTION_CHAT
import com.example.texter.data.COLLECTION_USER
import com.example.texter.data.ChatData
import com.example.texter.data.ChatUser
import com.example.texter.data.Event
import com.example.texter.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.UUID
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

    // Dummy chats data
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)

    init {
        //onLogout()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid = uid)
        }
    }

    /**
     * Listens to the document of user with uid and
     * retrieves the user's stored data if logged in
     *
     * @param uid The uid of the user
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
     * Function to process logging in with Firebase
     *
     * @param email The email entered to login
     * @param password The password entered to login
     */
    fun onLogin(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill all fields")
            return
        }

        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        getUserData(uid = uid)
                    }
                } else {
                    handleException(task.exception, "Login failed")
                }
            }
            .addOnFailureListener {
                handleException(it, "Login failed")
            }
    }

    /**
     * Function to handle logout functionality.
     * Resets all relevant states in the viewmodel
     */
    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Successfully logged out")

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
        val user = UserData(
            userId = uid,
            // We don't want to reset userData state if no arguments passed with function call
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageUrl ?: userData.value?.imageUrl
        )

        uid?.let { uid ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        // Update user
                        it.reference.update(user.toMap())
                            .addOnSuccessListener {
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Could not update user")
                            }
                    } else {
                        // Create user
                        db.collection(COLLECTION_USER).document(uid).set(user)
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
     * Updates the user's name and number
     *
     * @param name The updated name
     * @param number The updated number
     */
    fun updateProfile(name: String, number: String) {
        createOrUpdateProfile(name = name, number = number)
    }

    /**
     * Centralised function to handle any kind of exception or error gracefully
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

    /**
     * Function to upload a picture to Firebase Storage
     *
     * @param uri The uri of the picture to be uploaded
     * @param onSuccess A function to handle successful upload
     */
    private fun uploadPicture(
        uri: Uri,
        onSuccess: (Uri) -> Unit
    ) {

        inProgress.value = true

        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("image/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProgress.value = false
        }
        uploadTask.addOnFailureListener {
            handleException(it)
        }
    }

    /**
     * Function to upload a profile picture to Firebase Storage
     *
     * @param uri The uri of the picture to be uploaded
     */
    fun uploadProfilePicture(uri: Uri) {
        uploadPicture(uri) {
            createOrUpdateProfile(
                imageUrl = it.toString()
            )
        }
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly()) {
            handleException(customMessage = "Please enter a valid number")
        } else {
            // Query database to check if chat already exists
            db.collection(COLLECTION_CHAT)
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("userOne.number", number),
                            Filter.equalTo("userTwo.number", userData.value?.number)
                        ),
                        Filter.and(
                            Filter.equalTo("userOne.number", userData.value?.number),
                            Filter.equalTo("userTwo.number", number)
                        )
                    )
                ).get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        // Chat does not exist, create new chat
                        db.collection(COLLECTION_USER).whereEqualTo("number", number)
                            .get()
                            .addOnSuccessListener {
                                if (it.isEmpty) {
                                    handleException(customMessage = "User not found")
                                } else {
                                    val chatPartner = it.toObjects<UserData>()[0]
                                    val id = db.collection(COLLECTION_CHAT).document().id
                                    val chat = ChatData(
                                        chatId = id,
                                        userOne = ChatUser(
                                            userId = userData.value?.userId,
                                            name = userData.value?.name,
                                            number = userData.value?.number,
                                            imageUrl = userData.value?.imageUrl
                                        ),
                                        userTwo = ChatUser(
                                            userId = chatPartner.userId,
                                            name = chatPartner.name,
                                            number = chatPartner.number,
                                            imageUrl = chatPartner.imageUrl
                                        )
                                    )
                                    db.collection(COLLECTION_CHAT).document(id).set(chat)
                                }
                            }
                            .addOnFailureListener {
                                 handleException(it)
                            }
                    } else {
                        handleException(customMessage = "Chat already exists")
                    }
                }

        }
    }

}

