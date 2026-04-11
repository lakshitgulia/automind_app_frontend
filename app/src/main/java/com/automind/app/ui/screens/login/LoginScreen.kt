package com.automind.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automind.app.data.local.UserPreferences
import com.automind.app.ui.components.GradientButton
import com.automind.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userPreferences: UserPreferences,
    onLoginSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(!userPreferences.hasAccount()) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val glowAlpha = 0.45f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Subtle cyan glow behind logo area
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopCenter)
                .offset(y = 60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .blur(60.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo & Title
            Text(
                text = "AutoMind",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = AccentCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "VEHICULAR INTELLIGENCE SYSTEM",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    fontSize = 10.sp
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Section Label
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isSignUp) "CREATE PROFILE" else "ACCESS CREDENTIALS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name field (sign up only)
            if (isSignUp) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("FULL NAME", letterSpacing = 1.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("EMAIL ADDRESS", letterSpacing = 1.sp) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedLabelColor = AccentCyan,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = AccentCyan,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "SECURITY KEY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                if (!isSignUp) {
                    Text(
                        text = "FORGOT?",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyan
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("PASSWORD", letterSpacing = 1.sp) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedLabelColor = AccentCyan,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = AccentCyan,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            // Error message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Authenticate button
            GradientButton(
                text = if (isSignUp) "CREATE ACCOUNT" else "AUTHENTICATE",
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill all fields"
                        return@GradientButton
                    }
                    if (isSignUp) {
                        if (name.isBlank()) {
                            errorMessage = "Please enter your name"
                            return@GradientButton
                        }
                        userPreferences.saveUser(name, email, password)
                        onLoginSuccess()
                    } else {
                        if (userPreferences.login(email, password)) {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Invalid credentials"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle sign up / login
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (isSignUp) "SIGN IN" else "CREATE NEW PROFILE",
                    color = AccentCyan,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { isSignUp = !isSignUp; errorMessage = null }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "ENCRYPTED WITH 256-BIT AES • MONITORING ACTIVE\nCONNECTED TO AUTOMIND v3.0.6",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp,
                    lineHeight = 18.sp
                ),
                color = TextSecondary.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
