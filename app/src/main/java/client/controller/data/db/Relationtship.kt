package client.controller.data.db

import androidx.room.Embedded
import androidx.room.Relation


data class MessageBoxWithMessage(
    @Embedded val messageBox: MessageBox,
    @Relation(
        parentColumn = "uid",
        entityColumn = "message_box_id"
    )
    val messages: List<Message>
)


data class UserAndMessageBox(
    @Embedded val user: User,
    @Relation(
        parentColumn = "uid",
        entityColumn = "user_id"
    )
    val messageBox: MessageBox
)


