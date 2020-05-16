package client.controller.data.db

import androidx.room.TypeConverter
import java.io.*
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimeStamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long? {
        return date?.time
    }


    @TypeConverter
    fun uuidToString(uuid: UUID?): String? {
        return uuid.toString()
    }

    @TypeConverter
    fun stringToUUID(uuid: String): UUID? {
        return UUID.fromString(uuid)
    }
}