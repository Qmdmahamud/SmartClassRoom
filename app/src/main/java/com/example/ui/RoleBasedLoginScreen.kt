package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleBasedLoginScreen(
    viewModel: ClassroomViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }

    // Form inputs
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") } // "teacher", "student", "parent"
    var showPassword by remember { mutableStateOf(false) }

    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(ElectricViolet, CyanAura)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Identity with Gradient Logo / Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(gradientBrush)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeepCharcoal)
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ω",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAura
                        )
                    }
                }

                Text(
                    text = "SYLHET POLYTECHNIC INSTITUTE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Unified Management & Portal Hub",
                    fontSize = 13.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }

            // Beautiful Glassmorphic Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                border = BorderStroke(1.5.dp, MutedMolybdenum)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Segmented Switch Pill (Sign In / Register)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DeepCharcoal)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isSignUpMode) ElectricViolet else Color.Transparent)
                                .clickable { isSignUpMode = false }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                color = if (!isSignUpMode) Color.White else TextGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSignUpMode) ElectricViolet else Color.Transparent)
                                .clickable { isSignUpMode = true }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Register",
                                color = if (isSignUpMode) Color.White else TextGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Form Header Text
                    Text(
                        text = if (isSignUpMode) "Create Account" else "Welcome Back",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Register-only fields
                    AnimatedVisibility(
                        visible = isSignUpMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Full Name Input
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("register_name"),
                                label = { Text("Full Name", color = TextGray) },
                                placeholder = { Text("e.g. Liam Carter", color = TextGray.copy(alpha = 0.5f)) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon", tint = CyanAura) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAura,
                                    unfocusedBorderColor = MutedMolybdenum,
                                    focusedContainerColor = DeepCharcoal,
                                    unfocusedContainerColor = DeepCharcoal,
                                    focusedLabelColor = CyanAura,
                                    unfocusedLabelColor = TextGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            // Role Selection Segmented Row
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Select Account Role",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val roles = listOf("teacher", "student", "parent")
                                    roles.forEach { r ->
                                        val isSelected = selectedRole == r
                                        val label = r.replaceFirstChar { it.uppercase() }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) MutedMolybdenum else DeepCharcoal)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) CyanAura else MutedMolybdenum,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { selectedRole = r }
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) CyanAura else TextGray,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Email Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("login_email"),
                        label = { Text("Email Address", color = TextGray) },
                        placeholder = { Text("e.g. teacher@example.com", color = TextGray.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = CyanAura) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAura,
                            unfocusedBorderColor = MutedMolybdenum,
                            focusedContainerColor = DeepCharcoal,
                            unfocusedContainerColor = DeepCharcoal,
                            focusedLabelColor = CyanAura,
                            unfocusedLabelColor = TextGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("login_password"),
                        label = { Text("Password", color = TextGray) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = CyanAura) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextGray
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAura,
                            unfocusedBorderColor = MutedMolybdenum,
                            focusedContainerColor = DeepCharcoal,
                            unfocusedContainerColor = DeepCharcoal,
                            focusedLabelColor = CyanAura,
                            unfocusedLabelColor = TextGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Error Message display
                    loginError?.let { err ->
                        Text(
                            text = err,
                            color = AlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ACTION BUTTON (Sign In / Register)
                    Button(
                        onClick = {
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                Toast.makeText(context, "Please fill in all credentials", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isSignUpMode) {
                                if (nameInput.isBlank()) {
                                    Toast.makeText(context, "Name is required for registration", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.register(
                                    name = nameInput,
                                    email = emailInput,
                                    password = passwordInput,
                                    role = selectedRole
                                ) {
                                    Toast.makeText(context, "Registration successful! Signing in...", Toast.LENGTH_SHORT).show()
                                    viewModel.login(emailInput, passwordInput) { role ->
                                        onLoginSuccess(role)
                                    }
                                }
                            } else {
                                viewModel.login(emailInput, passwordInput) { role ->
                                    Toast.makeText(context, "Authenticated successfully as $role", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(role)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoggingIn
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUpMode) "CREATE ACCOUNT" else "SECURE SIGN IN",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Demo Accounts Bypass Helper Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSlate.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, MutedMolybdenum)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🔐 Developer Account Sandbox",
                        color = CyanAura,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any pre-seeded profile below to instantly populate credentials and securely authenticate.",
                        color = TextGray,
                        fontSize = 11.sp
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val accounts = listOf(
                            Triple("teacher@example.com", "Teacher Hub", "teacher"),
                            Triple("student@example.com", "Student Hub", "student"),
                            Triple("parent@example.com", "Parent Portal", "parent")
                        )

                        accounts.forEach { (email, label, role) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepCharcoal)
                                    .border(1.dp, MutedMolybdenum, RoundedCornerShape(8.dp))
                                    .clickable {
                                        emailInput = email
                                        passwordInput = "password123"
                                        selectedRole = role
                                        isSignUpMode = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(email, color = TextGray, fontSize = 10.sp)
                                }
                                Text("Select", color = CyanAura, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
