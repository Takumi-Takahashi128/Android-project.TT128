package com.example.contactsapp.model

data class Contact(
    val id: String,
    val name: String,
    val title: String = "",
    val company: String = "",
    val phone: String,
    val secondaryPhone: String = "",
    val email: String,
    val address: String = "",
    val workAddress: String = "",
    val nickname: String = "",
    val birthday: String = "",
    val assistant: String = "",
    val notes: String = "",
    val group: String = "All",
    val isFavorite: Boolean = false,
    val avatarColor: Long = 0xFF006A6A,
    val avatarUri: String? = null
)

val sampleContacts = listOf(
    Contact(
        id = "1",
        name = "Alice Freeman",
        title = "Designer @ Google",
        company = "Google",
        phone = "+1 555-0192",
        email = "alice.f@google.com",
        address = "1600 Amphitheatre Pkwy, Mountain View, CA",
        notes = "Lead UI/UX designer on the Material team.",
        group = "Work",
        isFavorite = true
    ),
    Contact(
        id = "2",
        name = "Brian Kelly",
        title = "Product Manager",
        company = "TechCorp",
        phone = "+1 555-0148",
        email = "brian.k@example.com",
        group = "Work",
        isFavorite = true
    ),
    Contact(
        id = "3",
        name = "Chloe Bennett",
        title = "Architect",
        company = "Studio North",
        phone = "+1 555-0173",
        email = "chloe@example.com",
        group = "Family",
        isFavorite = true
    ),
    Contact(
        id = "4",
        name = "David Miller",
        phone = "+1 555-0199",
        email = "david.m@example.com",
        group = "Family",
        isFavorite = false
    )
)

val sampleMyProfile = Contact(
    id = "my_info",
    name = "My Information",
    title = "Android Developer",
    company = "Personal Profile",
    phone = "+1 555-0100",
    email = "me@example.com",
    address = "123 Main St",
    notes = "My personal contact card.",
    group = "All",
    isFavorite = false
)