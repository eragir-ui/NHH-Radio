package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.RadioStation
import com.example.data.RadioStationRepository
import com.example.ui.RadioViewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: RadioViewModel by viewModels()

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                viewModel.syncVolumeState()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register system volume keys broadcast listener
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        try {
            registerReceiver(volumeReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(volumeReceiver)
        } catch (e: Exception) {
            // Ignore
        }
    }
}

data class ThemeColors(
    val bg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val redFav: Color
)

val LocalThemeColors = staticCompositionLocalOf {
    ThemeColors(
        bg = Color(0xFF121212),
        cardBg = Color(0xFF1C1B1F),
        cardBorder = Color(0xFF313033),
        textPrimary = Color(0xFFE6E1E5),
        textSecondary = Color(0xFF938F99),
        textMuted = Color(0xFF49454F),
        primary = Color(0xFFD0BCFF),
        secondary = Color(0xFFCCC2DC),
        accent = Color(0xFFD0BCFF),
        redFav = Color(0xFFF2B8B5)
    )
}

fun getThemeColors(themeMode: Int): ThemeColors {
    return when (themeMode) {
        1 -> ThemeColors( // Light / Açık
            bg = Color(0xFFF6F5F9),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE1DFE9),
            textPrimary = Color(0xFF1C1B22),
            textSecondary = Color(0xFF5D5B66),
            textMuted = Color(0xFFB1AEBE),
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            accent = Color(0xFF6750A4),
            redFav = Color(0xFFB3261E)
        )
        2 -> ThemeColors( // OLED Black / OLED Siyah
            bg = Color(0xFF000000),
            cardBg = Color(0xFF0C0C0E),
            cardBorder = Color(0xFF222225),
            textPrimary = Color(0xFFF0F0F0),
            textSecondary = Color(0xFF9F9F9F),
            textMuted = Color(0xFF303035),
            primary = Color(0xFF64B5F6),
            secondary = Color(0xFF90CAF9),
            accent = Color(0xFF64B5F6),
            redFav = Color(0xFFE57373)
        )
        3 -> ThemeColors( // Sunset Amber / Günbatımı
            bg = Color(0xFF16110F),
            cardBg = Color(0xFF221A17),
            cardBorder = Color(0xFF352B27),
            textPrimary = Color(0xFFFFECE0),
            textSecondary = Color(0xFFCEB5A7),
            textMuted = Color(0xFF523F38),
            primary = Color(0xFFFA8231),
            secondary = Color(0xFFF39C12),
            accent = Color(0xFFFA8231),
            redFav = Color(0xFFFC5C65)
        )
        4 -> ThemeColors( // Forest Moss / Sığ Orman
            bg = Color(0xFF0A0F0B),
            cardBg = Color(0xFF121A13),
            cardBorder = Color(0xFF222F24),
            textPrimary = Color(0xFFE4EFE6),
            textSecondary = Color(0xFFABC4B0),
            textMuted = Color(0xFF3B4E40),
            primary = Color(0xFF2ED573),
            secondary = Color(0xFF26DE81),
            accent = Color(0xFF2ED573),
            redFav = Color(0xFFFF6B6B)
        )
        5 -> ThemeColors( // Cyberpunk Neon
            bg = Color(0xFF0B041C),
            cardBg = Color(0xFF150B2D),
            cardBorder = Color(0xFF2A1552),
            textPrimary = Color(0xFFFEE6F4),
            textSecondary = Color(0xFFC8B3E6),
            textMuted = Color(0xFF4C278C),
            primary = Color(0xFF00FA9A),
            secondary = Color(0xFF00FFFF),
            accent = Color(0xFF00FA9A),
            redFav = Color(0xFFFF1493)
        )
        6 -> ThemeColors( // Nordic Glacier / İskandinav Buzulu
            bg = Color(0xFF141A22),
            cardBg = Color(0xFF1D2633),
            cardBorder = Color(0xFF2A374A),
            textPrimary = Color(0xFFEAF0F6),
            textSecondary = Color(0xFF9BB1C9),
            textMuted = Color(0xFF455A73),
            primary = Color(0xFF4DCEFF),
            secondary = Color(0xFF00A8FF),
            accent = Color(0xFF4DCEFF),
            redFav = Color(0xFFFF7675)
        )
        else -> ThemeColors( // Slate / Sen Seç (Cosmic Dark - Default)
            bg = Color(0xFF121212),
            cardBg = Color(0xFF1C1B1F),
            cardBorder = Color(0xFF313033),
            textPrimary = Color(0xFFE6E1E5),
            textSecondary = Color(0xFF938F99),
            textMuted = Color(0xFF49454F),
            primary = Color(0xFFD0BCFF),
            secondary = Color(0xFFCCC2DC),
            accent = Color(0xFFD0BCFF),
            redFav = Color(0xFFF2B8B5)
        )
    }
}

