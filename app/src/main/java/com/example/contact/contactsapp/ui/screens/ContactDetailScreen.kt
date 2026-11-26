package com.example.contactsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contactsapp.model.Contact
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.contact.ui.components.generateQrCodeBitmap
import com.example.contact.ui.components.loadImageBitmapFromUri
import com.example.contact.ui.components.toVCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contact: Contact,
    isMyProfile: Boolean = false,
    onBackClick: () -> Unit,
    onEditClick: (Contact) -> Unit,
    onImageSelected: (Contact, String) -> Unit,
    onGroupChanged: (Contact, String) -> Unit,
    onDeleteClick: (Contact) -> Unit
) {
    val context = LocalContext.current
    val tealPrimary = Color(0xFF00695C)

    var showQrDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val availableGroups = listOf("All", "Favorites", "Family", "Work")

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
            onImageSelected(contact, it.toString())
        }
    }

    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageSelected(contact, it.toString()) }
    }

    val avatarBitmap = remember(contact.avatarUri) {
        loadImageBitmapFromUri(context, contact.avatarUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (!isMyProfile) {
                        IconButton(onClick = { showGroupDialog = true }) {
                            Icon(
                                Icons.Default.GroupAdd,
                                contentDescription = "Add to Group",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = "Show QR Code",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tealPrimary)
            )
        },
        containerColor = tealPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tappable Avatar with Gallery Badge
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clickable {
                        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            legacyPickerLauncher.launch("image/*")
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB2DFDB))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = "${contact.name} photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = contact.name.take(1).uppercase(),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = tealPrimary
                        )
                    }
                }

                FilledIconButton(
                    onClick = {
                        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            legacyPickerLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = tealPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = "Choose Image",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = contact.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (contact.title.isNotEmpty() || contact.company.isNotEmpty()) {
                val subtitle = listOf(contact.title, contact.company)
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            if (!isMyProfile) {
                Spacer(modifier = Modifier.height(8.dp))

                // Current Group Pill (tappable to change group)
                Surface(
                    onClick = { showGroupDialog = true },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (contact.isFavorite && contact.group != "Favorites") {
                                "${contact.group} • Favorite"
                            } else {
                                contact.group
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionCircleButton(Icons.AutoMirrored.Filled.Message, "Message") {}
                ActionCircleButton(Icons.Default.Call, "Call") {}
                ActionCircleButton(Icons.Default.QrCode2, "QR Code") { showQrDialog = true }
                ActionCircleButton(Icons.Default.Edit, "Edit") { onEditClick(contact) }
                if (!isMyProfile) {
                    ActionCircleButton(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        containerColor = Color(0xFFFFCDD2),
                        iconColor = Color(0xFFB71C1C)
                    ) {
                        showDeleteDialog = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom White Sheet with Info Cards
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color(0xFFF5F7F8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Contact info", fontWeight = FontWeight.Bold, color = tealPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoRow(Icons.Default.Phone, "Mobile", contact.phone)
                            if (contact.secondaryPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow(Icons.Default.PhoneAndroid, "Mobile 2", contact.secondaryPhone)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoRow(Icons.Default.Email, "Email", contact.email)
                            if (contact.address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow(Icons.Default.LocationOn, "Address", contact.address)
                            }
                            if (contact.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow(Icons.Default.Notes, "Notes", contact.notes)
                            }
                            if (contact.nickname.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow(Icons.Default.Badge, "Nickname", contact.nickname)
                            }
                            if (contact.birthday.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow(Icons.Default.Cake, "Birthday", contact.birthday)
                            }
                            if (contact.workAddress.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow(Icons.Default.Work, "Work Address", contact.workAddress)
                            }
                            if (contact.assistant.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                InfoRow(Icons.Default.SupportAgent, "Assistant", contact.assistant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add to Group Dialog (only for regular contacts)
    if (!isMyProfile && showGroupDialog) {
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            icon = {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = tealPrimary)
            },
            title = { Text("Add ${contact.name} to Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableGroups.forEach { groupName ->
                        val isSelected = if (groupName == "Favorites") {
                            contact.isFavorite || contact.group.equals("Favorites", ignoreCase = true)
                        } else {
                            contact.group.equals(groupName, ignoreCase = true)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onGroupChanged(contact, groupName)
                                    showGroupDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onGroupChanged(contact, groupName)
                                    showGroupDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = groupName,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // QR Code Dialog containing user information
    if (showQrDialog) {
        val vCardText = remember(contact) { contact.toVCard() }
        val qrBitmap = remember(vCardText) { generateQrCodeBitmap(vCardText) }

        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${contact.name} — QR Card",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = tealPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scan to import contact details",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Contact QR Code",
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = listOfNotNull(
                            contact.phone.takeIf { it.isNotBlank() },
                            contact.email.takeIf { it.isNotBlank() }
                        ).joinToString(" • "),
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { showQrDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                        shape = CircleShape
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog (only for regular contacts)
    if (!isMyProfile && showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFB71C1C)
                )
            },
            title = { Text("Delete Contact?") },
            text = {
                Text("Are you sure you want to delete ${contact.name}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(contact)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActionCircleButton(
    icon: ImageVector,
    label: String,
    containerColor: Color = Color(0xFFB2DFDB),
    iconColor: Color = Color(0xFF004D40),
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(46.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = containerColor,
                contentColor = iconColor
            )
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}