package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.AttendanceRecord
import com.example.data.model.RoutineItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val routineListType = Types.newParameterizedType(List::class.java, RoutineItem::class.java)
    private val routineAdapter = moshi.adapter<List<RoutineItem>>(routineListType)

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val attendanceListType = Types.newParameterizedType(List::class.java, AttendanceRecord::class.java)
    private val attendanceAdapter = moshi.adapter<List<AttendanceRecord>>(attendanceListType)

    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Integer::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Int>>(mapType)

    @TypeConverter
    fun fromRoutineList(value: List<RoutineItem>?): String? {
        return value?.let { routineAdapter.toJson(it) }
    }

    @TypeConverter
    fun toRoutineList(value: String?): List<RoutineItem>? {
        return value?.let { routineAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { stringListAdapter.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { stringListAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromAttendanceList(value: List<AttendanceRecord>?): String? {
        return value?.let { attendanceAdapter.toJson(it) }
    }

    @TypeConverter
    fun toAttendanceList(value: String?): List<AttendanceRecord>? {
        return value?.let { attendanceAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromGradesMap(value: Map<String, Int>?): String? {
        return value?.let { mapAdapter.toJson(it) }
    }

    @TypeConverter
    fun toGradesMap(value: String?): Map<String, Int>? {
        return value?.let { mapAdapter.fromJson(it) }
    }
}
