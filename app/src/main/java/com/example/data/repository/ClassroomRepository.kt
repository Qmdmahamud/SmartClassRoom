package com.example.data.repository

import com.example.data.db.ClassroomDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ClassroomRepository(private val dao: ClassroomDao) {

    val allClasses: Flow<List<ClassEntity>> = dao.getAllClasses()
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allAssignments: Flow<List<AssignmentEntity>> = dao.getAllAssignments()
    val allSubmissions: Flow<List<SubmissionEntity>> = dao.getAllSubmissions()
    val allMessages: Flow<List<MessageEntity>> = dao.getAllMessages()
    val allReminders: Flow<List<ReminderEntity>> = dao.getAllReminders()

    suspend fun insertClass(classEntity: ClassEntity) = dao.insertClass(classEntity)
    suspend fun insertStudent(student: StudentEntity) = dao.insertStudent(student)
    suspend fun insertAssignment(assignment: AssignmentEntity) = dao.insertAssignment(assignment)
    suspend fun insertSubmission(submission: SubmissionEntity) = dao.insertSubmission(submission)
    suspend fun insertMessage(message: MessageEntity) = dao.insertMessage(message)
    suspend fun insertReminder(reminder: ReminderEntity) = dao.insertReminder(reminder)

    suspend fun getStudentById(studentId: String): StudentEntity? = dao.getStudentById(studentId)

    suspend fun seedDatabase() {
        val currentClasses = allClasses.first()
        if (currentClasses.isNotEmpty()) return // Already seeded!

        // Seed Classes
        val classes = listOf(
            ClassEntity(
                classId = "class_physics",
                name = "AP Physics C: Mechanics",
                description = "Calculus-based physics covering kinematics, Newton's laws, rotational dynamics, and gravity.",
                teacherId = "teacher_1",
                teacherName = "Dr. Elizabeth Vance",
                routine = listOf(
                    RoutineItem("Monday", "09:00 AM", "10:30 AM", "Room 402", "Zoom", "https://zoom.us/j/901234567"),
                    RoutineItem("Wednesday", "09:00 AM", "10:30 AM", "Room 402", "Zoom", "https://zoom.us/j/901234567")
                )
            ),
            ClassEntity(
                classId = "class_lit",
                name = "English Lit & Composition",
                description = "Analytical reading of literary masterpieces with emphasis on persuasive expository writing.",
                teacherId = "teacher_1",
                teacherName = "Dr. Elizabeth Vance",
                routine = listOf(
                    RoutineItem("Tuesday", "11:00 AM", "12:30 PM", "Room 105", "Google Meet", "https://meet.google.com/abc-defg-hij"),
                    RoutineItem("Thursday", "11:00 AM", "12:30 PM", "Room 105", "Google Meet", "https://meet.google.com/abc-defg-hij")
                )
            ),
            ClassEntity(
                classId = "class_chem",
                name = "Organic Chemistry II",
                description = "Structure, synthesis, and reactions of aliphatic and aromatic hydrocarbons, with lab components.",
                teacherId = "teacher_2",
                teacherName = "Prof. Marcus Albright",
                routine = listOf(
                    RoutineItem("Tuesday", "02:00 PM", "03:30 PM", "Lab Hall B", "Zoom", "https://zoom.us/j/987654321"),
                    RoutineItem("Friday", "02:00 PM", "03:30 PM", "Lab Hall B", "Zoom", "https://zoom.us/j/987654321")
                )
            )
        )
        dao.insertClasses(classes)

        // Seed Assignments
        val assignments = listOf(
            AssignmentEntity(
                assignmentId = "assign_phys_1",
                classId = "class_physics",
                title = "Rotational Inertia Lab Report",
                description = "Submit your formal laboratory report on rotational dynamics, including raw data tables and error analysis.",
                dueDate = System.currentTimeMillis() + 86400000L * 2, // 2 days from now
                maxPoints = 100
            ),
            AssignmentEntity(
                assignmentId = "assign_phys_2",
                classId = "class_physics",
                title = "Kepler's Laws Problem Set",
                description = "Solve problems 1-15 on Page 234 regarding planetary orbits and orbital velocities.",
                dueDate = System.currentTimeMillis() - 86400000L * 3, // 3 days ago
                maxPoints = 50
            ),
            AssignmentEntity(
                assignmentId = "assign_lit_1",
                classId = "class_lit",
                title = "Macbeth Character Foil Essay",
                description = "A 1000-word essay analyzing Macbeth's relationship with Banquo or Macduff as a character foil.",
                dueDate = System.currentTimeMillis() + 86400000L * 5, // 5 days from now
                maxPoints = 100
            ),
            AssignmentEntity(
                assignmentId = "assign_chem_1",
                classId = "class_chem",
                title = "Stereochemistry & Chirality Quiz",
                description = "Complete the nomenclature and optical activity identification questions in the worksheet.",
                dueDate = System.currentTimeMillis() + 86400000L * 1, // Tomorrow
                maxPoints = 30
            )
        )
        dao.insertAssignments(assignments)

        // Seed Students
        val students = listOf(
            StudentEntity(
                studentId = "student_liam",
                name = "Liam Carter",
                parentEmail = "parent_liam@example.com",
                enrolledClasses = listOf("class_physics", "class_chem"),
                attendanceHistory = listOf(
                    AttendanceRecord("2026-06-25", "class_physics", "Present"),
                    AttendanceRecord("2026-06-27", "class_physics", "Present"),
                    AttendanceRecord("2026-06-26", "class_chem", "Present"),
                    AttendanceRecord("2026-06-29", "class_physics", "Late")
                ),
                grades = mapOf(
                    "assign_phys_2" to 48
                ),
                engagementScore = 94
            ),
            StudentEntity(
                studentId = "student_olivia",
                name = "Olivia Vance",
                parentEmail = "parent_olivia@example.com",
                enrolledClasses = listOf("class_physics", "class_lit"),
                attendanceHistory = listOf(
                    AttendanceRecord("2026-06-25", "class_physics", "Present"),
                    AttendanceRecord("2026-06-27", "class_physics", "Absent"),
                    AttendanceRecord("2026-06-28", "class_lit", "Present")
                ),
                grades = mapOf(
                    "assign_phys_2" to 38
                ),
                engagementScore = 82
            ),
            StudentEntity(
                studentId = "student_noah",
                name = "Noah Albright",
                parentEmail = "parent_noah@example.com",
                enrolledClasses = listOf("class_lit", "class_chem"),
                attendanceHistory = listOf(
                    AttendanceRecord("2026-06-26", "class_chem", "Present"),
                    AttendanceRecord("2026-06-28", "class_lit", "Present"),
                    AttendanceRecord("2026-06-30", "class_lit", "Late")
                ),
                grades = mapOf(),
                engagementScore = 75
            ),
            StudentEntity(
                studentId = "student_emma",
                name = "Emma Watson",
                parentEmail = "parent_emma@example.com",
                enrolledClasses = listOf("class_physics", "class_lit", "class_chem"),
                attendanceHistory = listOf(
                    AttendanceRecord("2026-06-25", "class_physics", "Present"),
                    AttendanceRecord("2026-06-27", "class_physics", "Present"),
                    AttendanceRecord("2026-06-26", "class_chem", "Present"),
                    AttendanceRecord("2026-06-28", "class_lit", "Present")
                ),
                grades = mapOf(
                    "assign_phys_2" to 50
                ),
                engagementScore = 98
            ),
            StudentEntity(
                studentId = "student_sophia",
                name = "Sophia Martinez",
                parentEmail = "parent_sophia@example.com",
                enrolledClasses = listOf("class_physics", "class_chem"),
                attendanceHistory = listOf(
                    AttendanceRecord("2026-06-25", "class_physics", "Absent"),
                    AttendanceRecord("2026-06-27", "class_physics", "Present"),
                    AttendanceRecord("2026-06-26", "class_chem", "Present")
                ),
                grades = mapOf(
                    "assign_phys_2" to 42
                ),
                engagementScore = 88
            )
        )
        dao.insertStudents(students)

        // Seed Submissions
        val submissions = listOf(
            SubmissionEntity(
                submissionId = "sub_liam_1",
                assignmentId = "assign_phys_2",
                studentId = "student_liam",
                studentName = "Liam Carter",
                submissionDate = System.currentTimeMillis() - 86400000L * 4,
                contentText = "Answers:\n1. T = 2π√(r³/GM) = 5400s\n2. v = √(GM/r) = 7500 m/s\n3. Elliptical path centered at focus.\n[Full answers 4-15 submitted via document links]",
                status = "Graded",
                pointsScored = 48,
                teacherFeedback = "Excellent calculations. Minor rounding error in Kepler's third constant on Q12, but otherwise flawless."
            ),
            SubmissionEntity(
                submissionId = "sub_olivia_1",
                assignmentId = "assign_phys_2",
                studentId = "student_olivia",
                studentName = "Olivia Vance",
                submissionDate = System.currentTimeMillis() - 86400000L * 4,
                contentText = "Homework submission Kepler's laws. See orbit formulas sheet.\n1. T = 5400s\n2. v = 7400 m/s (approximated)",
                status = "Graded",
                pointsScored = 38,
                teacherFeedback = "A bit rushed on the second half of the worksheet. Ensure you show your derivations."
            ),
            SubmissionEntity(
                submissionId = "sub_emma_1",
                assignmentId = "assign_phys_2",
                studentId = "student_emma",
                studentName = "Emma Watson",
                submissionDate = System.currentTimeMillis() - 86400000L * 5,
                contentText = "Kepler's Laws Complete Problem Set:\n1. Proof that Kepler's second law is conservation of angular momentum: dA/dt = L / 2m...\n2. Orbit calculation: a³ / T² = G(M1 + M2) / 4π²...",
                status = "Graded",
                pointsScored = 50,
                teacherFeedback = "Stunning work, Emma. Your derivation of the orbital sweep area is college-level."
            ),
            SubmissionEntity(
                submissionId = "sub_liam_2",
                assignmentId = "assign_phys_1",
                studentId = "student_liam",
                studentName = "Liam Carter",
                submissionDate = System.currentTimeMillis() - 3600000L, // 1 hour ago
                contentText = "Physics Lab Report - Rotational Dynamics and Inertia Matrix.\nObjective: To determine the moment of inertia of complex composite solids using oscillatory cycles.\nData:\nSolid A: Mass = 1.2kg, Radius = 0.05m. T_obs = 1.12s\nSolid B: Mass = 0.8kg, Radius = 0.03m. T_obs = 0.85s\nAnalysis and discussion is attached. Graph shows strict correlation.",
                status = "Submitted",
                pointsScored = 0,
                teacherFeedback = ""
            ),
            SubmissionEntity(
                submissionId = "sub_olivia_2",
                assignmentId = "assign_phys_1",
                studentId = "student_olivia",
                studentName = "Olivia Vance",
                submissionDate = System.currentTimeMillis() - 2 * 3600000L, // 2 hours ago
                contentText = "Rotational Inertia Lab Report submission. Dr. Vance, here is my lab data and calculations:\nTrial 1: Cylinder, I_theory = 0.5 * M * R^2 = 0.0015 kg.m2\nMeasured Inertia = 0.0016 kg.m2 (6.6% error).\nWe found that air friction and thread mass contributed to the error margin.",
                status = "Submitted",
                pointsScored = 0,
                teacherFeedback = ""
            )
        )
        dao.insertSubmissions(submissions)

        // Seed Messages
        val messages = listOf(
            MessageEntity(
                messageId = "msg_1",
                channelId = "global",
                senderId = "teacher_1",
                senderName = "Dr. Elizabeth Vance",
                senderRole = "Teacher",
                text = "Welcome everyone to our Smart Classroom Platform! You can check your schedules under the Routines tab, view active assignments, and contact me directly. Parents, feel free to use the Parent-Teacher quick-chat pathway here.",
                timestamp = System.currentTimeMillis() - 86400000L * 2
            ),
            MessageEntity(
                messageId = "msg_2",
                channelId = "global",
                senderId = "student_emma",
                senderName = "Emma Watson",
                senderRole = "Student",
                text = "Thank you Dr. Vance! The virtual Launchpad is super helpful. I can join the lectures with a single tap now.",
                timestamp = System.currentTimeMillis() - 86400000L * 1
            ),
            MessageEntity(
                messageId = "msg_3",
                channelId = "global",
                senderId = "parent_liam",
                senderName = "Robert Carter (Liam's Parent)",
                senderRole = "Parent",
                text = "Hello Dr. Vance, thank you for setting this up. It is great to see Liam's live engagement logs and weekly grades in real-time.",
                timestamp = System.currentTimeMillis() - 3600000L * 5
            )
        )
        dao.insertMessages(messages)

        // Seed Reminders
        val reminders = listOf(
            ReminderEntity(
                reminderId = "rem_1",
                classId = "class_physics",
                type = "Exam",
                title = "Physics Midterm Exam",
                content = "The Physics Mechanics Midterm will take place in Room 402. Bring a scientific calculator and a cheat sheet (one-sided A4). Covers Kinematics, Dynamics, and Work-Energy.",
                targetDate = System.currentTimeMillis() + 86400000L * 4
            ),
            ReminderEntity(
                reminderId = "rem_2",
                classId = "class_chem",
                type = "Deadline",
                title = "Lab Safety Forms Submission",
                content = "All signed lab safety guidelines and waiver documents must be uploaded into the files drawer by 5 PM.",
                targetDate = System.currentTimeMillis() + 86400000L * 2
            ),
            ReminderEntity(
                reminderId = "rem_3",
                classId = "class_lit",
                type = "Announcement",
                title = "Shakespeare Guest Lecture",
                content = "We will have a virtual session with Prof. Higgins from Oxford on Elizabethan drama. Do not miss it!",
                targetDate = System.currentTimeMillis() + 86400000L * 6
            )
        )
        dao.insertReminders(reminders)
    }
}
