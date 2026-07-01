package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AssignmentEntity::class,
        SubmissionEntity::class,
        MessageEntity::class,
        ReminderEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classroomDao(): ClassroomDao
}
