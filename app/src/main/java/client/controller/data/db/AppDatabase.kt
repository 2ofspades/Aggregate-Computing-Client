package client.controller.data.db

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope



@Database(
    entities = [User::class, MessageBox::class, Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AwareNetDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun messageBoxDao(): MessageBoxDao

    companion object {
        @Volatile
        private var INSTANCE: AwareNetDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AwareNetDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AwareNetDatabase::class.java, "aware_net_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AwareNetDatabaseCallBack(scope))
                    .build()
                INSTANCE = instance
                return instance
            }

        }

        private class AwareNetDatabaseCallBack(private val scope: CoroutineScope) :
            RoomDatabase.Callback() {
            fun populateDatabase(mainUser: UserDao) {
                // for testing
                // Add your own user

            }
        }
    }
}
