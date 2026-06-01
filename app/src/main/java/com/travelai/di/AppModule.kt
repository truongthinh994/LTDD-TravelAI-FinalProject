package com.travelai.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.travelai.BuildConfig
import com.travelai.data.api.ApiClient
import com.travelai.data.api.DeepSeekApi
import com.travelai.data.api.VisionApi
import com.travelai.data.api.VisionApiClient
import com.travelai.data.api.WeatherApi
import com.travelai.data.api.WeatherApiClient
import com.travelai.data.db.AppDatabase
import com.travelai.data.db.ChatDao
import com.travelai.data.db.LandmarkScanDao
import com.travelai.util.ACTIVE_VISION_PROVIDER
import com.travelai.util.VisionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Named("DeepSeekApiKey")
    fun provideDeepSeekApiKey(): String = BuildConfig.DEEPSEEK_API_KEY

    @Provides
    @Singleton
    @Named("MapsApiKey")
    fun provideMapsApiKey(): String = BuildConfig.MAPS_API_KEY

    @Provides
    @Singleton
    @Named("DeepSeekOkHttpClient")
    fun provideDeepSeekOkHttpClient(
        @Named("DeepSeekApiKey") apiKey: String
    ): OkHttpClient = ApiClient.createOkHttpClient(apiKey)

    @Provides
    @Singleton
    fun provideDeepSeekApi(
        @Named("DeepSeekOkHttpClient") client: OkHttpClient
    ): DeepSeekApi = ApiClient.create(client)

    @Provides
    @Singleton
    @Named("VisionApiKey")
    fun provideVisionApiKey(): String = when (ACTIVE_VISION_PROVIDER) {
        VisionProvider.GEMINI -> BuildConfig.GEMINI_API_KEY
        VisionProvider.OPENCODE_ZEN -> BuildConfig.OPENCODE_API_KEY
    }

    @Provides
    @Singleton
    fun provideVisionApi(
        @Named("VisionApiKey") apiKey: String
    ): VisionApi = VisionApiClient.create(
        baseUrl = ACTIVE_VISION_PROVIDER.baseUrl,
        apiKey = apiKey
    )

    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi = WeatherApiClient.create()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "travelai.db"
    )
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8
        )
        .build()

    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()

    @Provides
    @Singleton
    fun provideLandmarkScanDao(database: AppDatabase): LandmarkScanDao = database.landmarkScanDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `trip_profiles` (
                    `sessionId` INTEGER NOT NULL,
                    `destination` TEXT NOT NULL,
                    `days` INTEGER NOT NULL,
                    `budget` TEXT NOT NULL,
                    `people` INTEGER NOT NULL,
                    `travelStyle` TEXT NOT NULL,
                    `transport` TEXT NOT NULL,
                    `note` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`sessionId`),
                    FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `trip_plan_snapshots` (
                    `sessionId` INTEGER NOT NULL,
                    `rawResponse` TEXT NOT NULL,
                    `parsedJson` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`sessionId`),
                    FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `budget_items` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionId` INTEGER NOT NULL,
                    `category` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `amountVnd` INTEGER NOT NULL,
                    `note` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_budget_items_sessionId` ON `budget_items` (`sessionId`)"
            )
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `checklist_items` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionId` INTEGER NOT NULL,
                    `title` TEXT NOT NULL,
                    `isChecked` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_checklist_items_sessionId` ON `checklist_items` (`sessionId`)"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `chat_sessions` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `trip_map_places` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionId` INTEGER NOT NULL,
                    `dayNumber` INTEGER NOT NULL,
                    `period` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `query` TEXT NOT NULL,
                    `latitude` REAL,
                    `longitude` REAL,
                    `status` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    FOREIGN KEY(`sessionId`) REFERENCES `chat_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_trip_map_places_sessionId` ON `trip_map_places` (`sessionId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_trip_map_places_sessionId_dayNumber_period_name` ON `trip_map_places` (`sessionId`, `dayNumber`, `period`, `name`)"
            )
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `landmark_scans` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `location` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `history` TEXT NOT NULL,
                    `tipsJson` TEXT NOT NULL,
                    `confidence` REAL NOT NULL,
                    `imagePath` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
