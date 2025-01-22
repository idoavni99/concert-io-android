package com.example.concertio.data.users

data class RemoteSourceUser(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val profilePicture: String? = null
) {
    fun toUserModel(): UserModel {
        return UserModel(
            uid = uid!!,
            name = name!!,
            email = email!!,
            profilePicture = profilePicture
        )
    }
}