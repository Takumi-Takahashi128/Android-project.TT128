package com.example.contactsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contactsapp.model.Contact
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    existingContact: Contact?,
    onCancel: () -> Unit,
    onSave: (Contact) -> Unit
) {
    var name by remember { mutableStateOf(existingContact?.name ?: "") }
    var company by remember { mutableStateOf(existingContact?.company ?: "") }
    var phone by remember { mutableStateOf(existingContact?.phone ?: "") }
    var email by remember { mutableStateOf(existingContact?.email ?: "") }
    var address by remember { mutableStateOf(existingContact?.address ?: "") }
    var notes by remember { mutableStateOf(existingContact?.notes ?: "") }

    val tealPrimary = Color(0xFF00695C)

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
                            if (name.isNotBlank()) {
                                onSave(
                                    Contact(
                                        id = existingContact?.id ?: UUID.randomUUID().toString(),
                                        name = name,
                                        company = company,
                                        phone = phone,
                                        email = email,
                                        address = address,
                                        notes = notes
                                    )
                                )
                            }
                        },
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

            // Profile Photo Placeholder
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = "Add Photo",
                    tint = tealPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val fieldModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )
            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Company") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = fieldModifier
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
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
        }
    }
}