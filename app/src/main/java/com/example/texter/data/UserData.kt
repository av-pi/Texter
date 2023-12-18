package com.example.texter.data

/**
 * Data class for the Firebase db
 *
 * @param userId The unique ID of the user
 * @param name The user's name
 * @param number The user's number
 * @param imageUrl Url to the user's profile image
 */
data class UserData(
    val userId: String? = "",
    val name: String? = "",
    val number: String? = "",
    val imageUrl: String? = ""
) {

    /**
     * Function that maps fields of this data
     * class to fields in the Firebase database
     */
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "number" to number,
        "imageUrl" to imageUrl
    )
}