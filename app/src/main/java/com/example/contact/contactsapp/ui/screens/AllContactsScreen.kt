package com.example.contactsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.contact.ui.components.loadImageBitmapFromUri
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility as BoxAnimatedVisibility
import androidx.compose.foundation.combinedClickable


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun AllContactsScreen(
    contacts: List<Contact>,
    myProfile: Contact,
    onContactClick: (Contact) -> Unit,
    onMyInfoClick: () -> Unit,
    onAddClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onGroupChanged: (Contact, String) -> Unit,
    onDeleteClick: (Contact) -> Unit,
    onBatchGroupChanged: (List<Contact>, String) -> Unit,
    onBatchDeleteClick: (List<Contact>) -> Unit
) {
    val density = LocalDensity.current
    var searchQuery by remember { mutableStateOf("") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    var groupTargetContact by remember { mutableStateOf<Contact?>(null) }
    var deleteTargetContact by remember { mutableStateOf<Contact?>(null) }
    var showBatchGroupDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var openedSwipeContactId by remember { mutableStateOf<String?>(null) }

    val availableGroups = listOf("All", "Favorites", "Family", "Work")

    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }.sortedBy { it.name }

    val visibleIds = remember(filteredContacts) { filteredContacts.map { it.id }.toSet() }
    val allVisibleSelected = visibleIds.isNotEmpty() && visibleIds.all { it in selectedIds }
    val selectedContacts = remember(contacts, selectedIds) {
        contacts.filter { it.id in selectedIds }
    }
    val hasSelection = selectedContacts.isNotEmpty()

    LaunchedEffect(contacts) {
        val existingIds = contacts.map { it.id }.toSet()
        selectedIds = selectedIds.intersect(existingIds)
        if (selectedIds.isEmpty() && !isSelectionMode) {
            isSelectionMode = false
        }
    }

    val alphabet = ('A'..'Z').toList()

    // Initial add-button offset: 5.dp from right, 10.dp from bottom inside the list box
    val initialRightPx = with(density) { 30.dp.toPx() }
    val initialBottomPx = with(density) { 50.dp.toPx() }
    val buttonSizeDp = 52.dp
    val buttonSizePx = with(density) { buttonSizeDp.toPx() }

    var listBoxSize by remember { mutableStateOf(IntSize.Zero) }
    var hasInitializedButton by remember { mutableStateOf(false) }
    var buttonOffset by remember { mutableStateOf(Offset.Zero) }
    var isDraggingAddButton by remember { mutableStateOf(false) }

    LaunchedEffect(listBoxSize, hasInitializedButton) {
        if (listBoxSize.width > 0 && listBoxSize.height > 0 && !hasInitializedButton) {
            val startX = (listBoxSize.width - buttonSizePx - initialRightPx)
                .coerceIn(0f, (listBoxSize.width - buttonSizePx).coerceAtLeast(0f))
            val startY = (listBoxSize.height - buttonSizePx - initialBottomPx)
                .coerceIn(0f, (listBoxSize.height - buttonSizePx).coerceAtLeast(0f))
            buttonOffset = Offset(startX, startY)
            hasInitializedButton = true
        }
    }

    fun toggleSelectAll() {
        if (!isSelectionMode) {
            isSelectionMode = true
            selectedIds = selectedIds + visibleIds
        } else if (allVisibleSelected) {
            selectedIds = selectedIds - visibleIds
            isSelectionMode = false
        } else {
            selectedIds = selectedIds + visibleIds
        }
    }

    fun toggleContactSelection(contactId: String) {
        if (!isSelectionMode) {
            isSelectionMode = true
            selectedIds = selectedIds + contactId
        } else {
            val next = if (contactId in selectedIds) {
                selectedIds - contactId
            } else {
                selectedIds + contactId
            }
            selectedIds = next
            if (next.isEmpty()) {
                isSelectionMode = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Contacts",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2)),
                actions = {
                    TextButton(onClick = onGroupsClick) {
                        Text("Groups", color = Color.White)
                    }
                    // Select-all radio button right next to "Groups"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { toggleSelectAll() }
                            .padding(end = 8.dp)
                    ) {
                        RadioButton(
                            selected = allVisibleSelected,
                            onClick = { toggleSelectAll() },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.White,
                                unselectedColor = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // My Information Button (Left) + Search Bar (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onMyInfoClick,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFE0F2F1),
                        contentColor = Color(0xFF00695C)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "My Information",
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Bar") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = CircleShape,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Selection notification directly under the Search Bar
            AnimatedVisibility(
                visible = hasSelection,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${selectedContacts.size} contact(s) selected",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0D47A1),
                                fontSize = 14.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                selectedIds = emptySet()
                                isSelectionMode = false
                            }
                        ) {
                            Text("Clear", color = Color(0xFF1976D2))
                        }
                    }
                }
            }

            // Contacts List Box + Alphabet Scroller + Movable Add Button + Bottom Action Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { newSize ->
                        listBoxSize = newSize
                        if (hasInitializedButton && newSize.width > 0 && newSize.height > 0) {
                            val maxX = (newSize.width - buttonSizePx).coerceAtLeast(0f)
                            val maxY = (newSize.height - buttonSizePx).coerceAtLeast(0f)
                            buttonOffset = Offset(
                                x = buttonOffset.x.coerceIn(0f, maxX),
                                y = buttonOffset.y.coerceIn(0f, maxY)
                            )
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 24.dp),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 6.dp,
                        bottom = if (hasSelection) 80.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredContacts, key = { it.id }) { contact ->
                        val isSelected = contact.id in selectedIds
                        SwipeableContactListItem(
                            contact = contact,
                            showRadioButton = isSelectionMode,
                            isSelected = isSelected,
                            isRevealed = openedSwipeContactId == contact.id,
                            onToggleSelect = { toggleContactSelection(contact.id) },
                            onLongPress = {
                                openedSwipeContactId = null
                                toggleContactSelection(contact.id)
                            },
                            onRevealChange = { revealed ->
                                openedSwipeContactId = if (revealed) contact.id else null
                            },
                            onClick = {
                                if (openedSwipeContactId != null) {
                                    openedSwipeContactId = null
                                } else if (isSelectionMode) {
                                    toggleContactSelection(contact.id)
                                } else {
                                    onContactClick(contact)
                                }
                            },
                            onAddGroupClick = {
                                openedSwipeContactId = null
                                groupTargetContact = contact
                            },
                            onDeleteClick = {
                                openedSwipeContactId = null
                                deleteTargetContact = contact
                            }
                        )
                    }
                }

                // Alphabet Sidebar
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    alphabet.forEach { letter ->
                        Text(
                            text = letter.toString(),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Movable Add Button: only moves when pressed/held and dragged
                if (hasInitializedButton) {
                    SmallFloatingActionButton(
                        onClick = {
                            if (!isDraggingAddButton) {
                                onAddClick()
                            }
                        },
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = buttonOffset.x.roundToInt(),
                                    y = buttonOffset.y.roundToInt()
                                )
                            }
                            .size(buttonSizeDp)
                            .pointerInput(listBoxSize) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDraggingAddButton = true
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (isDraggingAddButton) {
                                            val maxX = (listBoxSize.width - buttonSizePx).coerceAtLeast(0f)
                                            val maxY = (listBoxSize.height - buttonSizePx).coerceAtLeast(0f)
                                            buttonOffset = Offset(
                                                x = (buttonOffset.x + dragAmount.x).coerceIn(0f, maxX),
                                                y = (buttonOffset.y + dragAmount.y).coerceIn(0f, maxY)
                                            )
                                        }
                                    },
                                    onDragEnd = {
                                        isDraggingAddButton = false
                                    },
                                    onDragCancel = {
                                        isDraggingAddButton = false
                                    }
                                )
                            },
                        shape = CircleShape,
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Contact",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Group & Delete bar appearing from the bottom when contacts are selected
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = hasSelection,
                        enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
                    ) {
                        Surface(
                            color = Color.White,
                            shadowElevation = 10.dp,
                            tonalElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { showBatchGroupDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF00695C),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = "Group",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Group")
                                }

                                Button(
                                    onClick = { showBatchDeleteDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD32F2F),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Batch Add Group Dialog
    if (showBatchGroupDialog && selectedContacts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBatchGroupDialog = false },
            icon = {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFF00695C))
            },
            title = { Text("Add ${selectedContacts.size} Contacts to Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableGroups.forEach { groupName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onBatchGroupChanged(selectedContacts, groupName)
                                    selectedIds = emptySet()
                                    isSelectionMode = false
                                    showBatchGroupDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF00695C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = groupName, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBatchGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteDialog && selectedContacts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFB71C1C))
            },
            title = { Text("Delete ${selectedContacts.size} Contacts?") },
            text = {
                Text("Are you sure you want to delete all ${selectedContacts.size} selected contacts?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBatchDeleteClick(selectedContacts)
                        selectedIds = emptySet()
                        isSelectionMode = false
                        showBatchDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Single-contact Add to Group Dialog from swiped item
    groupTargetContact?.let { target ->
        AlertDialog(
            onDismissRequest = { groupTargetContact = null },
            icon = {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFF00695C))
            },
            title = { Text("Add ${target.name} to Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableGroups.forEach { groupName ->
                        val isSelected = if (groupName == "Favorites") {
                            target.isFavorite || target.group.equals("Favorites", ignoreCase = true)
                        } else {
                            target.group.equals(groupName, ignoreCase = true)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onGroupChanged(target, groupName)
                                    groupTargetContact = null
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onGroupChanged(target, groupName)
                                    groupTargetContact = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = groupName, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { groupTargetContact = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Single-contact Delete Confirmation Dialog from swiped item
    deleteTargetContact?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTargetContact = null },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFB71C1C))
            },
            title = { Text("Delete Contact?") },
            text = { Text("Delete ${target.name} from your contacts?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick(target)
                        deleteTargetContact = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetContact = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableContactListItem(
    contact: Contact,
    showRadioButton: Boolean,
    isSelected: Boolean,
    isRevealed: Boolean,
    onToggleSelect: () -> Unit,
    onLongPress: () -> Unit,
    onRevealChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val actionWidthDp = 128.dp
    val actionWidthPx = with(density) { actionWidthDp.toPx() }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isRevealed) {
        offsetX.animateTo(if (isRevealed) -actionWidthPx else 0f)
    }

    val avatarBitmap = remember(contact.avatarUri) {
        loadImageBitmapFromUri(context, contact.avatarUri)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Hidden right-side actions revealed when item slides left
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF00695C))
                    .clickable(onClick = onAddGroupClick),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "Add to Group",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Group", color = Color.White, fontSize = 10.sp)
                }
            }

            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFD32F2F))
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Delete", color = Color.White, fontSize = 10.sp)
                }
            }
        }

        // Foreground Contact Row that shifts left on horizontal drag
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(if (isSelected) Color(0xFFE3F2FD) else Color.White)
                .pointerInput(contact.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = (offsetX.value + dragAmount).coerceIn(-actionWidthPx, 0f)
                            scope.launch { offsetX.snapTo(next) }
                        },
                        onDragEnd = {
                            scope.launch {
                                val shouldReveal = offsetX.value < -actionWidthPx * 0.35f
                                onRevealChange(shouldReveal)
                                offsetX.animateTo(if (shouldReveal) -actionWidthPx else 0f)
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                val shouldReveal = offsetX.value < -actionWidthPx * 0.35f
                                onRevealChange(shouldReveal)
                                offsetX.animateTo(if (shouldReveal) -actionWidthPx else 0f)
                            }
                        }
                    )
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showRadioButton) {
                RadioButton(
                    selected = isSelected,
                    onClick = onToggleSelect
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2F1)),
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
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (contact.group.isNotBlank() && contact.group != "All") {
                    Text(
                        text = contact.group,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}