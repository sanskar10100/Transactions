package dev.sanskar.transactions.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sanskar.transactions.data.MIGRATION_2_3
import dev.sanskar.transactions.data.PreferenceStore
import dev.sanskar.transactions.data.TransactionDatabase
import dev.sanskar.transactions.notifications.NotificationScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRoomInstance(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        TransactionDatabase::class.java,
        "transactions"
        )
        .allowMainThreadQueries() // For small queries like sum calculation
        .addMigrations(MIGRATION_2_3)
        .build()

    @Singleton
    @Provides
    fun provideTransactionDao(db: TransactionDatabase) = db.transactionDao()

    @Provides
    @Singleton
    fun providePreferenceStoreInstance(@ApplicationContext context: Context) = PreferenceStore(context)

    @Provides
    @Singleton
    fun provideWorkManagerScheduler(@ApplicationContext context: Context) = NotificationScheduler(context)
}