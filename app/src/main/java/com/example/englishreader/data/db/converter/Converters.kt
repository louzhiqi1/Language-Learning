package com.example.englishreader.data.db.converter

import androidx.room.TypeConverter
import com.example.englishreader.domain.model.WordStatus

class Converters {
    @TypeConverter
    fun fromWordStatus(status: WordStatus): String = status.name

    @TypeConverter
    fun toWordStatus(value: String): WordStatus = WordStatus.valueOf(value)
}
