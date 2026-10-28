package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.OutlinedButton
import com.example.model.CuratedPresets
import com.example.player.YouTubeWebViewPlayer
import com.example.ui.components.PlayerControlsView
import com.example.ui.components.ScreenDimmerOverlay
import com.example.ui.components.SleepTimerBottomSheet
import com.example.ui.components.VideoItemCard
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.NeonRedGlow
import com.example.ui.theme.SkyBlueAccent
import com.example.util.YouTubeMetadataFetcher
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PlayerViewModel,
    onRequestPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentVideo by viewModel.currentVideo.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val currentTime by viewModel.currentTimeSeconds.collectAsState()
    val totalDuration by viewModel.totalDurationSeconds.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isInPip by viewModel.isInPipMode.collectAsState()
    val isDimmerActive by viewModel.isDimmerActive.collectAsState()
    val sleepMinutes by viewModel.sleepTimerMinutes.collectAsState()
    val sleepSeconds by viewModel.sleepTimerRemainingSeconds.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val urlInput by viewModel.urlInputText.collectAsState()
    val history by viewModel.history.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSleepModal by remember { mutableStateOf(false) }
    val sleepSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToastMessage()
        }
    }

    // 1. If Screen Dimmer mode active: pure AMOLED black overlay
    if (isDimmerActive) {
        ScreenDimmerOverlay(
            item = currentVideo,
            onDismiss = { viewModel.toggleDimmer(false) }
        )
        return
    }

    // 2. Normal & PiP App Interface (Single persistent player instance)
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (!isInPip) {
                    Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                } else {
                    Modifier
                }
            ),
        topBar = {
            if (!isInPip) {
                TopAppBarHeader(
                    onPipClick = onRequestPip,
                    onDimmerClick = { viewModel.toggleDimmer(true) }
                )
            }
        },
        bottomBar = {
            if (!isInPip) {
                BottomNavigation(
                    activeTab = activeTab,
                    onSelectTab = { viewModel.setActiveTab(it) }
                )
            }
        },
        snackbarHost = { if (!isInPip) SnackbarHost(snackbarHostState) },
        containerColor = if (isInPip) Color.Black else MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isInPip) PaddingValues(0.dp) else innerPadding)
        ) {
            // Persistent Video Player: full-screen in PiP, 16:9 banner in normal mode
            Box(
                modifier = if (isInPip) {
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                }
            ) {
                YouTubeWebViewPlayer(
                    videoId = currentVideo.id,
                    callback = viewModel,
                    controller = { viewModel.setPlayerController(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (!isInPip) {
                // Controls View
                PlayerControlsView(
                    item = currentVideo,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                currentTime = currentTime,
                totalDuration = totalDuration,
                playbackSpeed = playbackSpeed,
                sleepTimerMinutes = sleepMinutes,
                sleepTimerRemainingSeconds = sleepSeconds,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekRelative = { viewModel.seekRelative(it) },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onPipClick = onRequestPip,
                onSleepTimerClick = { showSleepModal = true },
                onDimmerClick = { viewModel.toggleDimmer(true) },
                onFavoriteToggle = { viewModel.toggleFavoriteCurrent() }
            )

            // Body Content based on Active Tab
            when (activeTab) {
                NavigationTab.NOW_PLAYING -> {
                    NowPlayingTabContent(
                        urlInput = urlInput,
                        onUrlInputChange = { viewModel.setUrlInput(it) },
                        onPlayInput = { viewModel.playFromInput() },
                        onPasteClipboard = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (clipText.isNotEmpty()) {
                                viewModel.setUrlInput(clipText)
                            }
                        },
                        currentVideo = currentVideo,
                        onPlayItem = { viewModel.playItem(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) }
                    )
                }
                NavigationTab.DISCOVER -> {
                    DiscoverTabContent(
                        selectedCategory = selectedCategory,
                        onSelectCategory = { viewModel.setSelectedCategory(it) },
                        currentVideo = currentVideo,
                        onPlayItem = { viewModel.playItem(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) }
                    )
                }
                NavigationTab.LIBRARY -> {
                    LibraryTabContent(
                        favorites = favorites,
                        history = history,
                        currentVideo = currentVideo,
                        onPlayItem = { viewModel.playItem(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onDeleteHistoryItem = { viewModel.removeFromHistory(it) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
            }
        }
    }
}

    // Sleep Timer Bottom Sheet
    if (showSleepModal) {
        SleepTimerBottomSheet(
            sheetState = sleepSheetState,
            activeMinutes = sleepMinutes,
            remainingSeconds = sleepSeconds,
            onSetTimer = { viewModel.startSleepTimer(it) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { showSleepModal = false }
        )
    }
}

@Composable
private fun TopAppBarHeader(
    onPipClick: () -> Unit,
    onDimmerClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CrimsonPrimary, NeonRedGlow)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Background Player",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Background & PiP Audio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPipClick,
                    modifier = Modifier.testTag("header_pip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Picture in picture",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingTabContent(
    urlInput: String,
    onUrlInputChange: (String) -> Unit,
    onPlayInput: () -> Unit,
    onPasteClipboard: () -> Unit,
    currentVideo: com.example.model.PlayItem,
    onPlayItem: (com.example.model.PlayItem) -> Unit,
    onFavoriteToggle: (com.example.model.PlayItem) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("now_playing_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Link Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Play from YouTube Link",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Paste any video, shorts, or music link",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = onUrlInputChange,
                        placeholder = {
                            Text(
                                "Paste a YouTube link",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (urlInput.isNotEmpty()) {
                                IconButton(onClick = { onUrlInputChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = onPasteClipboard,
                                    modifier = Modifier.testTag("paste_clipboard_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from clipboard",
                                        tint = SkyBlueAccent
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPlayInput,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("play_url_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play in App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val targetLink = urlInput.trim().ifEmpty { currentVideo.originalUrl }
                                YouTubeMetadataFetcher.openYouTubeExternal(context, targetLink)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_youtube_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = SkyBlueAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Open YouTube",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section header for Up Next / Curated
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended Background Streams",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        items(CuratedPresets.items) { item ->
            VideoItemCard(
                item = item,
                isCurrent = currentVideo.id == item.id,
                onPlayClick = { onPlayItem(item) },
                onFavoriteToggle = { onFavoriteToggle(item) }
            )
        }
    }
}

@Composable
private fun DiscoverTabContent(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    currentVideo: com.example.model.PlayItem,
    onPlayItem: (com.example.model.PlayItem) -> Unit,
    onFavoriteToggle: (com.example.model.PlayItem) -> Unit
) {
    val filteredItems = remember(selectedCategory) {
        if (selectedCategory == "All") {
            CuratedPresets.items
        } else {
            CuratedPresets.items.filter { it.category == selectedCategory }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CuratedPresets.categories.forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(category) },
                    label = { Text(category, fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("filter_chip_$category")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("discover_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredItems) { item ->
                VideoItemCard(
                    item = item,
                    isCurrent = currentVideo.id == item.id,
                    onPlayClick = { onPlayItem(item) },
                    onFavoriteToggle = { onFavoriteToggle(item) }
                )
            }
        }
    }
}

@Composable
private fun LibraryTabContent(
    favorites: List<com.example.model.PlayItem>,
    history: List<com.example.model.PlayItem>,
    currentVideo: com.example.model.PlayItem,
    onPlayItem: (com.example.model.PlayItem) -> Unit,
    onFavoriteToggle: (com.example.model.PlayItem) -> Unit,
    onDeleteHistoryItem: (com.example.model.PlayItem) -> Unit,
    onClearHistory: () -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Favorites, 1: History

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = CrimsonPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]),
                    color = CrimsonPrimary
                )
            }
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saved (${favorites.size})", fontWeight = FontWeight.SemiBold)
                    }
                },
                modifier = Modifier.testTag("subtab_favorites")
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("History (${history.size})", fontWeight = FontWeight.SemiBold)
                    }
                },
                modifier = Modifier.testTag("subtab_history")
            )
        }

        if (subTab == 0) {
            // Favorites list
            if (favorites.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Favorite,
                    title = "No Saved Videos Yet",
                    subtitle = "Tap the heart icon on any playing video or station to save it for quick background access."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("favorites_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites) { item ->
                        VideoItemCard(
                            item = item,
                            isCurrent = currentVideo.id == item.id,
                            onPlayClick = { onPlayItem(item) },
                            onFavoriteToggle = { onFavoriteToggle(item) }
                        )
                    }
                }
            }
        } else {
            // History list
            if (history.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "No Playback History",
                    subtitle = "Videos and music links you play will be recorded here so you can revisit them anytime."
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onClearHistory,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear History", fontSize = 12.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("history_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(history) { item ->
                            VideoItemCard(
                                item = item,
                                isCurrent = currentVideo.id == item.id,
                                onPlayClick = { onPlayItem(item) },
                                onFavoriteToggle = { onFavoriteToggle(item) },
                                onDeleteClick = { onDeleteHistoryItem(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun BottomNavigation(
    activeTab: NavigationTab,
    onSelectTab: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = activeTab == NavigationTab.NOW_PLAYING,
            onClick = { onSelectTab(NavigationTab.NOW_PLAYING) },
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Now Playing"
                )
            },
            label = { Text("Player") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CrimsonPrimary,
                selectedTextColor = CrimsonPrimary,
                indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_player")
        )

        NavigationBarItem(
            selected = activeTab == NavigationTab.DISCOVER,
            onClick = { onSelectTab(NavigationTab.DISCOVER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = "Discover"
                )
            },
            label = { Text("Radio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CrimsonPrimary,
                selectedTextColor = CrimsonPrimary,
                indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_discover")
        )

        NavigationBarItem(
            selected = activeTab == NavigationTab.LIBRARY,
            onClick = { onSelectTab(NavigationTab.LIBRARY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = "Library"
                )
            },
            label = { Text("Library") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CrimsonPrimary,
                selectedTextColor = CrimsonPrimary,
                indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_tab_library")
        )
    }
}
