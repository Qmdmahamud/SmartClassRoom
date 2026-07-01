package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ClassroomRepository
import com.example.ui.ClassroomViewModel
import com.example.ui.ClassroomViewModelFactory
import com.example.ui.RoleBasedLoginScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Database Initialization
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "classroom_database_v3"
        ).fallbackToDestructiveMigration().build()

        val repository = ClassroomRepository(db.classroomDao())
        val factory = ClassroomViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: ClassroomViewModel = viewModel(factory = factory)
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepCharcoal),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeepCharcoal)
                            .padding(innerPadding)
                    ) {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: ClassroomViewModel) {
    val context = LocalContext.current
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val submissions by viewModel.submissions.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }

    // Reset tabs when changing role
    LaunchedEffect(currentRole) {
        activeTab = 0
    }

    if (loggedInUser == null) {
        RoleBasedLoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { _ -> }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Portal Brand Header and Role Switcher with Logout
            AppHeader(
                currentRole = currentRole,
                loggedInUser = loggedInUser,
                onRoleSelected = { role ->
                    viewModel.switchRole(role)
                    Toast.makeText(context, "Role switched to $role View", Toast.LENGTH_SHORT).show()
                },
                onLogout = {
                    viewModel.logout()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role Specific Workspaces with dynamic layouts
            Box(modifier = Modifier.weight(1f)) {
                when (currentRole) {
                    "Teacher" -> TeacherWorkspace(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        classes = classes,
                        students = students,
                        assignments = assignments,
                        submissions = submissions,
                        reminders = reminders,
                        viewModel = viewModel
                    )
                    "Student" -> StudentWorkspace(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        classes = classes,
                        assignments = assignments,
                        submissions = submissions,
                        reminders = reminders,
                        viewModel = viewModel
                    )
                    "Parent" -> ParentWorkspace(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        classes = classes,
                        students = students,
                        assignments = assignments,
                        submissions = submissions,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ==========================================
// COMMON UI COMPONENTS
// ==========================================

@Composable
fun AppHeader(
    currentRole: String,
    loggedInUser: UserEntity?,
    onRoleSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_header"),
        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
        border = BorderStroke(1.dp, MutedMolybdenum),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${currentRole.uppercase()} WORKSPACE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentRole == "Student") CyanAura else ElectricViolet,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = loggedInUser?.name ?: "Sylhet Polytechnic Institute",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = loggedInUser?.email ?: "Unified Management & Portal Hub",
                        color = TextGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Modern Logout Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlertRed.copy(alpha = 0.15f))
                            .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = AlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepCharcoal)
                            .border(1.dp, MutedMolybdenum, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 18.sp)
                        PulseDot(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            color = CyanAura
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MutedMolybdenum, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Account Role Shortcuts:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val roles = listOf("Teacher", "Student", "Parent")
                roles.forEach { role ->
                    val isSelected = currentRole == role
                    val activeColor = if (role == "Student") CyanAura else ElectricViolet
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) activeColor else DeepCharcoal)
                            .border(1.dp, if (isSelected) activeColor else MutedMolybdenum, RoundedCornerShape(12.dp))
                            .clickable { onRoleSelected(role) }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (role) {
                                "Teacher" -> "Teacher"
                                "Student" -> "Student (Liam)"
                                else -> "Parent (Robert)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) {
                                if (role == "Student") DeepCharcoal else Color.White
                            } else TextGray,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TEACHER WORKSPACE
// ==========================================

@Composable
fun TeacherWorkspace(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    assignments: List<AssignmentEntity>,
    submissions: List<SubmissionEntity>,
    reminders: List<ReminderEntity>,
    viewModel: ClassroomViewModel
) {
    val tabs = listOf("Schedule", "Grading Hub", "Analytics", "Chat Feed")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> TeacherScheduleTab(classes = classes, students = students, viewModel = viewModel)
                1 -> TeacherGradingTab(assignments = assignments, submissions = submissions, viewModel = viewModel)
                2 -> TeacherAnalyticsTab(students = students, submissions = submissions, reminders = reminders, viewModel = viewModel)
                3 -> ChatScreen(viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        WorkspaceBottomNavBar(
            tabs = tabs,
            activeTab = activeTab,
            onTabSelected = onTabSelected,
            activeColor = CyanAura
        )
    }
}

@Composable
fun TeacherScheduleTab(
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    viewModel: ClassroomViewModel
) {
    val activeClassFilter by viewModel.currentClassFilter.collectAsStateWithLifecycle()
    val activeClass = classes.find { it.classId == activeClassFilter }
    val filteredStudents = students.filter { it.enrolledClasses.contains(activeClassFilter) }.sortedBy { it.name }
    val context = LocalContext.current

    var showModifyRoutineDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Class selection filter pills
        item {
            Text(
                text = "Classrooms Filter",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classes.forEach { cls ->
                    val isSelected = activeClassFilter == cls.classId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) CyanAura else MutedMolybdenum)
                            .clickable { viewModel.setClassFilter(cls.classId) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cls.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) DeepCharcoal else OffWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Class Description
        activeClass?.let { cls ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    border = BorderStroke(1.dp, MutedMolybdenum)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cls.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = CyanAura
                            )
                            Button(
                                onClick = { showModifyRoutineDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Modify Routine", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Modify Routine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Teacher: ${cls.teacherName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cls.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OffWhite
                        )
                    }
                }
            }

            // Scheduled Days (Interactive Routine Grid)
            item {
                Text(
                    text = "Interactive Routine Grid",
                    style = MaterialTheme.typography.titleMedium,
                    color = OffWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cls.routine.forEach { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSlate.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MutedMolybdenum),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MutedMolybdenum, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = extractTimeBadge(r.startTime),
                                        color = CyanAura,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.day, style = MaterialTheme.typography.titleMedium, color = OffWhite, fontWeight = FontWeight.Bold)
                                    Text("${r.startTime} - ${r.endTime}", style = MaterialTheme.typography.labelSmall, color = TextGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(r.room, style = MaterialTheme.typography.bodyMedium, color = OffWhite, fontWeight = FontWeight.SemiBold)
                                    Text(r.platform, style = MaterialTheme.typography.labelSmall, color = ElectricViolet, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Roster: Add New Student Panel Card
            item {
                var showAddStudentPanel by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    border = BorderStroke(1.dp, MutedMolybdenum),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Add Student", tint = CyanAura)
                                Text(
                                    text = "Roster: Add New Student",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OffWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { showAddStudentPanel = !showAddStudentPanel },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (showAddStudentPanel) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Panel",
                                    tint = TextGray
                                )
                            }
                        }

                        if (showAddStudentPanel) {
                            Spacer(modifier = Modifier.height(12.dp))
                            var studentName by remember { mutableStateOf("") }
                            var parentEmail by remember { mutableStateOf("") }
                            var rollNumber by remember { mutableStateOf("") }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = studentName,
                                    onValueChange = { studentName = it },
                                    label = { Text("Student Full Name") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAura,
                                        focusedLabelColor = CyanAura,
                                        cursorColor = CyanAura
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = parentEmail,
                                    onValueChange = { parentEmail = it },
                                    label = { Text("Parent Email Address") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAura,
                                        focusedLabelColor = CyanAura,
                                        cursorColor = CyanAura
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = rollNumber,
                                    onValueChange = { rollNumber = it },
                                    label = { Text("Roll / ID Number") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAura,
                                        focusedLabelColor = CyanAura,
                                        cursorColor = CyanAura
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAura),
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = {
                                        if (studentName.isNotBlank() && parentEmail.isNotBlank()) {
                                            viewModel.addNewStudent(studentName, parentEmail, rollNumber, cls.classId)
                                            Toast.makeText(context, "Student '$studentName' added and enrolled!", Toast.LENGTH_SHORT).show()
                                            studentName = ""
                                            parentEmail = ""
                                            rollNumber = ""
                                            showAddStudentPanel = false
                                        } else {
                                            Toast.makeText(context, "Name and Parent Email are required", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("ADD STUDENT TO CLASS", color = DeepCharcoal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Smart Attendance Logger Matrix
            item {
                val totalStudents = filteredStudents.size
                val presentCount = filteredStudents.count { s ->
                    val record = s.attendanceHistory.find { it.date == "2026-07-01" && it.classId == cls.classId }
                    record?.status == "Present"
                }
                val lateCount = filteredStudents.count { s ->
                    val record = s.attendanceHistory.find { it.date == "2026-07-01" && it.classId == cls.classId }
                    record?.status == "Late"
                }
                val absentCount = filteredStudents.count { s ->
                    val record = s.attendanceHistory.find { it.date == "2026-07-01" && it.classId == cls.classId }
                    val status = record?.status ?: "Absent"
                    status == "Absent"
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Attendance Logger (2026-07-01)",
                            style = MaterialTheme.typography.titleMedium,
                            color = OffWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                viewModel.markAllPresent(cls.classId, "2026-07-01")
                                Toast.makeText(context, "All students marked Present!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertGreen.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, AlertGreen),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Mark All Present", tint = AlertGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark All Present", color = AlertGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Counter Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                        border = BorderStroke(1.dp, MutedMolybdenum),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", color = TextGray, style = MaterialTheme.typography.labelSmall)
                                Text("$totalStudents", color = OffWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MutedMolybdenum))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Present", color = AlertGreen, style = MaterialTheme.typography.labelSmall)
                                Text("$presentCount", color = AlertGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MutedMolybdenum))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Late", color = AlertOrange, style = MaterialTheme.typography.labelSmall)
                                Text("$lateCount", color = AlertOrange, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MutedMolybdenum))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Absent", color = AlertRed, style = MaterialTheme.typography.labelSmall)
                                Text("$absentCount", color = AlertRed, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredStudents.isEmpty()) {
                item {
                    Text("No students enrolled in this class.", color = TextGray, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(filteredStudents) { student ->
                    val todayRecord = student.attendanceHistory.find { it.date == "2026-07-01" && it.classId == cls.classId }
                    val status = todayRecord?.status ?: "Absent"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                        border = BorderStroke(1.dp, MutedMolybdenum),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OffWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Parent: ${student.parentEmail}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGray
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            AttendanceSegmentControl(
                                currentStatus = status,
                                onStatusSelected = { newStatus ->
                                    viewModel.setAttendanceDirect(student.studentId, cls.classId, "2026-07-01", newStatus)
                                },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Live Routine CRUD Modal
    if (showModifyRoutineDialog) {
        var editingRoutineItem by remember { mutableStateOf<RoutineItem?>(null) }
        var showAddEditForm by remember { mutableStateOf(false) }

        // Form fields
        var day by remember { mutableStateOf("") }
        var startTime by remember { mutableStateOf("") }
        var endTime by remember { mutableStateOf("") }
        var room by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf("") }
        var link by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showModifyRoutineDialog = false },
            containerColor = CosmicSlate,
            title = {
                Text(
                    text = if (showAddEditForm) {
                        if (editingRoutineItem != null) "Edit Routine Slot" else "Add Routine Slot"
                    } else "Modify Routine",
                    color = CyanAura,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (showAddEditForm) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = day,
                            onValueChange = { day = it },
                            label = { Text("Day of Week (e.g., Monday)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAura,
                                focusedLabelColor = CyanAura,
                                cursorColor = CyanAura
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time (e.g., 09:00 AM)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAura,
                                focusedLabelColor = CyanAura,
                                cursorColor = CyanAura
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time (e.g., 10:30 AM)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAura,
                                focusedLabelColor = CyanAura,
                                cursorColor = CyanAura
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = room,
                            onValueChange = { room = it },
                            label = { Text("Room / Physical Hall") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAura,
                                focusedLabelColor = CyanAura,
                                cursorColor = CyanAura
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = platform,
                            onValueChange = { platform = it },
                            label = { Text("Platform (e.g., Zoom, Google Meet)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAura,
                                focusedLabelColor = CyanAura,
                                cursorColor = CyanAura
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = link,
                            onValueChange = { link = it },
                            label = { Text("Meeting Link") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAura,
                                focusedLabelColor = CyanAura,
                                cursorColor = CyanAura
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    val routineList = activeClass?.routine ?: emptyList()
                    if (routineList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Text("No timeslots in routine.", color = TextGray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            items(routineList) { r ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                                    border = BorderStroke(1.dp, MutedMolybdenum),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${r.day} • ${r.startTime} - ${r.endTime}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text("Room: ${r.room} | ${r.platform}", color = TextGray, style = MaterialTheme.typography.labelSmall)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    editingRoutineItem = r
                                                    day = r.day
                                                    startTime = r.startTime
                                                    endTime = r.endTime
                                                    room = r.room
                                                    platform = r.platform
                                                    link = r.link
                                                    showAddEditForm = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanAura, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    activeClass?.let { cls ->
                                                        viewModel.deleteScheduleRoutine(cls.classId, r)
                                                        Toast.makeText(context, "Timeslot deleted", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (showAddEditForm) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        onClick = {
                            if (day.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                                activeClass?.let { cls ->
                                    val conflictError = viewModel.checkRoutineConflict(
                                        classId = cls.classId,
                                        day = day,
                                        startTime = startTime,
                                        endTime = endTime,
                                        room = room,
                                        excludeItem = editingRoutineItem
                                    )
                                    if (conflictError != null) {
                                        Toast.makeText(context, conflictError, Toast.LENGTH_LONG).show()
                                    } else {
                                        val newItem = RoutineItem(day, startTime, endTime, room, platform, link)
                                        val oldItem = editingRoutineItem
                                        if (oldItem != null) {
                                            viewModel.updateScheduleRoutine(cls.classId, oldItem, newItem)
                                            Toast.makeText(context, "Timeslot updated", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.addScheduleRoutine(cls.classId, newItem)
                                            Toast.makeText(context, "Timeslot added", Toast.LENGTH_SHORT).show()
                                        }
                                        showAddEditForm = false
                                        editingRoutineItem = null
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Day, Start Time and End Time are required", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Save")
                    }
                } else {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAura),
                        onClick = {
                            editingRoutineItem = null
                            day = "Monday"
                            startTime = "10:00 AM"
                            endTime = "11:30 AM"
                            room = "Room 303"
                            platform = "Zoom"
                            link = "https://zoom.us/j/123"
                            showAddEditForm = true
                        }
                    ) {
                        Text("Add New Slot", color = DeepCharcoal, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (showAddEditForm) {
                    TextButton(
                        onClick = {
                            showAddEditForm = false
                            editingRoutineItem = null
                        }
                    ) {
                        Text("Back", color = TextGray)
                    }
                } else {
                    TextButton(onClick = { showModifyRoutineDialog = false }) {
                        Text("Close", color = TextGray)
                    }
                }
            }
        )
    }
}

@Composable
fun TeacherGradingTab(
    assignments: List<AssignmentEntity>,
    submissions: List<SubmissionEntity>,
    viewModel: ClassroomViewModel
) {
    var selectedSubId by remember { mutableStateOf<String?>(null) }
    val selectedSub = submissions.find { it.submissionId == selectedSubId }
    val activeAssignment = selectedSub?.let { sub -> assignments.find { it.assignmentId == sub.assignmentId } }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dual-Pane Layout logic: list of submissions on left
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "Submission Inbox",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(submissions) { sub ->
                    val assign = assignments.find { it.assignmentId == sub.assignmentId }
                    val isSelected = selectedSubId == sub.submissionId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSubId = sub.submissionId },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MutedMolybdenum else CosmicSlate
                        ),
                        border = BorderStroke(1.dp, if (isSelected) CyanAura else MutedMolybdenum)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sub.studentName, style = MaterialTheme.typography.titleMedium, color = OffWhite)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (sub.status == "Graded") AlertGreen else AlertOrange)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sub.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Task: ${assign?.title ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyanAura,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (sub.status == "Graded") {
                                Text(
                                    text = "Score: ${sub.pointsScored} / ${assign?.maxPoints ?: 100}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right-Hand Grading Pane (Detail View)
        Card(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CosmicSlate),
            border = BorderStroke(1.dp, MutedMolybdenum)
        ) {
            if (selectedSub == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, contentDescription = "Grading Hub", tint = TextGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Select a Student Submission",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextGray
                        )
                        Text(
                            text = "to initiate academic feedback.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                val gradeScore = remember(selectedSubId) { mutableStateOf(selectedSub.pointsScored.toFloat()) }
                var feedbackText by remember(selectedSubId) { mutableStateOf(selectedSub.teacherFeedback) }
                val maxPoints = activeAssignment?.maxPoints ?: 100

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Grading Desk",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedSub.studentName,
                        style = MaterialTheme.typography.titleLarge,
                        color = OffWhite
                    )
                    Text(
                        text = "Assignment: ${activeAssignment?.title ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyanAura
                    )
                    Divider(color = MutedMolybdenum)

                    Text(
                        text = "Student Submission text content:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepCharcoal, RoundedCornerShape(8.dp))
                            .border(1.dp, MutedMolybdenum, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = selectedSub.contentText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OffWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grading slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Score Allocated:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OffWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${gradeScore.value.toInt()} / $maxPoints pts",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyanAura,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = gradeScore.value,
                        onValueChange = { gradeScore.value = it },
                        valueRange = 0f..maxPoints.toFloat(),
                        steps = maxPoints - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAura,
                            activeTrackColor = ElectricViolet,
                            inactiveTrackColor = MutedMolybdenum
                        )
                    )

                    // Feedback text area
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        label = { Text("Teacher Evaluation Commentary") },
                        placeholder = { Text("Add detailed evaluation reviews here...") }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        onClick = {
                            viewModel.gradeSubmission(
                                selectedSub.submissionId,
                                gradeScore.value.toInt(),
                                feedbackText
                            )
                            selectedSubId = null // clear grading desks
                        }
                    ) {
                        Text("Record Academic Grade", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAnalyticsTab(
    students: List<StudentEntity>,
    submissions: List<SubmissionEntity>,
    reminders: List<ReminderEntity>,
    viewModel: ClassroomViewModel
) {
    val context = LocalContext.current
    val activeClassFilter by viewModel.currentClassFilter.collectAsStateWithLifecycle()
    val filteredStudents = students.filter { it.enrolledClasses.contains(activeClassFilter) }

    // Calculate Average Attendance
    val averageAttendance = if (filteredStudents.isNotEmpty()) {
        val rates = filteredStudents.map { s ->
            val records = s.attendanceHistory.filter { it.classId == activeClassFilter }
            if (records.isNotEmpty()) {
                val presents = records.count { it.status == "Present" }
                val lates = records.count { it.status == "Late" }
                (presents + lates * 0.7f) / records.size * 100f
            } else 100f
        }
        rates.average().toFloat()
    } else 0f

    val studentScores = filteredStudents.map { s -> s.name to s.engagementScore }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Performance Diagnostics",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Charts visual matrix row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radial Attendance gauge card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    border = BorderStroke(1.dp, MutedMolybdenum),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Average Attendance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RadialAttendanceGauge(attendanceRate = averageAttendance)
                    }
                }

                // Grade curve chart
                Box(modifier = Modifier.weight(1.2f)) {
                    GradeCurveChart()
                }
            }
        }

        // Engagement Scores Bar Chart
        if (studentScores.isNotEmpty()) {
            item {
                EngagementBarChart(studentScores = studentScores)
            }
        }

        // Student Detailed Directory with parent quick contact trigger
        item {
            Text(
                text = "Academic Student Roster & Parent Triggers",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (filteredStudents.isEmpty()) {
            item {
                Text("No students in this roster.", color = TextGray)
            }
        } else {
            items(filteredStudents) { student ->
                val records = student.attendanceHistory.filter { it.classId == activeClassFilter }
                val countP = records.count { it.status == "Present" }
                val rate = if (records.isNotEmpty()) (countP * 100 / records.size) else 100

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    border = BorderStroke(1.dp, MutedMolybdenum)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(student.name, style = MaterialTheme.typography.titleMedium, color = OffWhite)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Attendance: $rate%", style = MaterialTheme.typography.labelSmall, color = CyanAura)
                                Text("Engagement Index: ${student.engagementScore}", style = MaterialTheme.typography.labelSmall, color = ElectricViolet)
                            }
                            Text("Guardian: ${student.parentEmail}", style = MaterialTheme.typography.labelSmall, color = TextGray)
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MutedMolybdenum),
                            onClick = {
                                viewModel.setChatRoleFilter("Parent")
                                Toast.makeText(context, "Direct pathway open for ${student.name}'s parent Robert.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "Ping Parent", tint = CyanAura, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contact", color = OffWhite, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STUDENT WORKSPACE
// ==========================================

@Composable
fun StudentWorkspace(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    classes: List<ClassEntity>,
    assignments: List<AssignmentEntity>,
    submissions: List<SubmissionEntity>,
    reminders: List<ReminderEntity>,
    viewModel: ClassroomViewModel
) {
    val tabs = listOf("Live Launchpad", "Homework Vault", "Classroom Chat")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> StudentLaunchpadTab(classes = classes, reminders = reminders)
                1 -> StudentHomeworkTab(assignments = assignments, submissions = submissions, viewModel = viewModel)
                2 -> ChatScreen(viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        WorkspaceBottomNavBar(
            tabs = tabs,
            activeTab = activeTab,
            onTabSelected = onTabSelected,
            activeColor = ElectricViolet
        )
    }
}

@Composable
fun StudentLaunchpadTab(
    classes: List<ClassEntity>,
    reminders: List<ReminderEntity>
) {
    val context = LocalContext.current
    // Liam is enrolled in class_physics and class_chem
    val liamClasses = classes.filter { it.classId == "class_physics" || it.classId == "class_chem" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Synchronized Live Launchpad",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Daily Classes list
        item {
            var isFirst = true
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                liamClasses.forEach { cls ->
                    cls.routine.forEach { r ->
                        if (isFirst) {
                            isFirst = false
                            // Render the gorgeous Live Class Hero Card matching the design HTML
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                                border = BorderStroke(2.dp, CyanAura),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            PulseDot(color = CyanAura)
                                            Text(
                                                text = "LIVE SESSION",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyanAura,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ElectricViolet.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(r.platform, style = MaterialTheme.typography.labelSmall, color = CyanAura)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = cls.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = 22.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${r.day} | ${r.startTime} - ${r.endTime}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextGray
                                    )
                                    Text(
                                        text = "Room: ${r.room}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextGray.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OverlappingAvatars()
                                        Text(
                                            text = "Active Now",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextGray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanAura),
                                        shape = RoundedCornerShape(12.dp),
                                        onClick = {
                                            if (r.link.isNotBlank()) {
                                                try {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(r.link))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Redirecting deep-link to ${r.platform}...", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "No meeting link configured for this routine slot.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Text("JOIN CLASS NOW", color = DeepCharcoal, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = "Enter", tint = DeepCharcoal, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        } else {
                            // Render upcoming routine list items styled like the HTML Upcoming List
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSlate.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MutedMolybdenum),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(MutedMolybdenum, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = extractTimeBadge(r.startTime),
                                            color = CyanAura,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cls.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${r.day} • ${r.startTime} - ${r.endTime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextGray
                                        )
                                    }
                                    Button(
                                        colors = ButtonDefaults.buttonColors(containerColor = MutedMolybdenum),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp),
                                        onClick = {
                                            Toast.makeText(context, "Class scheduled at Room ${r.room}", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("VIEW", color = OffWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Student Alerts notification timeline
        item {
            Text(
                text = "Urgent Classroom Alerts",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(reminders) { rem ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlate.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MutedMolybdenum)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                when (rem.type) {
                                    "Exam" -> AlertRed
                                    "Deadline" -> AlertOrange
                                    else -> CyanAura
                                },
                                RoundedCornerShape(50)
                            )
                    )
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rem.title, style = MaterialTheme.typography.titleMedium, color = OffWhite)
                            Text(
                                text = rem.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(rem.content, style = MaterialTheme.typography.bodyMedium, color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentHomeworkTab(
    assignments: List<AssignmentEntity>,
    submissions: List<SubmissionEntity>,
    viewModel: ClassroomViewModel
) {
    // Show only Liam's classes assignments (Physics: class_physics, Chemistry: class_chem)
    val liamAssignments = assignments.filter { it.classId == "class_physics" || it.classId == "class_chem" }

    var selectedAssignId by remember { mutableStateOf<String?>(null) }
    val selectedAssignment = assignments.find { it.assignmentId == selectedAssignId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Assignments Drawer",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(liamAssignments) { assign ->
            val liamSub = submissions.find { it.assignmentId == assign.assignmentId && it.studentId == "student_liam" }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                border = BorderStroke(1.dp, MutedMolybdenum)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(assign.title, style = MaterialTheme.typography.titleMedium, color = OffWhite)
                            Text(
                                text = "Course: ${assign.classId.replace("class_", "").uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray
                            )
                        }
                        // Status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (liamSub?.status) {
                                        "Graded" -> AlertGreen
                                        "Submitted" -> AlertOrange
                                        "Late" -> AlertRed
                                        else -> MutedMolybdenum
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = liamSub?.status ?: "Not Submitted",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(assign.description, style = MaterialTheme.typography.bodyMedium, color = OffWhite)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (liamSub != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DeepCharcoal, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("My Submission content:", style = MaterialTheme.typography.labelSmall, color = TextGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(liamSub.contentText, style = MaterialTheme.typography.bodyMedium, color = OffWhite, fontFamily = FontFamily.Monospace)

                                if (liamSub.status == "Graded") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = MutedMolybdenum)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Feedback evaluation:", style = MaterialTheme.typography.labelSmall, color = CyanAura, fontWeight = FontWeight.Bold)
                                    Text("Grade: ${liamSub.pointsScored} / ${assign.maxPoints}", style = MaterialTheme.typography.titleMedium, color = OffWhite)
                                    Text(liamSub.teacherFeedback, style = MaterialTheme.typography.bodyMedium, color = TextGray)
                                }
                            }
                        }
                    } else {
                        Button(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            onClick = { selectedAssignId = assign.assignmentId }
                        ) {
                            Text("Prepare Submission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Homework Submission Panel
    if (selectedAssignment != null) {
        var submissionText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedAssignId = null },
            containerColor = CosmicSlate,
            title = { Text("Submit Homework", color = CyanAura) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(selectedAssignment.title, style = MaterialTheme.typography.titleMedium, color = OffWhite)
                    Text("Paste or type your assignment essay/lab report calculations below.", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                    OutlinedTextField(
                        value = submissionText,
                        onValueChange = { submissionText = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        label = { Text("Homework Content") }
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    onClick = {
                        viewModel.submitHomework(
                            selectedAssignment.assignmentId,
                            "student_liam",
                            "Liam Carter",
                            submissionText
                        )
                        selectedAssignId = null
                    }
                ) {
                    Text("Upload Submission")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAssignId = null }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}

// ==========================================
// PARENT WORKSPACE
// ==========================================

@Composable
fun ParentWorkspace(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    classes: List<ClassEntity>,
    students: List<StudentEntity>,
    assignments: List<AssignmentEntity>,
    submissions: List<SubmissionEntity>,
    viewModel: ClassroomViewModel
) {
    val tabs = listOf("Overview & Grades", "Quick-Ping Teacher", "Global Feed")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> ParentOverviewTab(students = students, assignments = assignments, submissions = submissions)
                1 -> ParentPingTab(viewModel = viewModel)
                2 -> ChatScreen(viewModel = viewModel)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        WorkspaceBottomNavBar(
            tabs = tabs,
            activeTab = activeTab,
            onTabSelected = onTabSelected,
            activeColor = ElectricViolet
        )
    }
}

@Composable
fun ParentOverviewTab(
    students: List<StudentEntity>,
    assignments: List<AssignmentEntity>,
    submissions: List<SubmissionEntity>
) {
    // Robert Carter is Liam Carter's parent
    val liam = students.find { it.studentId == "student_liam" } ?: return
    val physicsRecord = liam.attendanceHistory.filter { it.classId == "class_physics" }
    val totalPhysics = physicsRecord.size
    val ratePhysics = if (totalPhysics > 0) (physicsRecord.count { it.status == "Present" } * 100 / totalPhysics) else 100

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Liam's Academic Attendance Ring",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    border = BorderStroke(1.dp, MutedMolybdenum)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Class Attendance Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RadialAttendanceGauge(attendanceRate = ratePhysics.toFloat())
                    }
                }

                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    border = BorderStroke(1.dp, MutedMolybdenum)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Class Performance Indicators", style = MaterialTheme.typography.bodyMedium, color = TextGray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Engagement score:", style = MaterialTheme.typography.bodyMedium, color = OffWhite)
                            Text("${liam.engagementScore} / 100", style = MaterialTheme.typography.titleMedium, color = CyanAura, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Enrolled Classrooms:", style = MaterialTheme.typography.bodyMedium, color = OffWhite)
                            Text("${liam.enrolledClasses.size} active", style = MaterialTheme.typography.bodyMedium, color = ElectricViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Academic Grade Sheets & Feedbacks",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Display Liam's graded papers
        val liamSubmissions = submissions.filter { it.studentId == "student_liam" && it.status == "Graded" }
        if (liamSubmissions.isEmpty()) {
            item {
                Text("No grade reports recorded yet.", color = TextGray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(liamSubmissions) { sub ->
                val assign = assignments.find { it.assignmentId == sub.assignmentId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                    border = BorderStroke(1.dp, MutedMolybdenum)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(assign?.title ?: "N/A", style = MaterialTheme.typography.titleMedium, color = OffWhite)
                            Text(
                                text = "Grade Score: ${sub.pointsScored} / ${assign?.maxPoints ?: 100}",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyanAura,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Teacher Commentaries:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"${sub.teacherFeedback}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentPingTab(viewModel: ClassroomViewModel) {
    val context = LocalContext.current
    var textMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Direct Quick-Ping Pathway",
            style = MaterialTheme.typography.titleLarge,
            color = CyanAura
        )
        Text(
            text = "Send a direct message specifically routed to your child's teachers. Responses will sync instantly in the Global Chat Portal.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicSlate),
            border = BorderStroke(1.dp, MutedMolybdenum)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Recipient: Dr. Elizabeth Vance (AP Physics)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OffWhite,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    label = { Text("Query details") },
                    placeholder = { Text("Type any message regarding schedules, syllabus, or grades...") }
                )

                Button(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    onClick = {
                        viewModel.sendMessage(textMessage)
                        Toast.makeText(context, "Direct message dispatched to Dr. Vance!", Toast.LENGTH_SHORT).show()
                        textMessage = ""
                    }
                ) {
                    Text("Dispatched Quick Ping", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// MESSAGING FEED (REAL-TIME MESSAGE CHAT)
// ==========================================

@Composable
fun ChatScreen(viewModel: ClassroomViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val chatFilter by viewModel.chatRoleFilter.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var textInput by remember { mutableStateOf("") }

    val filteredMessages = when (chatFilter) {
        "Teacher" -> messages.filter { it.senderRole == "Teacher" }
        "Parent" -> messages.filter { it.senderRole == "Parent" }
        "Student" -> messages.filter { it.senderRole == "Student" }
        else -> messages
    }

    // Scroll to bottom on load or new message
    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(filteredMessages.size - 1)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat filter chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Teacher", "Parent", "Student").forEach { role ->
                val isSelected = chatFilter == role
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) ElectricViolet else MutedMolybdenum)
                        .clickable { viewModel.setChatRoleFilter(role) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (role == "All") "All Feeds" else "${role}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat lists with virtualization
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(DeepCharcoal, RoundedCornerShape(12.dp))
                .border(1.dp, MutedMolybdenum, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No discussions active in this role channel.", color = TextGray)
                    }
                }
            } else {
                items(filteredMessages) { msg ->
                    val isTeacher = msg.senderRole == "Teacher"
                    val isParent = msg.senderRole == "Parent"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isTeacher) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            modifier = Modifier.widthIn(max = 280.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTeacher) CosmicSlate else MutedMolybdenum
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isTeacher) ElectricViolet else if (isParent) CyanAura else BorderGray
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isTeacher) ElectricViolet else if (isParent) CyanAura else TextGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = msg.senderRole.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextGray.copy(alpha = 0.5f),
                                        fontSize = 8.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OffWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Text input row with minimum 48dp target action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Write classroom announcement...", color = TextGray) },
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color.White)
            }
        }
    }
}

// ==========================================
// HIGH PERFORMANCE CUSTOM CANVAS CHARTS
// ==========================================

@Composable
fun RadialAttendanceGauge(attendanceRate: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            // Track base arc
            drawArc(
                color = MutedMolybdenum,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = CyanAura,
                startAngle = 140f,
                sweepAngle = 260f * (attendanceRate.coerceIn(0f, 100f) / 100f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${attendanceRate.toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = CyanAura,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Present Rate",
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun EngagementBarChart(studentScores: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
        border = BorderStroke(1.dp, MutedMolybdenum),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Class Engagement Indices",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                studentScores.forEach { (name, score) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(14.dp)
                                    .background(MutedMolybdenum, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(score / 100f)
                                    .width(14.dp)
                                    .background(ElectricViolet, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name.split(" ").firstOrNull() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GradeCurveChart(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSlate),
        border = BorderStroke(1.dp, MutedMolybdenum),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Aggregate Grade Curves",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val path = Path()
                val width = size.width
                val height = size.height

                path.moveTo(0f, height * 0.9f)
                path.cubicTo(
                    width * 0.25f, height * 0.8f,
                    width * 0.4f, height * 0.15f,
                    width * 0.5f, height * 0.15f
                )
                path.cubicTo(
                    width * 0.6f, height * 0.15f,
                    width * 0.75f, height * 0.8f,
                    width * 1.0f, height * 0.95f
                )

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(CyanAura.copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = CyanAura,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                drawLine(
                    color = MutedMolybdenum,
                    start = Offset(0f, height * 0.5f),
                    end = Offset(width, height * 0.5f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("F", style = MaterialTheme.typography.labelSmall, color = TextGray)
                Text("D", style = MaterialTheme.typography.labelSmall, color = TextGray)
                Text("C", style = MaterialTheme.typography.labelSmall, color = TextGray)
                Text("B", style = MaterialTheme.typography.labelSmall, color = TextGray)
                Text("A", style = MaterialTheme.typography.labelSmall, color = TextGray)
            }
        }
    }
}

// ==========================================
// SLEEK THEME HELPER COMPONENTS & UTILITIES
// ==========================================

@Composable
fun PulseDot(modifier: Modifier = Modifier, color: Color = CyanAura) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(color, RoundedCornerShape(50))
    )
}

@Composable
fun OverlappingAvatars() {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-10).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("AL", "JD", "MC").forEachIndexed { i, initials ->
            val bg = when(i) {
                0 -> MutedMolybdenum
                1 -> ElectricViolet
                else -> CyanAura
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(1.5.dp, CosmicSlate, RoundedCornerShape(50))
                    .background(bg, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = if (i == 2) DeepCharcoal else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(1.5.dp, CosmicSlate, RoundedCornerShape(50))
                .background(MutedMolybdenum, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+24",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun extractTimeBadge(timeStr: String): String {
    return try {
        val clean = timeStr.trim().uppercase()
        val hour = clean.split(":").firstOrNull() ?: "12"
        val ampm = if (clean.contains("PM")) "PM" else "AM"
        val cleanHour = hour.replace(Regex("[^0-9]"), "")
        "$cleanHour$ampm"
    } catch (e: Exception) {
        "12PM"
    }
}

@Composable
fun AttendanceSegmentControl(
    currentStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DeepCharcoal)
            .border(1.dp, MutedMolybdenum, RoundedCornerShape(12.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf("Present", "Late", "Absent").forEach { status ->
            val isSelected = currentStatus == status
            val activeBg = when (status) {
                "Present" -> AlertGreen
                "Late" -> AlertOrange
                else -> AlertRed
            }
            val activeTextColor = Color.White
            val inactiveTextColor = TextGray

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "status_scale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) activeBg else Color.Transparent)
                    .clickable { onStatusSelected(status) }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(status) {
                        "Present" -> "Pres"
                        "Late" -> "Late"
                        else -> "Abs"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeTextColor else inactiveTextColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun WorkspaceBottomNavBar(
    tabs: List<String>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    activeColor: Color = ElectricViolet
) {
    Surface(
        color = CosmicSlate,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, MutedMolybdenum),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                val icon = when (title) {
                    "Schedule", "Live Launchpad" -> Icons.Default.Schedule
                    "Grading Hub", "Homework Vault" -> Icons.Default.Assignment
                    "Analytics", "Overview & Grades" -> Icons.Default.Analytics
                    "Chat Feed", "Classroom Chat", "Quick-Ping Teacher", "Global Feed" -> Icons.Default.Chat
                    else -> Icons.Default.Home
                }

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "tab_scale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) activeColor else TextGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) activeColor else TextGray,
                        maxLines = 1,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
