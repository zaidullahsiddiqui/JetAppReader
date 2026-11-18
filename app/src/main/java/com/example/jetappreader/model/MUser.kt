package com.example.jetappreader.model

import com.google.firebase.firestore.PropertyName

data class MUser(
    @get:PropertyName("id") @set:PropertyName("id") var id: String? = null,
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String = "",
    @get:PropertyName("display_name") @set:PropertyName("display_name") var displayName: String = "",
    @get:PropertyName("avatar_url") @set:PropertyName("avatar_url") var avatarUrl: String? = null,
    @get:PropertyName("quote") @set:PropertyName("quote") var quote: String? = null,
    @get:PropertyName("profession") @set:PropertyName("profession") var profession: String? = null
)
