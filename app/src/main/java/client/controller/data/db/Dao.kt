package client.controller.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun deleteUsers(vararg users: User)

    @Query("DELETE FROM user_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM user_table WHERE uid = :id")
    suspend fun suspendGetUserById(id: UUID): User?

    @Query("SELECT * FROM user_table WHERE uid = :id")
    fun getUserById(id: UUID): LiveData<List<User>>

    @Query("SELECT * FROM user_table WHERE uid in (:listID) ")
    suspend fun getUserById(listID: List<UUID>): List<User>

    @Query("SELECT * FROM user_table WHERE is_main_user = 0")
    fun getAllUser(): LiveData<List<User>>

    @Transaction
    @Query("SELECT * FROM user_table WHERE uid = :id")
    fun getUserMessageBox(id: UUID): LiveData<List<UserAndMessageBox>>

}

@Dao
interface MessageBoxDao {

    @Insert
    suspend fun insert(messageBox: MessageBox)

    @Delete
    suspend fun delete(messageBox: MessageBox)

    @Query("DELETE FROM message_box_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM message_box_table WHERE user_id= :uid")
    suspend fun getMessageBoxByUserId(uid: UUID): MessageBox?

    @Transaction
    @Query("SELECT * FROM message_box_table WHERE uid = :messageBoxId")
    fun getMessageBoxWithMessage(messageBoxId: Int): LiveData<List<MessageBoxWithMessage>>

    @Transaction
    @Query("SELECT * FROM message_table JOIN message_box_table ON message_box_id = message_box_table.uid WHERE message_box_table.uid = :uid")
    fun getMessageInThisMessageBox(uid: Int): LiveData<List<Message>>

    @Query("SELECT * FROM message_table WHERE user_id = :uid")
    fun getMessageFromThisUser(uid: UUID): LiveData<List<Message>>

}

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(message: Message)

    @Update
    suspend fun update(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("DELETE FROM message_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM message_table WHERE uid = :uid LIMIT 1")
    suspend fun getThisMessage(uid: UUID): Message?

}