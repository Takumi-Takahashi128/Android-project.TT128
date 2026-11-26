package com.example.contact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.contactsapp.model.Contact
import com.example.contactsapp.model.sampleContacts
import com.example.contactsapp.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ContactsApp()
            }
        }
    }
}

@Composable
fun ContactsApp() {
    var contacts by remember { mutableStateOf(sampleContacts) }
    var currentScreen by remember { mutableStateOf("list") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    when (currentScreen) {
        "list" -> AllContactsScreen(
            contacts = contacts,
            onContactClick = {
                selectedContact = it
                currentScreen = "detail"
            },
            onAddClick = {
                selectedContact = null
                currentScreen = "edit"
            },
            onGroupsClick = {
                currentScreen = "groups"
            }
        )
        "detail" -> selectedContact?.let { contact ->
            ContactDetailScreen(
                contact = contact,
                onBackClick = { currentScreen = "list" },
                onEditClick = { currentScreen = "edit" }
            )
        }
        "edit" -> EditContactScreen(
            existingContact = selectedContact,
            onCancel = {
                currentScreen = if (selectedContact != null) "detail" else "list"
            },
            onSave = { savedContact ->
                contacts = if (contacts.any { it.id == savedContact.id }) {
                    contacts.map { if (it.id == savedContact.id) savedContact else it }
                } else {
                    contacts + savedContact
                }
                selectedContact = savedContact
                currentScreen = "detail"
            }
        )
        "groups" -> GroupsScreen(
            contacts = contacts,
            onBackClick = { currentScreen = "list" },
            onContactClick = {
                selectedContact = it
                currentScreen = "detail"
            },
            onAddClick = {
                selectedContact = null
                currentScreen = "edit"
            }
        )
    }
}