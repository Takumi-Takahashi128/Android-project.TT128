package com.example.contact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contact.ui.theme.ContactTheme

data class ContactEntry(
    val id: Int,
    val name: String,
    val phone: String,
    val isFavorite: Boolean = false
)

private val AppFontFamily = FontFamily.SansSerif

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactTheme {
                GoogleContactsStyleApp()
            }
        }
    }
}

@Composable
fun GoogleContactsStyleApp() {
    val searchBarBg = Color(0xFFEDF2FA)
    val blueAccent = Color(0xFF0B57D0)
    val softBlueBg = Color(0xFFE8F0FE)
    val fabBg = Color(0xFFD3E3FD)
    val navBg = Color(0xFFF3F6FC)
    val navPillBg = Color(0xFFC2E7FF)

    val contacts = remember { mutableStateListOf<ContactEntry>() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All contacts") }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var sortAscending by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var showAddDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }

    val visibleContacts = remember(contacts.size, searchQuery, selectedFilter, sortAscending) {
        contacts
            .filter { item ->
                val matchesFilter = when (selectedFilter) {
                    "Favorites" -> item.isFavorite
                    else -> true
                }
                val matchesQuery = searchQuery.isBlank() ||
                        item.name.contains(searchQuery.trim(), ignoreCase = true) ||
                        item.phone.contains(searchQuery.trim(), ignoreCase = true)
                matchesFilter && matchesQuery
            }
            .let { list ->
                if (sortAscending) list.sortedBy { it.name }
                else list.sortedByDescending { it.name }
            }
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (selectedTab == 0) {
                Surface(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = fabBg,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add contact",
                            tint = Color(0xFF001D35),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = navBg,
                tonalElevation = 0.dp
            ) {
                val tabs = listOf("Contacts", "Highlights", "Organize")
                tabs.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) navPillBg else Color.Transparent)
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.Person
                                        1 -> Icons.Default.Search
                                        else -> Icons.Default.Add
                                    },
                                    contentDescription = label,
                                    tint = if (isSelected) Color(0xFF001D35) else Color(0xFF444746),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontFamily = AppFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF1F1F1F) else Color(0xFF444746)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // 1. 상단 알약형 검색바 + 프로필 배지
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(searchBarBg)
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF444746),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search contacts",
                            fontFamily = AppFontFamily,
                            color = Color(0xFF444746),
                            fontSize = 16.sp
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = AppFontFamily,
                            color = Color(0xFF1F1F1F),
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(blueAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        fontFamily = AppFontFamily,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            // 2. 필터 행 (All contacts 드롭다운 + 우측 정렬 아이콘)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { filterMenuExpanded = true }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF1F1F1F),
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedFilter,
                            fontFamily = AppFontFamily,
                            color = Color(0xFF1F1F1F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Filter options",
                            tint = Color(0xFF1F1F1F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All contacts", fontFamily = AppFontFamily) },
                            onClick = {
                                selectedFilter = "All contacts"
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Favorites", fontFamily = AppFontFamily) },
                            onClick = {
                                selectedFilter = "Favorites"
                                filterMenuExpanded = false
                            }
                        )
                    }
                }

                IconButton(onClick = { sortAscending = !sortAscending }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(2.dp)
                                .background(Color(0xFF1F1F1F))
                        )
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(Color(0xFF1F1F1F))
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(2.dp)
                                .background(Color(0xFF1F1F1F))
                        )
                    }
                }
            }

            // 3. 본문: 비어 있을 때 모던 카드 일러스트 + "No contacts yet", 있으면 목록
            if (visibleContacts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ModernEmptyContactsGraphic(
                        accentColor = blueAccent,
                        softColor = softBlueBg,
                        modifier = Modifier.size(width = 180.dp, height = 165.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "No contacts yet",
                        fontFamily = AppFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1F1F1F)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visibleContacts, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFD))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(fabBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1).uppercase(),
                                    fontFamily = AppFontFamily,
                                    color = blueAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontFamily = AppFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1F1F1F)
                                )
                                Text(
                                    text = contact.phone,
                                    fontFamily = AppFontFamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF444746)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("New contact", fontFamily = AppFontFamily) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Name", fontFamily = AppFontFamily) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Phone", fontFamily = AppFontFamily) },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                                val nextId = (contacts.maxOfOrNull { it.id } ?: 0) + 1
                                contacts.add(
                                    ContactEntry(
                                        id = nextId,
                                        name = nameInput.trim(),
                                        phone = phoneInput.trim()
                                    )
                                )
                                nameInput = ""
                                phoneInput = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Save", fontFamily = AppFontFamily)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", fontFamily = AppFontFamily)
                    }
                }
            )
        }
    }
}

@Composable
fun ModernEmptyContactsGraphic(
    accentColor: Color,
    softColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()

        // 뒤쪽 기울어진 연한 블루 카드
        drawRoundRect(
            color = softColor,
            topLeft = Offset(w * 0.24f, h * 0.18f),
            size = Size(w * 0.56f, h * 0.64f),
            cornerRadius = CornerRadius(24f, 24f)
        )

        // 앞쪽 메인 주소록 카드
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.18f, h * 0.24f),
            size = Size(w * 0.58f, h * 0.58f),
            cornerRadius = CornerRadius(24f, 24f)
        )
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(w * 0.18f, h * 0.24f),
            size = Size(w * 0.58f, h * 0.58f),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = stroke)
        )

        // 카드 상단 프로필 원형 아바타
        drawCircle(
            color = softColor,
            radius = w * 0.10f,
            center = Offset(w * 0.47f, h * 0.43f)
        )
        drawCircle(
            color = accentColor,
            radius = w * 0.042f,
            center = Offset(w * 0.47f, h * 0.405f)
        )
        drawArc(
            color = accentColor,
            startAngle = 195f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(w * 0.415f, h * 0.455f),
            size = Size(w * 0.11f, h * 0.075f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // 카드 하단 텍스트 라인 2줄
        drawLine(
            color = accentColor.copy(alpha = 0.85f),
            start = Offset(w * 0.31f, h * 0.61f),
            end = Offset(w * 0.63f, h * 0.61f),
            strokeWidth = stroke * 1.2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = accentColor.copy(alpha = 0.45f),
            start = Offset(w * 0.36f, h * 0.69f),
            end = Offset(w * 0.58f, h * 0.69f),
            strokeWidth = stroke * 1.2f,
            cap = StrokeCap.Round
        )
    }
}