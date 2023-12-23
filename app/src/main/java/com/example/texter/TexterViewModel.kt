package com.example.texter

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.texter.data.COLLECTION_CHAT
import com.example.texter.data.COLLECTION_MESSAGES
import com.example.texter.data.COLLECTION_STATUS
import com.example.texter.data.COLLECTION_USER
import com.example.texter.data.ChatData
import com.example.texter.data.ChatUser
import com.example.texter.data.Event
import com.example.texter.data.Message
import com.example.texter.data.Status
import com.example.texter.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TexterViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    // Authentication states
    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val signedIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    // Chats states
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessages = mutableStateOf(false)
    var currentChatMessagesListener: ListenerRegistration? = null

    // Status states
    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid = uid)
        }
    }

    /**
     * Retrieves the user's data stored in Firebase
     *
     * @param uid The user ID
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
                    populateChatsList()
                    populateStatuses()
                }
            }
    }

    /**
     * Signs the user into Firebase
     *
     * @param number The user's number
     * @param password The user's password
     * @param name The user's name
     * @param email The user's email
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
     * Logs the user into the application
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
     * Logs the user out of the application
     */
    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        chats.value = listOf()
        popupNotification.value = Event("Successfully logged out")

    }

    /**
     * Creates a new user or updates existing user
     *
     * @param name User's name
     * @param number User's number
     * @param imageUrl URL of user's image
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

        uid?.let { userId ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(userId).get()
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
                        db.collection(COLLECTION_USER).document(userId).set(user)
                        inProgress.value = false
                        getUserData(userId)
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

    /**
     * Adds a new chat with the given number.
     *
     * @param number The number of the user to add to the chat.
     */
    fun onAddChat(number: String) {
        if (number.isEmpty() || !number.isDigitsOnly()) {
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
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        // Chat does not exist, create new chat
                        db.collection(COLLECTION_USER).whereEqualTo("number", number)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.isEmpty) {
                                    handleException(customMessage = "User not found")
                                } else {
                                    val chatPartner = snapshot.toObjects<UserData>()[0]
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

    /**
     * Retrieves all chats of the logged in user
     */
    private fun populateChatsList() {
        inProgressChats.value = true

        db.collection(COLLECTION_CHAT).where(
            Filter.or(
                Filter.equalTo("userOne.userId", userData.value?.userId),
                Filter.equalTo("userTwo.userId", userData.value?.userId)
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                }
                inProgressChats.value = false
            }
    }

    /**
     * Adds a new message to the chat with the given ID.
     *
     * @param chatId The ID of the chat to add the message to.
     * @param message The message to be sent.
     */
    fun onSendMessage(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(
            sentBy = userData.value?.userId,
            message = message,
            timestamp = time
        )
        if (message.isEmpty()) {
            handleException(customMessage = "Please enter a message")
        } else {

            // Each chat document in the chat collection has a messages collection
            db.collection(COLLECTION_CHAT)
                .document(chatId)
                .collection(COLLECTION_MESSAGES)
                .document()
                .set(msg)
        }
    }

    /**
     * Retrieves all messages of a specific chat
     *
     * @param chatId The id of the chat to retrieve messages from
     */
    fun populateChatMessages(chatId: String) {
        inProgressChatMessages.value = true

        currentChatMessagesListener = db
            .collection(COLLECTION_CHAT)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents
                        .mapNotNull { it.toObject<Message>() }
                        .sortedBy { it.timestamp }
                }
                inProgressChatMessages.value = false
            }
    }

    /**
     * Cleans up the listener for the chat messages collection
     */
    fun depopulateChatMessages() {
        chatMessages.value = listOf()
        currentChatMessagesListener = null
    }

    /**
     * Creates a new status with the given image URL.
     *
     * @param imageUrl The URL of the image to be set as the status image.
     */
    private fun createStatus(imageUrl: String) {
        val newStatus = Status(
            user = ChatUser(
                userId = userData.value?.userId,
                name = userData.value?.name,
                number = userData.value?.number,
                imageUrl = userData.value?.imageUrl
            ),
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        // Add status to the Firebase db
        db.collection(COLLECTION_STATUS).document().set(newStatus)
    }

    fun uploadStatus(imageUri: Uri) {
        uploadPicture(imageUri) {
            createStatus(it.toString())
        }
    }

    /**
     * Retrieves all statuses of all users chatting with logged in user
     *
     */
    private fun populateStatuses() {
        inProgressStatus.value = true

        // Compute the 24 hour time cutoff for displaying status updates
        val millisTimeDelta = 24L * 60 * 60 * 1000
        val cutoff = Calendar.getInstance().timeInMillis - millisTimeDelta

        // Retrieve the users that have a chat with the user
        db.collection(COLLECTION_CHAT)
            .where(
                Filter.or(
                    Filter.equalTo("userOne.userId", userData.value?.userId),
                    Filter.equalTo("userTwo.userId", userData.value?.userId)
                )
            )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    // Want to display our own status updates as well
                    val currentConnections = arrayListOf(userData.value?.userId)
                    currentConnections.addAll(value.toObjects<ChatData>().map { chat ->
                        if (chat.userOne.userId == userData.value?.userId) chat.userTwo.userId
                        else chat.userOne.userId
                    })

                    db.collection(COLLECTION_STATUS)
                        .whereGreaterThan("timestamp", cutoff)
                        .whereIn("user.userId", currentConnections)
                        .addSnapshotListener { value1, error1 ->
                            if (error1 != null) {
                                handleException(error1)
                            }
                            if (value1 != null) {
                                status.value = value1.toObjects()
                            }
                            inProgressStatus.value = false
                        }
                }
            }
        inProgressStatus.value = false
    }
}


