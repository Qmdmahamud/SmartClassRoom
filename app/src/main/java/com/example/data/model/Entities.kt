package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoutineItem(
    val day: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val platform: String,
    val link: String
)

@JsonClass(generateAdapter = true)
data class AttendanceRecord(
    val date: String,
    val classId: String,
    val status: String // "Present", "Absent", "Late"
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey val classId: String,
    val name: String,
    val description: String,
    val teacherId: String,
    val teacherName: String,
    val routine: List<RoutineItem>
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val name: String,
    val parentEmail: String,
    val enrolledClasses: List<String>,
    val attendanceHistory: List<AttendanceRecord>,
    val grades: Map<String, Int>, // assignmentId -> pointsScored
    val engagementScore: Int
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val assignmentId: String,
    val classId: String,
    val title: String,
    val description: String,
    val dueDate: Long,
    val maxPoints: Int
)

@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey val submissionId: String,
    val assignmentId: String,
    val studentId: String,
    val studentName: String,
    val submissionDate: Long,
    val contentText: String,
    val status: String, // "Submitted", "Graded", "Late"
    val pointsScored: Int,
    val teacherFeedback: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val channelId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String, // "Teacher", "Student", "Parent"
    val text: String,
    val timestamp: Long
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val reminderId: String,
    val classId: String,
    val type: String, // "Exam", "Deadline", "Virtual Session", "Announcement"
    val title: String,
    val content: String,
    val targetDate: Long
)
