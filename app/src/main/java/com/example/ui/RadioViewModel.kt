package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import android.content.Intent
import android.os.Build
import com.example.RadioService
import com.example.data.RadioStation
import com.example.data.RadioStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs: SharedPreferences =
        application.getSharedPreferences("NHH_Radio_Prefs", Context.MODE_PRIVATE)

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Google Sheets Config State (Defaults to the user's custom Google Sheet containing the Turkish Radios)
    private val _googleSheetId = MutableStateFlow(
        sharedPrefs.getString("google_sheet_id", "1eNs8WDPY5CsW7rofMoTFGVCEtRHp8cKJyPYS-w6uFD4") ?: "1eNs8WDPY5CsW7rofMoTFGVCEtRHp8cKJyPYS-w6uFD4"
    )
    val googleSheetId: StateFlow<String> = _googleSheetId.asStateFlow()

    private val _isSheetLoading = MutableStateFlow(false)
    val isSheetLoading: StateFlow<Boolean> = _isSheetLoading.asStateFlow()

    private val _sheetError = MutableStateFlow<String?>(null)
    val sheetError: StateFlow<String?> = _sheetError.asStateFlow()

    // State definitions
    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    private val _genres = MutableStateFlow<List<String>>(listOf("Tümü"))
    val genres: StateFlow<List<String>> = _genres.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Tümü")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation>(
        RadioStation(
            id = -1,
            name = "Radyo Seçilmedi",
            streamUrl = "",
            genre = "",
            initials = "?",
            gradientStart = 0xFF141E30,
            gradientEnd = 0xFF243B55
        )
    )
    val currentStation: StateFlow<RadioStation> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentTrackName = MutableStateFlow("Yükleniyor...")
    val currentTrackName: StateFlow<String> = _currentTrackName.asStateFlow()

    private val _currentTrackArtwork = MutableStateFlow<String?>(null)
    val currentTrackArtwork: StateFlow<String?> = _currentTrackArtwork.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    private val _themeMode = MutableStateFlow(sharedPrefs.getInt("theme_mode", 0)) // 0: Slate Dark, 1: Light, 2: OLED Black, 3: Sunset Amber, 4: Forest Moss, 5: Cyberpunk Neon, 6: Nordic Glacier
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        sharedPrefs.edit().putInt("theme_mode", mode).apply()
    }

    // Sleep Timer States
    private val _sleepTimerMinutesLeft = MutableStateFlow(0)
    val sleepTimerMinutesLeft: StateFlow<Int> = _sleepTimerMinutesLeft.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow(0)
    val sleepTimerSecondsLeft: StateFlow<Int> = _sleepTimerSecondsLeft.asStateFlow()

    private val _sleepTimerActive = MutableStateFlow(false)
    val sleepTimerActive: StateFlow<Boolean> = _sleepTimerActive.asStateFlow()

    private val _sleepTimerPreset = MutableStateFlow(-1) // 5, 15, 30, 45, 60 or -1
    val sleepTimerPreset: StateFlow<Int> = _sleepTimerPreset.asStateFlow()

    private val _customSleepSliderValue = MutableStateFlow(30f) // default to 30 mins
    val customSleepSliderValue: StateFlow<Float> = _customSleepSliderValue.asStateFlow()

    // Volume States
    private val _systemVolume = MutableStateFlow(0)
    val systemVolume: StateFlow<Int> = _systemVolume.asStateFlow()

    val maxVolumeIndex: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    // Dynamic stream metadata fields (Codec/Bitrate)
    private val _streamCodec = MutableStateFlow<String?>(null)
    val streamCodec: StateFlow<String?> = _streamCodec.asStateFlow()

    private val _streamBitrate = MutableStateFlow<String?>(null)
    val streamBitrate: StateFlow<String?> = _streamBitrate.asStateFlow()

    // Private Player fields
    private var exoPlayer: ExoPlayer? = null
    private var sleepTimerJob: Job? = null
    private var metadataJob: Job? = null
    private var streamInfoJob: Job? = null
    private var reconnectJob: Job? = null
    private var liveMetadataJob: Job? = null
    private var reconnectAttempts = 0

    init {
        loadFavorites()
        syncVolumeState()
        // Set initial track metadata
        updateTrackMetadata()
        // Fetch dynamic radio stations from Google Sheet on startup!
        loadStations()
    }

    // Load favorites from SharedPreferences
    private fun loadFavorites() {
        val savedFavorites = sharedPrefs.getStringSet("favorite_stations", emptySet()) ?: emptySet()
        _favorites.value = savedFavorites.mapNotNull { it.toIntOrNull() }.toSet()
    }

    // Toggle favorite status
    fun toggleFavorite(stationId: Int) {
        val currentFavs = _favorites.value.toMutableSet()
        if (currentFavs.contains(stationId)) {
            currentFavs.remove(stationId)
        } else {
            currentFavs.add(stationId)
        }
        _favorites.value = currentFavs

        // Save
        sharedPrefs.edit()
            .putStringSet("favorite_stations", currentFavs.map { it.toString() }.toSet())
            .apply()
    }

    // Filter genre selection
    fun selectGenre(genre: String) {
        _selectedGenre.value = genre
    }

    // Volume Helpers
    fun syncVolumeState() {
        val currentVal = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVal = maxVolumeIndex.coerceAtLeast(1)
        val rawPercent = ((currentVal.toFloat() / maxVal.toFloat()) * 99f).toInt()
        _systemVolume.value = rawPercent.coerceIn(0, 99)
    }

    fun setSystemVolumePercentage(percent: Int) {
        val limitedPercent = percent.coerceIn(0, 99)
        val maxVal = maxVolumeIndex
        val targetVolume = ((limitedPercent.toFloat() / 99f) * maxVal.toFloat()).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        _systemVolume.value = limitedPercent
    }

    // Start/Stop/Switch Stream Playback
    fun selectStation(station: RadioStation) {
        reconnectAttempts = 0
        reconnectJob?.cancel()
        if (_currentStation.value.id == station.id && exoPlayer != null) {
            // Already plays or is selected, toggle instead
            togglePlayback()
            return
        }
        _currentStation.value = station
        updateTrackMetadata()
        playCurrentStation()
    }

    fun nextStation() {
        val currentList = getFilteredOrAllStations()
        if (currentList.isEmpty()) return
        val currentIndex = currentList.indexOfFirst { it.id == _currentStation.value.id }
        val nextIndex = if (currentIndex == -1 || currentIndex == currentList.size - 1) 0 else currentIndex + 1
        selectStation(currentList[nextIndex])
    }

    fun previousStation() {
        val currentList = getFilteredOrAllStations()
        if (currentList.isEmpty()) return
        val currentIndex = currentList.indexOfFirst { it.id == _currentStation.value.id }
        val prevIndex = if (currentIndex <= 0) currentList.size - 1 else currentIndex - 1
        selectStation(currentList[prevIndex])
    }

    private fun getFilteredOrAllStations(): List<RadioStation> {
        val genre = _selectedGenre.value
        return if (genre == "Tümü") {
            _stations.value
        } else {
            _stations.value.filter { it.genre == genre }
        }
    }

    private fun startService() {
        try {
            val context = getApplication<Application>()
            val intent = Intent(context, RadioService::class.java).apply {
                action = "START_PLAYBACK"
                putExtra("STATION_NAME", _currentStation.value.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopService() {
        try {
            val context = getApplication<Application>()
            val intent = Intent(context, RadioService::class.java).apply {
                action = "STOP_PLAYBACK"
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayback() {
        val isCurrentlyPlaying = _isPlaying.value
        reconnectJob?.cancel()
        reconnectAttempts = 0
        if (isCurrentlyPlaying) {
            pausePlayback()
        } else {
            if (exoPlayer == null) {
                playCurrentStation()
            } else {
                exoPlayer?.play()
                _isPlaying.value = true
                _currentTrackName.value = "Canlı Yayın"
                startService()
                startLiveMetadataMonitoring(_currentStation.value.streamUrl)
            }
        }
    }

    private fun playCurrentStation() {
        releaseMediaPlayer()

        _isBuffering.value = true
        _isPlaying.value = false

        val streamUrl = _currentStation.value.streamUrl
        fetchStreamMetadata(streamUrl)

        viewModelScope.launch(Dispatchers.Main) {
            try {
                // Configure robust buffering parameters specifically for live radio streaming
                val loadControl = DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        35_000, // 35 seconds of minimum buffered media before playing can be started/resumed
                        100_000, // 100 seconds of maximum buffer size
                        2_500,  // 2.5 seconds of initial buffer required before starting playback (smooth start!)
                        5_000   // 5.0 seconds of buffer required after alert/rebuffering (prevents rapid stutter loops!)
                    )
                    .build()

                // Crucial step: Configure HTTP DataSource to allow cross-protocol redirects (HTTPS -> HTTP) 
                // and configure a generic browser User-Agent to stop servers from rejecting connection requests.
                val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(15_000)
                    .setReadTimeoutMs(25_000)
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36")

                val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(getApplication<Application>())
                    .setDataSourceFactory(httpDataSourceFactory)

                val player = ExoPlayer.Builder(getApplication())
                    .setLoadControl(loadControl)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .setWakeMode(C.WAKE_MODE_NETWORK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .setUsage(C.USAGE_MEDIA)
                            .build(),
                        false // Explicitly false so it does NOT pause / change playback on incoming phone notifications
                    )
                    .build()

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                _isBuffering.value = true
                                _currentTrackName.value = "Yükleniyor..."
                            }
                            Player.STATE_READY -> {
                                _isBuffering.value = false
                                _isPlaying.value = true
                                reconnectAttempts = 0
                                _currentTrackName.value = "Canlı Yayın"
                                startService()
                                startLiveMetadataMonitoring(streamUrl)
                            }
                            Player.STATE_ENDED -> {
                                _isBuffering.value = false
                                _isPlaying.value = false
                                stopService()
                                handleReconnect()
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        _isBuffering.value = false
                        _isPlaying.value = false
                        stopService()
                        handleReconnect()
                    }

                    override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                        val title = mediaMetadata.title?.toString()
                        if (!title.isNullOrEmpty() && title != "Yükleniyor..." && title != "Canlı Yayın") {
                            updateTrackNameAndFetchArtwork(title)
                        }
                    }

                    override fun onMetadata(metadata: androidx.media3.common.Metadata) {
                        for (i in 0 until metadata.length()) {
                            val entry = metadata.get(i)
                            if (entry is androidx.media3.extractor.metadata.icy.IcyInfo) {
                                val title = entry.title
                                if (!title.isNullOrEmpty()) {
                                    updateTrackNameAndFetchArtwork(title)
                                }
                            }
                        }
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        for (group in tracks.groups) {
                            if (group.type == C.TRACK_TYPE_AUDIO) {
                                for (i in 0 until group.length) {
                                    if (group.isTrackSelected(i)) {
                                        val format = group.getTrackFormat(i)
                                        val bps = format.bitrate
                                        if (bps > 0 && bps != androidx.media3.common.Format.NO_VALUE) {
                                            val kbps = bps / 1000
                                            _streamBitrate.value = "$kbps kbps"
                                        } else {
                                            val urlLower = streamUrl.lowercase()
                                            val bitrateVal = when {
                                                urlLower.contains("powerturk") -> "280 kbps"
                                                urlLower.contains("kesintisizyayin") || urlLower.contains("karnaval") -> "96 kbps"
                                                else -> "128 kbps"
                                            }
                                            _streamBitrate.value = bitrateVal
                                        }
                                        val mime = format.sampleMimeType
                                        val codec = when {
                                            mime != null && (mime.contains("aac") || mime.contains("mp4a")) -> "AAC"
                                            mime != null && (mime.contains("mpeg") || mime.contains("mp3")) -> "MP3"
                                            mime != null && (mime.contains("ogg") || mime.contains("opus") || mime.contains("vorbis")) -> "OGG"
                                            else -> {
                                                val urlLower = streamUrl.lowercase()
                                                val defaultCodec = when {
                                                    urlLower.contains(".m3u8") || urlLower.contains("aac") || urlLower.contains(".aac") || urlLower.contains("mp4") -> "AAC"
                                                    urlLower.contains("ogg") || urlLower.contains("opus") -> "OGG"
                                                    else -> "MP3"
                                                }
                                                defaultCodec
                                            }
                                        }
                                        _streamCodec.value = codec
                                    }
                                }
                            }
                        }
                    }
                })

                val mediaItem = MediaItem.fromUri(streamUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
                
                exoPlayer = player
            } catch (e: Exception) {
                _isBuffering.value = false
                _isPlaying.value = false
                _currentTrackName.value = "Yayın Başlatılamadı (Hata)"
            }
        }
    }

    private fun handleReconnect() {
        if (reconnectAttempts < 10) { // Increased threshold for better self-healing robustness
            reconnectAttempts++
            _currentTrackName.value = "Yayın Koptu, Yeniden Bağlanılıyor (Deneme $reconnectAttempts/10)..."
            reconnectJob?.cancel()
            reconnectJob = viewModelScope.launch {
                delay(3000)
                playCurrentStation()
            }
        } else {
            _currentTrackName.value = "Bağlantı Hatası: Lütfen internetinizi kontrol edin."
        }
    }

    private fun pausePlayback() {
        exoPlayer?.pause()
        _isPlaying.value = false
        stopService()
        liveMetadataJob?.cancel()
        liveMetadataJob = null
    }

    private fun releaseMediaPlayer() {
        reconnectJob?.cancel()
        reconnectJob = null
        stopService()
        liveMetadataJob?.cancel()
        liveMetadataJob = null
        exoPlayer?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        exoPlayer = null
        _isPlaying.value = false
        _isBuffering.value = false
    }

    // Sleep Timer Controls
    fun setSleepTimerPreset(minutes: Int) {
        _sleepTimerPreset.value = minutes
        if (minutes > 0) {
            _customSleepSliderValue.value = minutes.toFloat()
            startSleepTimer(minutes)
        } else {
            cancelSleepTimer()
        }
    }

    fun adjustCustomSleepSlider(minutes: Float) {
        _customSleepSliderValue.value = minutes
        _sleepTimerPreset.value = -1 // marked as custom
    }

    fun startCustomSleepTimer() {
        val minutes = _customSleepSliderValue.value.toInt()
        startSleepTimer(minutes)
    }

    private fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        _sleepTimerMinutesLeft.value = minutes
        _sleepTimerSecondsLeft.value = 0
        _sleepTimerActive.value = true

        sleepTimerJob = viewModelScope.launch {
            var totalSeconds = minutes * 60
            while (totalSeconds > 0 && _sleepTimerActive.value) {
                delay(1000)
                totalSeconds--
                _sleepTimerMinutesLeft.value = totalSeconds / 60
                _sleepTimerSecondsLeft.value = totalSeconds % 60
            }
            if (_sleepTimerActive.value) {
                // Timer finished!
                pausePlayback()
                cancelSleepTimer()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerActive.value = false
        _sleepTimerMinutesLeft.value = 0
        _sleepTimerSecondsLeft.value = 0
        _sleepTimerPreset.value = -1
    }

    private fun updateTrackMetadata() {
        _currentTrackName.value = "Yükleniyor..."
        _currentTrackArtwork.value = null
    }

    private fun updateTrackNameAndFetchArtwork(title: String) {
        val cleaned = cleanTrackName(title)
        if (cleaned.isNotEmpty() && cleaned != "Yükleniyor..." && cleaned != "Canlı Yayın") {
            if (cleaned != _currentTrackName.value) {
                _currentTrackName.value = cleaned
                fetchTrackArtwork(cleaned)
            }
        }
    }

    private fun cleanTrackName(title: String): String {
        var clean = title.trim()
        if (clean.startsWith("StreamTitle='")) {
            clean = clean.replace("StreamTitle='", "").replace("';", "")
        }
        return clean.trim()
    }

    private fun fetchIcyStreamTitle(streamUrl: String): String? {
        var urlStr = streamUrl
        var redirects = 0
        while (redirects < 5) {
            var connection: java.net.HttpURLConnection? = null
            var inputStream: java.io.InputStream? = null
            try {
                val url = java.net.URL(urlStr)
                connection = url.openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty("Icy-MetaData", "1")
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36")
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                connection.instanceFollowRedirects = true
                
                val status = connection.responseCode
                if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP || 
                    status == java.net.HttpURLConnection.HTTP_MOVED_PERM || 
                    status == 307 || status == 308) {
                    val newUrl = connection.getHeaderField("Location")
                    if (!newUrl.isNullOrEmpty()) {
                        urlStr = newUrl
                        redirects++
                        connection.disconnect()
                        continue
                    }
                }

                val metaIntStr = connection.getHeaderField("icy-metaint")
                if (metaIntStr != null) {
                    val metaInt = metaIntStr.toIntOrNull() ?: 0
                    if (metaInt > 0) {
                        inputStream = connection.inputStream
                        val buffer = ByteArray(metaInt)
                        var bytesRead = 0
                        while (bytesRead < metaInt) {
                            val read = inputStream.read(buffer, 0, metaInt - bytesRead)
                            if (read == -1) break
                            bytesRead += read
                        }
                        
                        val metaLengthByte = inputStream.read()
                        if (metaLengthByte > 0) {
                            val metaLength = metaLengthByte * 16
                            val metaBuffer = ByteArray(metaLength)
                            var metaBytesRead = 0
                            while (metaBytesRead < metaLength) {
                                val read = inputStream.read(metaBuffer, metaBytesRead, metaLength - metaBytesRead)
                                if (read == -1) break
                                metaBytesRead += read
                            }
                            val metadata = String(metaBuffer, 0, metaBytesRead, Charsets.UTF_8)
                            if (metadata.contains("StreamTitle=")) {
                                val match = Regex("""StreamTitle='([^']*)'""").find(metadata)
                                val title = match?.groupValues?.get(1)
                                if (!title.isNullOrEmpty()) {
                                    return title
                                }
                            }
                        }
                    }
                }
                return null
            } catch (e: Exception) {
                return null
            } finally {
                try { inputStream?.close() } catch (e: Exception) {}
                try { connection?.disconnect() } catch (e: Exception) {}
            }
        }
        return null
    }

    private fun fetchTrackArtwork(trackName: String) {
        _currentTrackArtwork.value = null
        val cleanTrack = trackName.trim()
        val ignoreList = listOf("yükleniyor", "hata", "yayın başlatılamadı", "canlı yayın", "canlı radyo", "radyo", "yayın koptu", "bağlantı hatası")
        if (cleanTrack.isEmpty() || ignoreList.any { cleanTrack.lowercase().contains(it) }) {
            return
        }

        viewModelScope.launch {
            try {
                val artworkUrl = withContext(Dispatchers.IO) {
                    val encoded = java.net.URLEncoder.encode(cleanTrack, "UTF-8")
                    val urlStr = "https://itunes.apple.com/search?entity=song&limit=1&term=$encoded"
                    val connection = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 4000
                    connection.readTimeout = 4000
                    connection.inputStream.use { stream ->
                        val response = stream.bufferedReader().use { it.readText() }
                        val regex = Regex("""\"artworkUrl100\"\s*:\s*\"([^\"]+)\"""")
                        val match = regex.find(response)
                        val url100 = match?.groups?.get(1)?.value
                        url100?.replace("100x100bb", "500x500bb")
                    }
                }
                if (artworkUrl != null) {
                    _currentTrackArtwork.value = artworkUrl
                } else {
                    _currentTrackArtwork.value = null
                }
            } catch (e: Exception) {
                _currentTrackArtwork.value = null
                e.printStackTrace()
            }
        }
    }

    private fun startLiveMetadataMonitoring(streamUrl: String) {
        liveMetadataJob?.cancel()
        liveMetadataJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val streamTitle = fetchIcyStreamTitle(streamUrl)
                if (!streamTitle.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        updateTrackNameAndFetchArtwork(streamTitle)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }

            while (true) {
                delay(35000)
                if (_isPlaying.value) {
                    try {
                        val streamTitle = fetchIcyStreamTitle(streamUrl)
                        if (!streamTitle.isNullOrEmpty()) {
                            withContext(Dispatchers.Main) {
                                updateTrackNameAndFetchArtwork(streamTitle)
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    private fun fetchStreamMetadata(streamUrl: String) {
        val urlLower = streamUrl.lowercase()
        val codec = when {
            urlLower.contains(".m3u8") || urlLower.contains("aac") || urlLower.contains(".aac") || urlLower.contains("mp4") -> "AAC"
            urlLower.contains("ogg") || urlLower.contains("opus") -> "OGG"
            else -> "MP3"
        }
        val bitrate = when {
            urlLower.contains("powerturk") -> "280 KBPS"
            urlLower.contains("kesintisizyayin") || urlLower.contains("karnaval") -> "96 KBPS"
            else -> "128 KBPS"
        }
        _streamCodec.value = codec
        _streamBitrate.value = bitrate
    }

    fun setGoogleSheetId(id: String) {
        val cleanId = id.trim()
        _googleSheetId.value = cleanId
        sharedPrefs.edit().putString("google_sheet_id", cleanId).apply()
        loadStations()
    }

    fun loadStations(onComplete: ((Boolean) -> Unit)? = null) {
        _isSheetLoading.value = true
        _sheetError.value = null
        
        viewModelScope.launch {
            try {
                val fetched = loadStationsFromGoogleSheet(_googleSheetId.value)
                if (fetched.isNotEmpty()) {
                    _stations.value = fetched
                    // Extract unique genres dynamically
                    val uniqueGenres = fetched.map { it.genre }.distinct().filter { it.isNotEmpty() }.sorted()
                    _genres.value = listOf("Tümü") + uniqueGenres

                    // If the current station is not in the fetched list, select the first fetched station!
                    val exists = fetched.any { it.id == _currentStation.value.id || it.streamUrl == _currentStation.value.streamUrl }
                    if (!exists && fetched.isNotEmpty()) {
                        _currentStation.value = fetched.first()
                        updateTrackMetadata()
                    } else {
                        updateTrackMetadata()
                    }
                    _sheetError.value = null
                    onComplete?.invoke(true)
                } else {
                    _sheetError.value = "Google Sheet boş veya geçersiz formatta"
                    _stations.value = emptyList()
                    _genres.value = listOf("Tümü")
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                _sheetError.value = "Radyo listesi Google Sheet'ten alınamadı: ${e.localizedMessage ?: "Bağlantı hatası"}"
                _stations.value = emptyList()
                _genres.value = listOf("Tümü")
                onComplete?.invoke(false)
            } finally {
                _isSheetLoading.value = false
            }
        }
    }

    private fun String.cleanCsvField(): String {
        var s = this.trim()
        while (s.startsWith("\"") && s.endsWith("\"") && s.length >= 2) {
            s = s.substring(1, s.length - 1).trim()
        }
        while (s.startsWith("\'") && s.endsWith("\'") && s.length >= 2) {
            s = s.substring(1, s.length - 1).trim()
        }
        return s.trim()
    }

    private suspend fun loadStationsFromGoogleSheet(sheetId: String): List<RadioStation> {
        return withContext(Dispatchers.IO) {
            val urlStr = "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv"
            val url = java.net.URL(urlStr)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.useCaches = false
            
            val status = connection.responseCode
            if (status != 200) {
                throw IOException("Google Sheets sunucusundan veri çekilemedi: Hata $status")
            }
            
            val stations = mutableListOf<RadioStation>()
            connection.inputStream.bufferedReader(Charsets.UTF_8).useLines { lines ->
                var isFirstLine = true
                var index = 1
                lines.forEach { line ->
                    if (isFirstLine) {
                        isFirstLine = false
                        return@forEach
                    }
                    val row = parseCsvLine(line)
                    if (row.size >= 3) {
                        val name = row.getOrNull(0)?.cleanCsvField() ?: ""
                        val streamUrl = row.getOrNull(1)?.cleanCsvField() ?: ""
                        val genre = row.getOrNull(2)?.cleanCsvField() ?: ""
                        if (name.isNotEmpty() && streamUrl.isNotEmpty()) {
                            val logoUrl = row.getOrNull(3)?.cleanCsvField() ?: ""
                            val initialsRaw = row.getOrNull(4)?.cleanCsvField() ?: ""
                            val initials = if (initialsRaw.isNotEmpty()) initialsRaw else name.take(2).uppercase()
                            val gradStartStr = row.getOrNull(5)?.cleanCsvField() ?: ""
                            val gradEndStr = row.getOrNull(6)?.cleanCsvField() ?: ""
                            
                            val gradStart = gradStartStr.toLongColorOr(0xFF1E3C72)
                            val gradEnd = gradEndStr.toLongColorOr(0xFF2A5298)
                            
                            stations.add(
                                RadioStation(
                                    id = index++,
                                    name = name,
                                    streamUrl = streamUrl,
                                    genre = genre,
                                    initials = initials,
                                    gradientStart = gradStart,
                                    gradientEnd = gradEnd,
                                    logoUrl = if (logoUrl.startsWith("http", ignoreCase = true)) logoUrl else null
                                )
                            )
                        }
                    }
                }
            }
            stations
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val currentField = java.lang.StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                inQuotes = !inQuotes
            } else if ((c == ',' || c == ';') && !inQuotes) {
                result.add(currentField.toString())
                currentField.setLength(0)
            } else {
                currentField.append(c)
            }
            i++
        }
        result.add(currentField.toString())
        return result
    }

    private fun String.toLongColorOr(defaultColor: Long): Long {
        val clean = this.uppercase().trim().replace("#", "").replace("0X", "")
        if (clean.isEmpty()) return defaultColor
        return try {
            if (clean.length == 6) {
                ("FF$clean").toLong(16)
            } else if (clean.length == 8) {
                clean.toLong(16)
            } else {
                defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
        cancelSleepTimer()
        metadataJob?.cancel()
        streamInfoJob?.cancel()
    }
}
