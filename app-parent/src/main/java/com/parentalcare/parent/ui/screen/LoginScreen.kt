package com.parentalcare.parent.ui.screen


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parentalcare.parent.R
import com.parentalcare.core.design.theme.SharedColors
import timber.log.Timber

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel(), onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val googleSignInClient = remember { getGoogleSignInClient(context) }
    val errorMsg by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken, onLoginSuccess)
            } else {
                Timber.e("Google Sign In failed: idToken is null")
                // Using reflection or just calling the viewModel to set error
                // but wait, viewModel doesn't have setError yet? I just added it.
                viewModel.setError("Google Sign In failed. Your Firebase project might be missing a Web Client ID.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Google Sign In Intent failed")
            viewModel.setError("Google authentication failed. Please configure Google Sign-In in Firebase/Supabase.\nError: ${e.message}")
        }
    }
    
    if (errorMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(errorMsg!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) { Text("OK") }
            },
            containerColor = SharedColors.DarkSurface,
            titleContentColor = SharedColors.DarkTextPrimary,
            textContentColor = SharedColors.DarkTextSecondary
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SharedColors.DarkBg)
                .padding(horizontal = 24.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(8.dp, CircleShape)
                    .background(SharedColors.ParentPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Outlined.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSignUpMode) "Create Account" else stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = SharedColors.DarkTextPrimary,
            )
            Text(
                text = if (isSignUpMode) "Sign up to start monitoring" else stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SharedColors.DarkTextSecondary,
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Email
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.login_email).uppercase(), style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextSecondary)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = SharedColors.DarkTextTertiary) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = SharedColors.DarkTextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SharedColors.DarkBorder,
                        focusedBorderColor = SharedColors.ParentPrimary,
                        cursorColor = SharedColors.ParentPrimary,
                        unfocusedContainerColor = SharedColors.DarkSurface,
                        focusedContainerColor = SharedColors.DarkSurface,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Password
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.login_password).uppercase(), style = MaterialTheme.typography.labelMedium, color = SharedColors.DarkTextSecondary)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = SharedColors.DarkTextTertiary) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = SharedColors.DarkTextTertiary,
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = SharedColors.DarkTextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SharedColors.DarkBorder,
                        focusedBorderColor = SharedColors.ParentPrimary,
                        cursorColor = SharedColors.ParentPrimary,
                        unfocusedContainerColor = SharedColors.DarkSurface,
                        focusedContainerColor = SharedColors.DarkSurface,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!isSignUpMode) {
                    TextButton(onClick = {}) {
                        Text(stringResource(R.string.login_forgot), color = SharedColors.ParentPrimary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (isSignUpMode) {
                        viewModel.signUp(email, password, onLoginSuccess)
                    } else {
                        viewModel.login(email, password, onLoginSuccess)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharedColors.ParentPrimary,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(8.dp, RoundedCornerShape(10.dp)),
            ) {
                Text(if (isSignUpMode) "Sign Up" else stringResource(R.string.login_cta), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Divider
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = SharedColors.DarkBorder, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.login_or), style = MaterialTheme.typography.labelSmall, color = SharedColors.DarkTextTertiary, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(color = SharedColors.DarkBorder, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    googleAuthLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SharedColors.DarkBorder),
            ) {
                Text("Continue with Google", color = Color.White, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isSignUpMode) "Already have an account? " else "Don't have an account? ", style = MaterialTheme.typography.bodySmall, color = SharedColors.DarkTextSecondary)
                TextButton(onClick = { isSignUpMode = !isSignUpMode }) { Text(if (isSignUpMode) "Login" else "Sign up", color = SharedColors.ParentPrimary, style = MaterialTheme.typography.labelLarge) }
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SharedColors.ParentPrimary)
            }
        }
    }
}
