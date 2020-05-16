package client.controller.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*


@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "is_main_user") val isMainUser: Boolean = false,
    var isOnline: Boolean = false,
    var username: String,
    var interest: String = "Add your interest Here",
    @ColumnInfo(name = "image_path") var imagePath: String = ""
)

@Entity(tableName = "message_box_table")
data class MessageBox(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int
)


@Entity(tableName = "message_table")
data class Message(
    @PrimaryKey val uid: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "message_box_id") val messageBoxId: Int,
    @ColumnInfo(name = "time") val date: Date,
    @ColumnInfo(name = "sent_by_main_user") val isSentByMainUser: Boolean,
    @ColumnInfo(name = "type_content") val typeContent: Int,
    @ColumnInfo(name = "content") var content: String
)