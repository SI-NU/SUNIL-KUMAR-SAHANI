package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LoanDao
import com.example.data.dao.MemberDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.TransactionDao
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemberEntity::class,
        LoanEntity::class,
        TransactionEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AetDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun loanDao(): LoanDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AetDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AetDatabase::class.java,
                    "aet_committee_database.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            scope.launch(Dispatchers.IO) {
                                populateInitialData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(database: AetDatabase) {
            if (database.memberDao().getMemberCount() == 0) {
                database.memberDao().insertMembers(AetInitialData.members)
            }
            if (database.loanDao().getLoanCount() == 0) {
                database.loanDao().insertLoans(AetInitialData.loans)
            }
            if (database.transactionDao().getTransactionCount() == 0) {
                database.transactionDao().insertTransactions(AetInitialData.createInitialTransactions())
            }
            if (database.notificationDao().getNotificationCount() == 0) {
                database.notificationDao().insertNotifications(AetInitialData.notifications)
            }
        }
    }
}
