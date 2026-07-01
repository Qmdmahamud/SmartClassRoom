package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ClassroomRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ClassroomViewModel(private val repository: ClassroomRepository) : ViewModel() {

    // Roles: "Teacher" (Dr. Elizabeth Vance), "Student" (Liam Carter), "Parent" (Robert Carter - Liam's Parent)
    private val _currentRole = MutableStateFlow("Teacher")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _loggedInUser = MutableStateFlow<UserEntity?>(null)
    val loggedInUser: StateFlow<UserEntity?> = _loggedInUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _currentClassFilter = MutableStateFlow("class_physics")
    val currentClassFilter: StateFlow<String> = _currentClassFilter.asStateFlow()

    // Chat Filter: "All", "Teacher", "Parent", "Student"
    private val _chatRoleFilter = MutableStateFlow("All")
    val chatRoleFilter: StateFlow<String> = _chatRoleFilter.asStateFlow()

    val classes: StateFlow<List<ClassEntity>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<AssignmentEntity>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val submissions: StateFlow<List<SubmissionEntity>> = repository.allSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedDatabase()
        }
    }

    fun switchRole(role: String) {
        _currentRole.value = role
    }

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null
            // Artificial delay to simulate real network request securely
            kotlinx.coroutines.delay(1000)
            val user = repository.getUserByEmail(email.trim())
            if (user != null) {
                if (user.passwordHash == password) {
                    _loggedInUser.value = user
                    // Translate role options: "teacher" -> "Teacher", "student" -> "Student", "parent" -> "Parent"
                    val mappedRole = when (user.role.lowercase()) {
                        "teacher" -> "Teacher"
                        "student" -> "Student"
                        "parent" -> "Parent"
                        else -> "Teacher"
                    }
                    _currentRole.value = mappedRole
                    _isLoggingIn.value = false
                    onSuccess(mappedRole)
                } else {
                    _loginError.value = "Incorrect password. Please try again."
                    _isLoggingIn.value = false
                }
            } else {
                _loginError.value = "User not found. Use pre-seeded email or Register."
                _isLoggingIn.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null
            kotlinx.coroutines.delay(1000)

            val existing = repository.getUserByEmail(email.trim())
            if (existing != null) {
                _loginError.value = "Email is already registered."
                _isLoggingIn.value = false
                return@launch
            }

            val newUser = UserEntity(
                uid = UUID.randomUUID().toString(),
                name = name.trim(),
                email = email.trim(),
                role = role.trim().lowercase(),
                passwordHash = password
            )
            repository.insertUser(newUser)
            _isLoggingIn.value = false
            onSuccess()
        }
    }

    fun logout() {
        _loggedInUser.value = null
        _loginError.value = null
    }

    fun setClassFilter(classId: String) {
        _currentClassFilter.value = classId
    }

    fun setChatRoleFilter(filter: String) {
        _chatRoleFilter.value = filter
    }

    fun parseTimeToMinutes(timeStr: String): Int? {
        try {
            val clean = timeStr.trim().uppercase()
            val parts = clean.split(" ")
            if (parts.size < 2) return null
            val timePart = parts[0]
            val amPm = parts[1]
            val timeSplit = timePart.split(":")
            if (timeSplit.size < 2) return null
            var hour = timeSplit[0].toInt()
            val minute = timeSplit[1].toInt()
            if (amPm == "PM" && hour < 12) {
                hour += 12
            } else if (amPm == "AM" && hour == 12) {
                hour = 0
            }
            return hour * 60 + minute
        } catch (e: Exception) {
            return null
        }
    }

    fun checkRoutineConflict(classId: String, day: String, startTime: String, endTime: String, room: String, excludeItem: RoutineItem? = null): String? {
        val startNew = parseTimeToMinutes(startTime) ?: return "Invalid Start Time format. Use e.g. '09:00 AM'"
        val endNew = parseTimeToMinutes(endTime) ?: return "Invalid End Time format. Use e.g. '10:30 AM'"
        if (startNew >= endNew) {
            return "Start Time must be earlier than End Time."
        }

        classes.value.forEach { cls ->
            cls.routine.forEach { r ->
                if (excludeItem == null || excludeItem != r) {
                    if (r.day.trim().lowercase() == day.trim().lowercase() && r.room.trim().lowercase() == room.trim().lowercase()) {
                        val s = parseTimeToMinutes(r.startTime)
                        val e = parseTimeToMinutes(r.endTime)
                        if (s != null && e != null) {
                            if (startNew < e && s < endNew) {
                                return "Conflict: Room '$room' occupied on ${r.day} during ${r.startTime} - ${r.endTime} by '${cls.name}'."
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    // Interactive Schedule Modifier
    fun addScheduleRoutine(classId: String, routineItem: RoutineItem) {
        viewModelScope.launch {
            val classEntity = classes.value.find { it.classId == classId } ?: return@launch
            val updatedRoutine = classEntity.routine + routineItem
            repository.insertClass(classEntity.copy(routine = updatedRoutine))
        }
    }

    fun updateScheduleRoutine(classId: String, oldItem: RoutineItem, newItem: RoutineItem) {
        viewModelScope.launch {
            val classEntity = classes.value.find { it.classId == classId } ?: return@launch
            val updatedRoutine = classEntity.routine.map { if (it == oldItem) newItem else it }
            repository.insertClass(classEntity.copy(routine = updatedRoutine))
        }
    }

    fun deleteScheduleRoutine(classId: String, routineItem: RoutineItem) {
        viewModelScope.launch {
            val classEntity = classes.value.find { it.classId == classId } ?: return@launch
            val updatedRoutine = classEntity.routine.filter { it != routineItem }
            repository.insertClass(classEntity.copy(routine = updatedRoutine))
        }
    }

    fun addNewStudent(name: String, parentEmail: String, rollNumber: String, classId: String) {
        viewModelScope.launch {
            val studentId = if (rollNumber.isNotBlank()) rollNumber else "student_" + UUID.randomUUID().toString().take(6)
            val existingStudent = repository.getStudentById(studentId)
            if (existingStudent != null) {
                if (!existingStudent.enrolledClasses.contains(classId)) {
                    val updatedClasses = existingStudent.enrolledClasses + classId
                    repository.insertStudent(existingStudent.copy(enrolledClasses = updatedClasses))
                }
            } else {
                val newStudent = StudentEntity(
                    studentId = studentId,
                    name = name,
                    parentEmail = parentEmail,
                    enrolledClasses = listOf(classId),
                    attendanceHistory = emptyList(),
                    grades = emptyMap(),
                    engagementScore = 80
                )
                repository.insertStudent(newStudent)
            }
        }
    }

    // Rapid-Toggle Attendance Matrix: cycle Present -> Late -> Absent -> Present
    fun toggleAttendance(studentId: String, classId: String, dateString: String) {
        viewModelScope.launch {
            val student = students.value.find { it.studentId == studentId } ?: return@launch
            val existingIndex = student.attendanceHistory.indexOfFirst { it.date == dateString && it.classId == classId }

            val updatedHistory = student.attendanceHistory.toMutableList()
            if (existingIndex >= 0) {
                val currentStatus = student.attendanceHistory[existingIndex].status
                val nextStatus = when (currentStatus) {
                    "Present" -> "Late"
                    "Late" -> "Absent"
                    else -> "Present"
                }
                updatedHistory[existingIndex] = AttendanceRecord(dateString, classId, nextStatus)
            } else {
                updatedHistory.add(AttendanceRecord(dateString, classId, "Present"))
            }

            // Recalculate Engagement Score dynamically
            val presents = updatedHistory.count { it.status == "Present" }
            val lates = updatedHistory.count { it.status == "Late" }
            val total = updatedHistory.size
            val attRate = if (total > 0) ((presents + lates * 0.7) / total * 100).toInt().coerceIn(0, 100) else 100
            val newEngagement = ((attRate + student.engagementScore) / 2).coerceIn(40, 100)

            repository.insertStudent(student.copy(
                attendanceHistory = updatedHistory,
                engagementScore = newEngagement
            ))
        }
    }

    fun setAttendanceDirect(studentId: String, classId: String, dateString: String, status: String) {
        viewModelScope.launch {
            val student = students.value.find { it.studentId == studentId } ?: return@launch
            val existingIndex = student.attendanceHistory.indexOfFirst { it.date == dateString && it.classId == classId }

            val updatedHistory = student.attendanceHistory.toMutableList()
            if (existingIndex >= 0) {
                updatedHistory[existingIndex] = AttendanceRecord(dateString, classId, status)
            } else {
                updatedHistory.add(AttendanceRecord(dateString, classId, status))
            }

            // Recalculate Engagement Score dynamically
            val presents = updatedHistory.count { it.status == "Present" }
            val lates = updatedHistory.count { it.status == "Late" }
            val total = updatedHistory.size
            val attRate = if (total > 0) ((presents + lates * 0.7) / total * 100).toInt().coerceIn(0, 100) else 100
            val newEngagement = ((attRate + student.engagementScore) / 2).coerceIn(40, 100)

            repository.insertStudent(student.copy(
                attendanceHistory = updatedHistory,
                engagementScore = newEngagement
            ))
        }
    }

    fun markAllPresent(classId: String, dateString: String) {
        viewModelScope.launch {
            val classStudents = students.value.filter { it.enrolledClasses.contains(classId) }
            classStudents.forEach { student ->
                val existingIndex = student.attendanceHistory.indexOfFirst { it.date == dateString && it.classId == classId }
                val updatedHistory = student.attendanceHistory.toMutableList()
                if (existingIndex >= 0) {
                    updatedHistory[existingIndex] = AttendanceRecord(dateString, classId, "Present")
                } else {
                    updatedHistory.add(AttendanceRecord(dateString, classId, "Present"))
                }

                // Recalculate Engagement Score dynamically
                val presents = updatedHistory.count { it.status == "Present" }
                val lates = updatedHistory.count { it.status == "Late" }
                val total = updatedHistory.size
                val attRate = if (total > 0) ((presents + lates * 0.7) / total * 100).toInt().coerceIn(0, 100) else 100
                val newEngagement = ((attRate + student.engagementScore) / 2).coerceIn(40, 100)

                repository.insertStudent(student.copy(
                    attendanceHistory = updatedHistory,
                    engagementScore = newEngagement
                ))
            }
        }
    }

    // Dual-Pane Grading Hub: Grade an assignment and provide detailed feedback
    fun gradeSubmission(submissionId: String, pointsScored: Int, feedback: String) {
        viewModelScope.launch {
            val sub = submissions.value.find { it.submissionId == submissionId } ?: return@launch
            val updatedSub = sub.copy(
                status = "Graded",
                pointsScored = pointsScored,
                teacherFeedback = feedback
            )
            repository.insertSubmission(updatedSub)

            // Also update student's grades map
            val student = students.value.find { it.studentId == sub.studentId } ?: return@launch
            val updatedGrades = student.grades.toMutableMap()
            updatedGrades[sub.assignmentId] = pointsScored

            // Recalculate engagement score due to academic submission activity
            val updatedEngagement = (student.engagementScore + 5).coerceAtMost(100)

            repository.insertStudent(student.copy(
                grades = updatedGrades,
                engagementScore = updatedEngagement
            ))
        }
    }

    // Add Assignment
    fun addAssignment(classId: String, title: String, description: String, dueDate: Long, maxPoints: Int) {
        viewModelScope.launch {
            val assignment = AssignmentEntity(
                assignmentId = "assign_" + UUID.randomUUID().toString().take(6),
                classId = classId,
                title = title,
                description = description,
                dueDate = dueDate,
                maxPoints = maxPoints
            )
            repository.insertAssignment(assignment)

            // Create a general deadline reminder
            addReminder(
                classId = classId,
                type = "Deadline",
                title = "Due: $title",
                content = "Assignment '$title' is due. Total marks: $maxPoints.",
                targetDate = dueDate
            )
        }
    }

    // Add Reminder
    fun addReminder(classId: String, type: String, title: String, content: String, targetDate: Long) {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                reminderId = "rem_" + UUID.randomUUID().toString().take(6),
                classId = classId,
                type = type,
                title = title,
                content = content,
                targetDate = targetDate
            )
            repository.insertReminder(reminder)
        }
    }

    // Student Homework Vault: submit assignment
    fun submitHomework(assignmentId: String, studentId: String, studentName: String, contentText: String) {
        viewModelScope.launch {
            val assignment = assignments.value.find { it.assignmentId == assignmentId } ?: return@launch
            val isLate = System.currentTimeMillis() > assignment.dueDate
            val status = if (isLate) "Late" else "Submitted"

            val submission = SubmissionEntity(
                submissionId = "sub_" + UUID.randomUUID().toString().take(6),
                assignmentId = assignmentId,
                studentId = studentId,
                studentName = studentName,
                submissionDate = System.currentTimeMillis(),
                contentText = contentText,
                status = status,
                pointsScored = 0,
                teacherFeedback = ""
            )
            repository.insertSubmission(submission)
        }
    }

    // Optimized Messaging text chat
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val senderId: String
            val senderName: String
            val senderRole: String

            when (_currentRole.value) {
                "Teacher" -> {
                    senderId = "teacher_1"
                    senderName = "Dr. Elizabeth Vance"
                    senderRole = "Teacher"
                }
                "Student" -> {
                    senderId = "student_liam"
                    senderName = "Liam Carter"
                    senderRole = "Student"
                }
                else -> {
                    senderId = "parent_liam"
                    senderName = "Robert Carter (Liam's Parent)"
                    senderRole = "Parent"
                }
            }

            val message = MessageEntity(
                messageId = "msg_" + UUID.randomUUID().toString().take(6),
                channelId = "global",
                senderId = senderId,
                senderName = senderName,
                senderRole = senderRole,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(message)
        }
    }
}

class ClassroomViewModelFactory(private val repository: ClassroomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassroomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClassroomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
