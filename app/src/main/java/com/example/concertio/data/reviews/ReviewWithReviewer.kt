package com.example.concertio.data.reviews

import androidx.room.Embedded
import androidx.room.Relation
import com.example.concertio.data.ValidationResult
import com.example.concertio.data.users.UserModel
import java.util.UUID

data class ReviewWithReviewer(
    @Embedded val review: ReviewModel,
    @Relation(
        entity = UserModel::class,
        parentColumn = "reviewer_uid",
        entityColumn = "uid"
    ) val reviewer: UserModel
)