@Composable
fun MainAppScreen(viewModel: RadioViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val colors = getThemeColors(themeMode)
    
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    var activeTab by remember { mutableStateOf(1) } // Default to "Radyolar" tab so they see choices first
    val tabs = listOf("Şimdi Çalıyor", "Radyolar", "Favoriler")
    
    val currentStation by viewModel.currentStation.collectAsState()
    var lastTriggeredStationId by remember { mutableStateOf(-1) }
    
    // Google Sheets integration state management
    var showSheetDialog by remember { mutableStateOf(false) }
    val isSheetLoading by viewModel.isSheetLoading.collectAsState()

    LaunchedEffect(currentStation.id) {
        if (lastTriggeredStationId != -1 && lastTriggeredStationId != currentStation.id) {
            activeTab = 0 // Auto transition to "Now Playing" tab when a stream starts
        }
        lastTriggeredStationId = currentStation.id
    }

    CompositionLocalProvider(LocalThemeColors provides colors) {
        val currentTrackArtwork by viewModel.currentTrackArtwork.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateDarkBg)
        ) {
            // Full Screen Blurred backdrop when "Now Playing" is active
            val backgroundSource: Any? = if (activeTab == 0) {
                val art = currentTrackArtwork
                val logo = currentStation.logoUrl
                if (art is ByteArray && art.isNotEmpty()) {
                    art
                } else if (art is String && art.isNotEmpty()) {
                    art
                } else if (!logo.isNullOrEmpty()) {
                    logo
                } else {
                    null
                }
            } else {
                null
            }

            if (backgroundSource != null) {
                AsyncImage(
                    model = backgroundSource,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(30.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                    contentScale = ContentScale.FillBounds,
                    alpha = 0.48f
                )
                // Overlay vertical gradient to anchor contrast inside theme colors
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SlateDarkBg.copy(alpha = 0.12f),
                                    SlateDarkBg.copy(alpha = 0.30f),
                                    SlateDarkBg.copy(alpha = 0.78f)
                                )
                            )
                        )
                )
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent, // transparent scaffold so root background shows through!
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (activeTab == 0) Color.Transparent else SlateCardBg) // Transparent for Now Playing tab!
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        // Header Brand Title & Dynamic Theme Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NHH RADIO",
                                color = AmberPrimary, // Dynamic theme primary color
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 0.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Single looping theme selector! (Cycles through all 7 styles: 0 to 6)
                                val themeIcon = when (themeMode) {
                                    0 -> "🌌" // Cosmic Slate (Default)
                                    1 -> "☀️" // Light
                                    2 -> "🌑" // OLED Black
                                    3 -> "🌅" // Sunset Amber
                                    4 -> "🌲" // Forest Moss
                                    5 -> "🔮" // Cyberpunk Neon
                                    6 -> "❄️" // Nordic Glacier
                                    else -> "🌌"
                                }
                                IconButton(
                                    onClick = {
                                        val nextMode = (themeMode + 1) % 7
                                        viewModel.setThemeMode(nextMode)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text(
                                        text = themeIcon,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        // Navigation Tabs
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (activeTab == 0) Color.Transparent else SlateCardBg), // Dynamic themed background or transparent
                            contentAlignment = Alignment.Center
                        ) {
                            TabRow(
                                selectedTabIndex = activeTab,
                                containerColor = if (activeTab == 0) Color.Transparent else SlateCardBg, // Dynamic card background or transparent
                                contentColor = AmberPrimary,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                        color = AmberPrimary,
                                        height = 3.dp
                                    )
                                },
                                divider = {},
                                modifier = Modifier.width(320.dp)
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = activeTab == index,
                                        onClick = { activeTab = index },
                                        text = {
                                            Text(
                                                text = title,
                                                fontSize = 14.sp,
                                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                                color = if (activeTab == index) TextPrimary else TextSecondary,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Visible
                                            )
                                        },
                                        modifier = Modifier.testTag("tab_$index")
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            color = if (activeTab == 0) Color.White.copy(alpha = 0.12f) else SlateCardBorder,
                            thickness = 1.dp
                        ) // Dynamic theme border
                    }
                },
                bottomBar = {
                    if (activeTab != 0) {
                        CompactPersistentPlayer(
                            viewModel = viewModel,
                            onNavigateToPlayer = { activeTab = 0 }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (activeTab) {
                        0 -> NowPlayingScreen(
                            viewModel = viewModel,
                            onSwipeToRadios = { activeTab = 1 }
                        )
                        1 -> RadiosScreen(
                            viewModel = viewModel,
                            onStationSelected = { activeTab = 0 },
                            onSwipeToPlayer = { activeTab = 0 }
                        )
                        2 -> FavoritesScreen(
                            viewModel = viewModel,
                            onStationSelected = { activeTab = 0 },
                            onSwipeToPlayer = { activeTab = 0 }
                        )
                    }
                }
            }

            if (showSheetDialog) {
                GoogleSheetConfigDialog(
                    viewModel = viewModel,
                    onDismiss = { showSheetDialog = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreen(
    viewModel: RadioViewModel,
    onSwipeToRadios: () -> Unit
) {
    val colors = LocalThemeColors.current
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    val currentStation by viewModel.currentStation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val currentTrackName by viewModel.currentTrackName.collectAsState()
    val currentTrackArtwork by viewModel.currentTrackArtwork.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.contains(currentStation.id)
    val streamCodec by viewModel.streamCodec.collectAsState()
    val streamBitrate by viewModel.streamBitrate.collectAsState()

    // Sleep Timer States
    val sleepActive by viewModel.sleepTimerActive.collectAsState()
    val sleepMin by viewModel.sleepTimerMinutesLeft.collectAsState()
    val sleepSec by viewModel.sleepTimerSecondsLeft.collectAsState()
    var showSleepDialog by remember { mutableStateOf(false) }

    // Volume States
    val volumePercent by viewModel.systemVolume.collectAsState()

    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding() // Fixed overlapping under system navigation elements
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed elegant vertical margin between Album art and navigation Tab bar
            Spacer(modifier = Modifier.height(26.dp))

            // Group the Artwork and text block together to keep them tightly grouped and beautifully near
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Center Album Artwork box (scenes are always images; no text overlay codes inside!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .shadow(16.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(currentStation.gradientStart))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(currentStation.gradientStart),
                                    Color(currentStation.gradientEnd)
                                )
                            )
                        )
                        .border(1.5.dp, SlateCardBorder, RoundedCornerShape(24.dp))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    // User swiped horizontally above the threshold
                                    if (kotlin.math.abs(dragOffset) > 80f) {
                                        onSwipeToRadios()
                                    }
                                    dragOffset = 0f
                                },
                                onDragCancel = {
                                    dragOffset = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val fallbackArtwork = if (!currentStation.logoUrl.isNullOrEmpty()) currentStation.logoUrl else ""
                    val art = currentTrackArtwork
                    val activeArtwork: Any = if (art is ByteArray && art.isNotEmpty()) {
                        art
                    } else if (art is String && art.isNotEmpty()) {
                        art
                    } else if (!fallbackArtwork.isNullOrEmpty()) {
                        fallbackArtwork
                    } else {
                        ""
                    }

                    AsyncImage(
                        model = activeArtwork,
                        contentDescription = currentTrackName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Radio Name from Google Sheet is always displayed right below the album cover
                Text(
                    text = currentStation.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Display track artist and song names in a single streamlined row (e.g., Tarkan - Şımarık) with auto-marquee scrolling
                val displayText = if (currentTrackName.isEmpty()) "Canlı Yayın" else currentTrackName
                Text(
                    text = displayText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberPrimary,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .basicMarquee()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Beautifully expanded row width at 0.92f so modern layout elements (Heart favoriting & Countdown box)
                // align precisely on the same outer vertical guidelines as the 0.85f Album artwork cover box boundaries!
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left element: Inline Favorite Heart Button
                    IconButton(
                        onClick = { viewModel.toggleFavorite(currentStation.id) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("nowplay_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoriye Ekle",
                            tint = if (isFav) RedFavorite else TextPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Centered element: Dynamic Bitrate & Codec representation in "Aac / 128 Kbps" custom styling!
                    val rawCodec = if (streamCodec.isNullOrEmpty()) "AAC" else streamCodec!!
                    val displayCodec = when (rawCodec.uppercase()) {
                        "AAC" -> "Aac"
                        "MP3" -> "Mp3"
                        "OGG" -> "Ogg"
                        else -> rawCodec.lowercase().replaceFirstChar { it.uppercase() }
                    }
                    val displayBitrate = if (streamBitrate.isNullOrEmpty()) {
                        "128"
                    } else {
                        streamBitrate!!.replace("kbps", "", ignoreCase = true).replace("KBPS", "", ignoreCase = true).trim()
                    }
                    val codecInfo = "$displayCodec / $displayBitrate Kbps"

                    Text(
                        text = codecInfo,
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )

                    // Right element: Inline Sleep Timer button
                    if (!sleepActive) {
                        IconButton(
                            onClick = { showSleepDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("nowplay_sleep_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Alarm,
                                contentDescription = "Uyku Kapanışı",
                                tint = TextPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // Countdown active: elegant duration block
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateCardBg.copy(alpha = 0.6f))
                                .clickable { showSleepDialog = true }
                                .padding(horizontal = 9.dp)
                                .testTag("nowplay_sleep_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d:%02d", sleepMin, sleepSec),
                                fontSize = 12.sp,
                                color = EmeraldAccent,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Playback Controller Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.previousStation() },
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("prev_station_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Önceki Radyo",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AmberPrimary) // AmberPrimary is modern Lavender theme color
                        .clickable { viewModel.togglePlayback() }
                        .testTag("play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = Color(0xFF381E72),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Oynat Durdur",
                            tint = Color(0xFF381E72), // Beautiful contrast indigo as specified in design HTML
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(28.dp))

                IconButton(
                    onClick = { viewModel.nextStation() },
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("next_station_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Sonraki Radyo",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Placed slightly below play/pause keys

            // Volume Row: occupies full row width cleanly (thickened, draggable circle styler, percentage included)
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.96f) // Slightly inset for extra clean visual alignment
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Volume Speaker Icon
                Icon(
                    imageVector = if (volumePercent == 0) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                    contentDescription = "Ses Düzeyi",
                    tint = if (volumePercent == 0) TextMuted else TextSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            if (volumePercent > 0) {
                                viewModel.setSystemVolumePercentage(0)
                            } else {
                                viewModel.setSystemVolumePercentage(40)
                            }
                        }
                )

                // Volume Slider (Thicker and customized with beautiful knob)
                ThinVolumeSlider(
                    value = volumePercent.toFloat(),
                    onValueChange = { viewModel.setSystemVolumePercentage(it.toInt()) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("volume_slider"),
                    activeColor = AmberPrimary,
                    inactiveColor = SlateCardBorder
                )

                // Volume Percentage text
                Text(
                    text = "$volumePercent%",
                    color = AmberPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(34.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.weight(1.0f)) // Remaining spacing pushed to the bottom of card layout
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Alarm Clock Timer Control Dialog Drawer
    if (showSleepDialog) {
        SleepTimerDialog(
            viewModel = viewModel,
            onDismiss = { showSleepDialog = false }
        )
    }
}

@Composable
fun RadiosScreen(
    viewModel: RadioViewModel,
    onStationSelected: () -> Unit,
    onSwipeToPlayer: () -> Unit
) {
    val colors = LocalThemeColors.current
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    val genreFilter by viewModel.selectedGenre.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val currentTrackName by viewModel.currentTrackName.collectAsState()

    val genresList by viewModel.genres.collectAsState()
    val isSheetLoading by viewModel.isSheetLoading.collectAsState()
    val sheetError by viewModel.sheetError.collectAsState()

    val filteredStations = remember(genreFilter, stations) {
        if (genreFilter == "Tümü") stations else stations.filter { it.genre == genreFilter }
    }

    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // "sola kaydırınca radyolardan şimdi çalışıyora" -> Finger moves left (deltaX < 0)
                        if (dragOffset < -80f) {
                            onSwipeToPlayer()
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
        if (isSheetLoading && stations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AmberPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Radyo listesi alınıyor...",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (sheetError != null && stations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "⚠️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sunucu/Bağlantı Hatası",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sheetError ?: "Listeniz Google Sheet üzerinden alınamadı.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.loadStations() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                    ) {
                        Text("Tekrar Gözden Geçir", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Genres quick scroll row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genresList) { sGenre ->
                        val isSelected = sGenre == genreFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) TextMuted else SlateCardBorder)
                                .border(1.dp, if (isSelected) AmberPrimary.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectGenre(sGenre) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("genre_$sGenre")
                        ) {
                            Text(
                                text = sGenre,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(color = TextMuted, thickness = 1.dp)

                // Standard Compact List with very close borders as requested
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .testTag("radios_list")
                ) {
                    items(filteredStations) { station ->
                        val isFav = favorites.contains(station.id)
                        RadioItemRow(
                            station = station,
                            isFavorite = isFav,
                            isCurrent = (station.id == currentStation.id),
                            currentTrackName = currentTrackName,
                            onSelect = {
                                viewModel.selectStation(station)
                                onStationSelected()
                            },
                            onFavorite = { viewModel.toggleFavorite(station.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    viewModel: RadioViewModel,
    onStationSelected: () -> Unit,
    onSwipeToPlayer: () -> Unit
) {
    val colors = LocalThemeColors.current
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    val stations by viewModel.stations.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val currentTrackName by viewModel.currentTrackName.collectAsState()

    val favoriteStations = remember(favorites, stations) {
        stations.filter { favorites.contains(it.id) }
    }

    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // "sola kaydırınca radyolardan şimdi çalışıyora" -> Finger moves left (deltaX < 0)
                        if (dragOffset < -80f) {
                            onSwipeToPlayer()
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
        if (favoriteStations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = "Favori Boş",
                    tint = TextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Henüz Favori Radyo Eklenmedi",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Favorilere eklemek için listelerde kalbe dokunun.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("favorites_list")
            ) {
                items(favoriteStations) { station ->
                    RadioItemRow(
                        station = station,
                        isFavorite = true,
                        isCurrent = (station.id == currentStation.id),
                        currentTrackName = currentTrackName,
                        onSelect = {
                            viewModel.selectStation(station)
                            onStationSelected()
                        },
                        onFavorite = { viewModel.toggleFavorite(station.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadioItemRow(
    station: RadioStation,
    isFavorite: Boolean,
    isCurrent: Boolean,
    currentTrackName: String,
    onSelect: () -> Unit,
    onFavorite: () -> Unit
) {
    val colors = LocalThemeColors.current
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Dense row height as requested
            .clickable { onSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(station.gradientStart), Color(station.gradientEnd))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!station.logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = station.logoUrl,
                    contentDescription = station.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = station.initials,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = station.name,
                fontSize = 17.sp, // Increased by 2 points (from 15.sp to 17.sp) as requested
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) AmberPrimary else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = station.genre,
                fontSize = 11.sp,
                color = if (isCurrent) EmeraldAccent else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Heart inline toggle Button on far-right
        IconButton(
            onClick = { onFavorite() },
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorilere Ekle",
                tint = if (isFavorite) RedFavorite else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AnimatedEqualizer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalThemeColors.current
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    val barCount = 5
    val infiniteTransition = rememberInfiniteTransition()
    
    Row(
        modifier = modifier
            .width(60.dp)
            .height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val duration = remember(i) { (400 + i * 150) }
            val heightFraction by if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 0.15f,
                    targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            } else {
                remember { mutableStateOf(0.15f) }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AmberSecondary, EmeraldAccent)
                        )
                    )
            )
        }
    }
}

@Composable
fun CompactPersistentPlayer(
    viewModel: RadioViewModel,
    onNavigateToPlayer: () -> Unit
) {
    val colors = LocalThemeColors.current
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    val currentStation by viewModel.currentStation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateCardBg)
            .navigationBarsPadding() // Flows gorgeous dark background safely behind standard device nav controls
            .height(76.dp) // %30 artırıldı (58.dp için 58 * 1.3 ≈ 76.dp)
            .clickable { onNavigateToPlayer() }
            .border(1.dp, SlateCardBorder, RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp) // %30 artırıldı (38.dp * 1.3 ≈ 50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(currentStation.gradientStart), Color(currentStation.gradientEnd))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentStation.initials,
                color = Color.White,
                fontSize = 17.sp, // %30 daha büyük
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentStation.name,
                fontSize = 16.sp, // %30 daha büyük
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isBuffering) "Yükleniyor..." else if (isPlaying) "Şimdi Çalıyor..." else "Duraklatıldı",
                fontSize = 13.sp, // %30 daha büyük
                color = if (isPlaying) EmeraldAccent else TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.togglePlayback() },
                modifier = Modifier.size(48.dp)
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        color = AmberPrimary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Duraklat Oynat",
                        tint = AmberPrimary,
                        modifier = Modifier.size(30.dp) // %30 daha büyük
                    )
                }
            }
            
            IconButton(
                onClick = { viewModel.nextStation() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Sonraki Radyo",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp) // %30 daha büyük
                )
            }
        }
    }
}

@Composable
fun SleepTimerDialog(
    viewModel: RadioViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalThemeColors.current
    val SlateDarkBg = colors.bg
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val TextMuted = colors.textMuted
    val AmberPrimary = colors.primary
    val AmberSecondary = colors.secondary
    val EmeraldAccent = colors.accent
    val RedFavorite = colors.redFav

    val timerActive by viewModel.sleepTimerActive.collectAsState()
    val minutesLeft by viewModel.sleepTimerMinutesLeft.collectAsState()
    val secondsLeft by viewModel.sleepTimerSecondsLeft.collectAsState()
    val presetSet by viewModel.sleepTimerPreset.collectAsState()
    val customSlider by viewModel.customSleepSliderValue.collectAsState()

    val presets = listOf(5, 15, 30, 45, 60, 90)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg),
            border = BorderStroke(1.5.dp, SlateCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Uyku Zamanlayıcı",
                    color = AmberPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (timerActive) {
                    Text(
                        text = String.format("Yayının Kapanmasına: %02d dk %02d sn", minutesLeft, secondsLeft),
                        color = EmeraldAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(SlateDarkBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Otomatik kapanma süresi seçin",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Hızlı Dakika Seçimi:",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { min ->
                        val isSelectedPreset = presetSet == min && timerActive
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelectedPreset) TextMuted else SlateCardBorder)
                                .border(1.dp, if (isSelectedPreset) AmberPrimary.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setSleepTimerPreset(min)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$min",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelectedPreset) AmberPrimary else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hassas Ayarla:",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${customSlider.toInt()} Dakika",
                        color = AmberSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = customSlider,
                    onValueChange = { viewModel.adjustCustomSleepSlider(it) },
                    valueRange = 1f..180f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = AmberPrimary,
                        inactiveTrackColor = SlateDarkBg,
                        thumbColor = AmberSecondary
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (timerActive) {
                        Button(
                            onClick = {
                                viewModel.cancelSleepTimer()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedFavorite.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("İptal Et", color = RedFavorite)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDarkBg),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, SlateCardBorder)
                        ) {
                            Text("Geri", color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.startCustomSleepTimer()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Başlat", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ThinVolumeSlider(
    value: Float, // 0 to 99
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color,
    inactiveColor: Color
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp), // Comfortable height for interaction
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val progressPercent = value.coerceIn(0f, 99f)
        
        // Let's reserve some space at the start and end for half the thumb size (e.g. 10.dp) for smooth sliding
        val thumbSizeDp = 20.dp
        val thumbRadiusPx = with(density) { (thumbSizeDp / 2).toPx() }
        val usableWidthPx = (widthPx - 2 * thumbRadiusPx).coerceAtLeast(1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(widthPx) {
                    detectTapGestures { offset ->
                        val localX = (offset.x - thumbRadiusPx).coerceIn(0f, usableWidthPx)
                        val pct = localX / usableWidthPx
                        onValueChange(pct * 99f)
                    }
                }
                .pointerInput(widthPx) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val localX = (change.position.x - thumbRadiusPx).coerceIn(0f, usableWidthPx)
                        val pct = localX / usableWidthPx
                        onValueChange(pct * 99f)
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Background track set to exactly 6.dp thickness
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(inactiveColor)
            ) {
                // Active track set to exactly 6.dp thickness
                val fraction = progressPercent / 99f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .background(activeColor)
                )
            }

            // Draggable Thumb - visible round handle
            val thumbOffsetPx = (progressPercent / 99f) * usableWidthPx
            val thumbOffsetDp = with(density) { thumbOffsetPx.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetDp)
                    .size(thumbSizeDp)
                    .clip(CircleShape)
                    .background(Color.White) // High contrast white knob
                    .border(2.5.dp, activeColor, CircleShape) // Beautiful accent border around the knob
            )
        }
    }
}

@Composable
fun GoogleSheetConfigDialog(
    viewModel: RadioViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalThemeColors.current
    val SlateCardBg = colors.cardBg
    val SlateCardBorder = colors.cardBorder
    val TextPrimary = colors.textPrimary
    val TextSecondary = colors.textSecondary
    val AmberPrimary = colors.primary
    val EmeraldAccent = colors.accent

    val googleSheetId by viewModel.googleSheetId.collectAsState()
    val isSheetLoading by viewModel.isSheetLoading.collectAsState()
    val sheetError by viewModel.sheetError.collectAsState()

    var sheetIdInput by remember { mutableStateOf(googleSheetId) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = SlateCardBg,
            border = BorderStroke(1.dp, SlateCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📊", fontSize = 24.sp)
                    Text(
                        text = "Google Sheets Entegrasyonu",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aşağıya Google E-Tablonuzun bağlantısını (linkini) veya Spreadsheet ID değerini yapıştırın. Yayın listeniz anında tüm dinleyicilere güncellenecektir.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = sheetIdInput,
                    onValueChange = { sheetIdInput = it },
                    label = { Text("Google Sheet Bağlantısı veya ID", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = SlateCardBorder,
                        cursorColor = AmberPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Useful instructions card inside Dialog
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBorder.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, SlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Önemli E-Tablo Sütun Şablonu",
                            color = AmberPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "E-Tablonuzda sırasıyla şu sütunların olması gerekir:\n" +
                                   "1. Adı  |  2. Link  |  3. Tür  |  4. Logo Linki\n" +
                                   "5. Kısaltma  |  6. Renk1  |  7. Renk2\n\n" +
                                   "Paylaşım Ayarları:\n" +
                                   "• Dosya -> Paylaş -> Web'de yayınla yolundan CSV olarak yayınlayın veya dosyayı 'Bağlantıya sahip olan herkes görüntüleyebilir' şeklinde ayarlayın.",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display dynamic status/success/error feedback
                if (isSheetLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AmberPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Radyolar Alınıyor...", color = AmberPrimary, fontSize = 12.sp)
                    }
                } else {
                    if (sheetError != null) {
                        Text(
                            text = "❌ " + sheetError,
                            color = Color(0xFFF2B8B5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (successMsg != null) {
                        Text(
                            text = successMsg ?: "",
                            color = EmeraldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset Button (Get sample spreadsheet)
                    OutlinedButton(
                        onClick = {
                            sheetIdInput = "1eNs8WDPY5CsW7rofMoTFGVCEtRHp8cKJyPYS-w6uFD4"
                            successMsg = "Örnek şablon ID dolduruldu."
                        },
                        border = BorderStroke(1.dp, SlateCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Şablon", fontSize = 12.sp)
                    }

                    // Save / Apply Button
                    Button(
                        onClick = {
                            successMsg = null
                            val rawInput = sheetIdInput.trim()
                            // Extract sheet API ID from Google Sheet URL automatically if full link pasted!
                            val finalId = if (rawInput.contains("/d/")) {
                                try {
                                    rawInput.split("/d/")[1].split("/")[0]
                                } catch (e: Exception) {
                                    rawInput
                                }
                            } else {
                                rawInput
                            }
                            
                            viewModel.setGoogleSheetId(finalId)
                            viewModel.loadStations { success ->
                                if (success) {
                                    successMsg = "✅ Radyo listesi başarıyla güncellendi!"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Güncelle", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Close Button
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Kapat", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
