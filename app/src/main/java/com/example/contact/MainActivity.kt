package com.example.contact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.contactsapp.model.Contact
import com.example.contactsapp.model.sampleContacts
import com.example.contactsapp.model.sampleMyProfile
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
    var myProfile by remember { mutableStateOf(sampleMyProfile) }
    var currentScreen by remember { mutableStateOf("list") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    fun saveUpdatedContact(updated: Contact) {
        if (updated.id == myProfile.id) {
            myProfile = updated
        } else {
            contacts = if (contacts.any { it.id == updated.id }) {
                contacts.map { if (it.id == updated.id) updated else it }
            } else {
                contacts + updated
            }
        }
        selectedContact = updated
    }

    when (currentScreen) {
        "list" -> AllContactsScreen(
            contacts = contacts,
            myProfile = myProfile,
            onContactClick = {
                selectedContact = it
                currentScreen = "detail"
            },
            onMyInfoClick = {
                selectedContact = myProfile
                currentScreen = "detail"
            },
            onAddClick = {
                selectedContact = null
                currentScreen = "edit"
            },
            onGroupsClick = {
                currentScreen = "groups"
            },
            onGroupChanged = { targetContact, newGroup ->
                val updated = if (newGroup == "Favorites") {
                    targetContact.copy(group = newGroup, isFavorite = true)
                } else {
                    targetContact.copy(group = newGroup)
                }
                saveUpdatedContact(updated)
            },
            onDeleteClick = { targetContact ->
                contacts = contacts.filterNot { it.id == targetContact.id }
                if (selectedContact?.id == targetContact.id) {
                    selectedContact = null
                }
            },
            onBatchGroupChanged = { targets, newGroup ->
                val targetIds = targets.map { it.id }.toSet()
                contacts = contacts.map { item ->
                    if (item.id in targetIds) {
                        if (newGroup == "Favorites") {
                            item.copy(group = newGroup, isFavorite = true)
                        } else {
                            item.copy(group = newGroup)
                        }
                    } else {
                        item
                    }
                }
            },
            onBatchDeleteClick = { targets ->
                val targetIds = targets.map { it.id }.toSet()
                contacts = contacts.filterNot { it.id in targetIds }
                if (selectedContact?.id in targetIds) {
                    selectedContact = null
                }
            }
        )
        "detail" -> selectedContact?.let { contact ->
            ContactDetailScreen(
                contact = contact,
                isMyProfile = contact.id == myProfile.id,
                onBackClick = { currentScreen = "list" },
                onEditClick = { currentScreen = "edit" },
                onImageSelected = { targetContact, uriString ->
                    saveUpdatedContact(targetContact.copy(avatarUri = uriString))
                },
                onGroupChanged = { targetContact, newGroup ->
                    val updated = if (newGroup == "Favorites") {
                        targetContact.copy(group = newGroup, isFavorite = true)
                    } else {
                        targetContact.copy(group = newGroup)
                    }
                    saveUpdatedContact(updated)
                },
                onDeleteClick = { targetContact ->
                    contacts = contacts.filterNot { it.id == targetContact.id }
                    selectedContact = null
                    currentScreen = "list"
                }
            )
        }
        "edit" -> EditContactScreen(
            existingContact = selectedContact,
            onCancel = {
                currentScreen = if (selectedContact != null) "detail" else "list"
            },
            onSave = { savedContact ->
                val merged = if (selectedContact?.avatarUri != null && savedContact.avatarUri == null) {
                    savedContact.copy(avatarUri = selectedContact?.avatarUri)
                } else {
                    savedContact
                }
                saveUpdatedContact(merged)
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