package com.example.contact.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.contactsapp.model.Contact
import com.example.contact.ui.components.loadImageBitmapFromUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

private const val MAX_PHONE_LENGTH = 15

private fun sanitizePhoneInput(input: String): String =
    input.filter { it.isDigit() }.take(MAX_PHONE_LENGTH)

private fun isValidDotComEmail(email: String): Boolean {
    val trimmed = email.trim()
    if (trimmed.isEmpty()) return true
    return trimmed.endsWith(".com", ignoreCase = true) &&
            trimmed.contains("@") &&
            trimmed.substringAfter("@").length > 4
}

private fun formatMillisToDateString(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    existingContact: Contact?,
    onCancel: () -> Unit,
    onSave: (Contact) -> Unit
) {
    val context = LocalContext.current
    val tealPrimary = Color(0xFF00695C)

    var name by remember { mutableStateOf(existingContact?.name ?: "") }
    var company by remember { mutableStateOf(existingContact?.company ?: "") }
    var phone by remember {
        mutableStateOf(sanitizePhoneInput(existingContact?.phone ?: ""))
    }
    var secondaryPhone by remember {
        mutableStateOf(sanitizePhoneInput(existingContact?.secondaryPhone ?: ""))
    }
    var email by remember { mutableStateOf(existingContact?.email ?: "") }
    var address by remember { mutableStateOf(existingContact?.address ?: "") }
    var notes by remember { mutableStateOf(existingContact?.notes ?: "") }
    var avatarUri by remember { mutableStateOf(existingContact?.avatarUri) }

    // Extra fields pre-populated from existingContact and revealed under the More button
    var birthday by remember { mutableStateOf(existingContact?.birthday ?: "") }
    var nickname by remember { mutableStateOf(existingContact?.nickname ?: "") }
    var workAddress by remember { mutableStateOf(existingContact?.workAddress ?: "") }
    var assistant by remember { mutableStateOf(existingContact?.assistant ?: "") }

    var showMoreFields by remember { mutableStateOf(false) }
    var showBirthdayCalendarDialog by remember { mutableStateOf(false) }
    var showEmailError by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            avatarUri = it.toString()
        }
    }

    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { avatarUri = it.toString() }
    }

    fun launchImagePicker() {
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            legacyPickerLauncher.launch("image/*")
        }
    }

    val avatarBitmap = remember(avatarUri) {
        loadImageBitmapFromUri(context, avatarUri)
    }

    val emailValid = isValidDotComEmail(email)
    val canSave = name.isNotBlank() && emailValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingContact == null) "New Contact" else "Edit Contact",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", color = Color.DarkGray)
                    }
                    Button(
                        onClick = {
                            if (!emailValid) {
                                showEmailError = true
                                return@Button
                            }
                            if (name.isNotBlank()) {
                                onSave(
                                    Contact(
                                        id = existingContact?.id ?: UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        title = existingContact?.title ?: "",
                                        company = company.trim(),
                                        phone = phone,
                                        secondaryPhone = secondaryPhone,
                                        email = email.trim(),
                                        address = address.trim(),
                                        workAddress = workAddress.trim(),
                                        nickname = nickname.trim(),
                                        birthday = birthday.trim(),
                                        assistant = assistant.trim(),
                                        notes = notes.trim(),
                                        group = existingContact?.group ?: "All",
                                        isFavorite = existingContact?.isFavorite ?: false,
                                        avatarColor = existingContact?.avatarColor ?: 0xFF006A6A,
                                        avatarUri = avatarUri
                                    )
                                )
                            }
                        },
                        enabled = canSave,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Tappable Profile Image Picker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { launchImagePicker() }
            ) {
                Box(modifier = Modifier.size(92.dp)) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2F1))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = "Contact avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (name.isNotBlank()) {
                            Text(
                                text = name.trim().take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = tealPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = tealPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    FilledIconButton(
                        onClick = { launchImagePicker() },
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = tealPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "Add Image",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (avatarBitmap != null) "Change Photo" else "Add Photo",
                        fontWeight = FontWeight.SemiBold,
                        color = tealPrimary
                    )
                    Text(
                        text = "Choose from gallery or storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val fieldModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )

            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Company") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = sanitizePhoneInput(it) },
                label = { Text("Phone Number 1") },
                supportingText = { Text("${phone.length}/$MAX_PHONE_LENGTH (digits only)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )

            OutlinedTextField(
                value = secondaryPhone,
                onValueChange = { secondaryPhone = sanitizePhoneInput(it) },
                label = { Text("Phone Number 2") },
                supportingText = { Text("${secondaryPhone.length}/$MAX_PHONE_LENGTH (digits only)") },
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )

            val hasEmailError = (email.isNotBlank() && !emailValid) || (showEmailError && !emailValid)
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    showEmailError = false
                },
                label = { Text("Email") },
                isError = hasEmailError,
                supportingText = {
                    if (hasEmailError) {
                        Text("Email must end with .com (e.g. name@example.com)")
                    } else {
                        Text("Must end with .com")
                    }
                },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )

            // "More" button directly under Notes
            TextButton(
                onClick = { showMoreFields = !showMoreFields },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (showMoreFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = tealPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showMoreFields) "Less" else "More",
                    color = tealPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Pre-populated extra inputs appearing right below the More button
            AnimatedVisibility(
                visible = showMoreFields,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Birthday in Calendar Mode
                    Box(modifier = fieldModifier) {
                        OutlinedTextField(
                            value = birthday,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Birthday") },
                            placeholder = { Text("Select date from calendar") },
                            leadingIcon = {
                                Icon(Icons.Default.Cake, contentDescription = null)
                            },
                            trailingIcon = {
                                Row {
                                    if (birthday.isNotBlank()) {
                                        IconButton(onClick = { birthday = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear birthday")
                                        }
                                    }
                                    IconButton(onClick = { showBirthdayCalendarDialog = true }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = "Open calendar")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(end = 48.dp)
                                .clickable { showBirthdayCalendarDialog = true }
                        )
                    }

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = fieldModifier
                    )

                    OutlinedTextField(
                        value = workAddress,
                        onValueChange = { workAddress = it },
                        label = { Text("Work Address") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = fieldModifier
                    )

                    OutlinedTextField(
                        value = assistant,
                        onValueChange = { assistant = it },
                        label = { Text("Assistant") },
                        leadingIcon = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = fieldModifier
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Calendar DatePickerDialog for Birthday
    if (showBirthdayCalendarDialog) {
        DatePickerDialog(
            onDismissRequest = { showBirthdayCalendarDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            birthday = formatMillisToDateString(millis)
                        }
                        showBirthdayCalendarDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthdayCalendarDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}