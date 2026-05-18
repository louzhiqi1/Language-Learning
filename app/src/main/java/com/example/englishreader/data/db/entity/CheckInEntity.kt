package com.example.englishreader.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey val date: String,
    val storiesRead: Int,
    val quizScore: Float,
    val pointsEarned: Int
)
