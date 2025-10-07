package com.nenapiyasa.fees.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nenapiyasa.fees.dao.StudentDao
import com.nenapiyasa.fees.model.Student

@Database(entities = [Student::class], version = 1, exportSchema = false)
abstract class StudentDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile private var instance: StudentDatabase? = null

        fun getDatabase(context: Context): StudentDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "student_db"
                ).build().also { instance = it }
            }
    }
}
