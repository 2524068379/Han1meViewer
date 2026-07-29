package io.github.daisukikaffuchino.han1meviewer.ui.player

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.Surface
import androidx.core.net.toUri
import io.github.daisukikaffuchino.han1meviewer.BuildConfig
import io.github.daisukikaffuchino.han1meviewer.Preferences
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT
import io.github.daisukikaffuchino.han1meviewer.logic.network.HProxySelector
import io.github.daisukikaffuchino.han1meviewer.util.AnimeShaders
import io.github.daisukikaffuchino.han1meviewer.util.AnimeShaders.getCert
import `is`.xyz.mpv.MPVLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MpvPlaybackEngine(
    private val context: Context,
) : PlaybackEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(PlaybackEngineState())
    private var currentSurface: Surface? = null
    private var currentPfd: ParcelFileDescriptor? = null
    private var detachedFd: Int? = null
    private var pendingRequest: PlaybackRequest? = null
    private var initialized = false
    private var released = false
    private var lastVideoWidth = 0
    private var lastVideoHeight = 0
    private var hasRenderedFrame = false
    private val observer = object : MPVLib.EventObserver {
        override fun eventProperty(property: String) = publishState()
        override fun eventProperty(property: String, value: Double) = publishState()
        override fun eventProperty(property: String, value: Long) = publishState()
        override fun eventProperty(property: String, value: Boolean) = publishState()
        override fun eventProperty(property: String, value: String) = publishState()

        override fun event(eventId: Int) {
            when (eventId) {
                MPVLib.mpvEventId.MPV_EVENT_START_FILE -> {
                    mutableState.value = mutableState.value.copy(
                        phase = PlaybackPhase.Preparing,
                        isBuffering = true,
                        errorMessage = null,
                    )
                }

                MPVLib.mpvEventId.MPV_EVENT_FILE_LOADED -> {
                    pendingRequest?.let { request ->
                        MPVLib.setPropertyDouble("speed", requestSpeed.toDouble())
                        if (request.startPositionMs > 0L) {
                            seekTo(request.startPositionMs)
                        }
                        if (request.playWhenReady) startPlayback()
                    }
                    mutableState.value = mutableState.value.copy(
                        phase = PlaybackPhase.Ready,
                        isBuffering = false,
                    )
                }

                MPVLib.mpvEventId.MPV_EVENT_END_FILE -> {
                    mutableState.value = mutableState.value.copy(
                        phase = PlaybackPhase.Ended,
                        isPlaying = false,
                        isBuffering = false,
                    )
                }

                MPVLib.mpvEventId.MPV_EVENT_SHUTDOWN -> {
                    mutableState.value = PlaybackEngineState()
                }
            }
        }
    }
    private var requestSpeed = PlayerDefaults.DEFAULT_SPEED

    override val state: StateFlow<PlaybackEngineState> = mutableState.asStateFlow()

    override fun load(request: PlaybackRequest) {
        check(!released) { "Playback engine has already been released" }
        initializeIfNeeded()
        pendingRequest = request
        lastVideoWidth = 0
        lastVideoHeight = 0
        hasRenderedFrame = false
        MPVLib.setPropertyBoolean("pause", true)
        MPVLib.command(arrayOf("loadfile", "", "replace"))
        val path = prepareUri(request.uri.toUri())
        if (path == null) {
            mutableState.value = mutableState.value.copy(
                phase = PlaybackPhase.Error,
                errorMessage = "Unable to open media URI",
            )
            return
        }
        MPVLib.setOptionString("force-window", "yes")
        MPVLib.command(arrayOf("loadfile", path, "replace"))
        currentSurface?.let(MPVLib::attachSurface)
        mutableState.value = mutableState.value.copy(
            phase = PlaybackPhase.Preparing,
            isBuffering = true,
            errorMessage = null,
            videoWidth = 0,
            videoHeight = 0,
            hasRenderedFirstFrame = false,
        )
    }

    override fun play() = startPlayback()

    override fun pause() {
        MPVLib.setPropertyBoolean("pause", true)
        publishState()
    }

    override fun seekTo(positionMs: Long) {
        MPVLib.command(arrayOf("seek", (positionMs.coerceAtLeast(0L) / 1000.0).toString(), "absolute", "exact"))
        publishState()
    }

    override fun setPlaybackSpeed(speed: Float) {
        requestSpeed = speed.coerceIn(0.25f, 5f)
        MPVLib.setPropertyDouble("speed", requestSpeed.toDouble())
        publishState()
    }

    override fun setVolume(volume: Float) {
        MPVLib.setPropertyDouble("volume", (volume.coerceIn(0f, 1f) * 100f).toDouble())
    }

    override fun attachSurface(surface: Surface) {
        currentSurface = surface
        if (initialized) {
            MPVLib.attachSurface(surface)
            MPVLib.setOptionString("force-window", "yes")
            MPVLib.setPropertyString("vo", videoOutput)
        }
    }

    override fun detachSurface(surface: Surface) {
        if (currentSurface == surface) {
            currentSurface = null
            if (initialized) {
                MPVLib.setPropertyString("vo", "null")
                MPVLib.setOptionString("force-window", "no")
                MPVLib.detachSurface()
            }
        }
    }

    fun updateSurfaceSize(width: Int, height: Int) {
        if (!initialized || released) return
        if (width > 0 && height > 0) {
            MPVLib.setPropertyString("android-surface-size", "${width}x${height}")
            if (MPVLib.getPropertyBoolean("pause") == true) {
                MPVLib.command(arrayOf("seek", "0", "relative", "exact"))
            }
        }
    }

    fun setSuperResolution(index: Int) {
        val shader = AnimeShaders.getShader(context, index)
        MPVLib.command(arrayOf("change-list", "glsl-shaders", "set", shader))
    }

    override fun release() {
        if (released) return
        released = true
        if (initialized) {
            MPVLib.setPropertyBoolean("pause", true)
            MPVLib.command(arrayOf("loadfile", "", "replace"))
            MPVLib.setOptionString("force-window", "no")
            MPVLib.detachSurface()
            MPVLib.removeObserver(observer)
        }
        closeCurrentFile()
        scope.cancel()
        mutableState.value = PlaybackEngineState()
    }

    private fun initializeIfNeeded() {
        if (initialized) return
        mpvOptions().forEach { (key, value) -> MPVLib.setOptionString(key, value) }
        parseCustomMpvParams().forEach { (key, value) -> MPVLib.setOptionString(key, value) }
        MPVLib.observeProperty("time-pos", MPVLib.mpvFormat.MPV_FORMAT_DOUBLE)
        MPVLib.observeProperty("duration", MPVLib.mpvFormat.MPV_FORMAT_DOUBLE)
        MPVLib.observeProperty("pause", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
        MPVLib.observeProperty("video-params/w", MPVLib.mpvFormat.MPV_FORMAT_INT64)
        MPVLib.observeProperty("video-params/h", MPVLib.mpvFormat.MPV_FORMAT_INT64)
        MPVLib.observeProperty("demuxer-cache-duration", MPVLib.mpvFormat.MPV_FORMAT_DOUBLE)
        MPVLib.addObserver(observer)
        initialized = true
        scope.launch {
            while (isActive) {
                publishState()
                delay(250L.milliseconds)
            }
        }
    }

    private fun startPlayback() {
        MPVLib.setPropertyBoolean("pause", false)
        publishState()
    }

    private fun publishState() {
        if (!initialized || released) return
        val position = MPVLib.getPropertyDouble("time-pos") ?: 0.0
        val duration = MPVLib.getPropertyDouble("duration") ?: 0.0
        val buffered = MPVLib.getPropertyDouble("demuxer-cache-duration") ?: 0.0
        val paused = MPVLib.getPropertyBoolean("pause") ?: true
        val width = MPVLib.getPropertyInt("video-params/w") ?: 0
        val height = MPVLib.getPropertyInt("video-params/h") ?: 0
        if (width > 0 && height > 0) {
            lastVideoWidth = width
            lastVideoHeight = height
            hasRenderedFrame = true
        }
        mutableState.value = mutableState.value.copy(
            isPlaying = !paused,
            isBuffering = !paused && duration > 0 && position == 0.0,
            positionMs = (position * 1000).toLong().coerceAtLeast(0L),
            durationMs = (duration * 1000).toLong().coerceAtLeast(0L),
            bufferedPositionMs = ((position + buffered) * 1000).toLong().coerceAtLeast(0L),
            playbackSpeed = requestSpeed,
            videoWidth = lastVideoWidth,
            videoHeight = lastVideoHeight,
            hasRenderedFirstFrame = hasRenderedFrame,
        )
    }

    private fun prepareUri(uri: Uri): String? {
        return when (uri.scheme) {
            "http", "https" -> uri.toString()
            "file", "content" -> {
                closeCurrentFile()
                currentPfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
                detachedFd = currentPfd?.detachFd()
                detachedFd?.let { "fd://$it" }
            }
            else -> null
        }
    }

    private fun closeCurrentFile() {
        currentPfd?.close()
        detachedFd?.let { runCatching { ParcelFileDescriptor.adoptFd(it).close() } }
        currentPfd = null
        detachedFd = null
    }

    private fun mpvOptions(): Map<String, String> = buildMap {
        put("vo", videoOutput)
        put("profile", Preferences.mpvProfile.takeIf { it == "gpu-hq" || it == "fast" } ?: "default")
        put("hwdec", when (Preferences.mpvHwdec) {
            "HW" -> "mediacodec-copy"
            "HW+" -> "mediacodec"
            "Vulkan" -> "vulkan-copy"
            "vulkan+" -> "vulkan"
            "SW" -> "no"
            else -> "auto"
        })
        put("msg-level", "all=" + if (BuildConfig.DEBUG) "debug" else "warn")
        put("cache", "yes")
        put("cache-secs", Preferences.mpvCacheSecs.toString())
        put("vd-lavc-threads", Runtime.getRuntime().availableProcessors().toString())
        put("framedrop", if (Preferences.mpvFramedrop) "vo" else "no")
        put("deband", if (Preferences.mpvDeband) "yes" else "no")
        put("cache-pause", "no")
        put("network-timeout", Preferences.mpvNetworkTimeout.toString())
        put("tls-ca-file", getCert(context))
        put("tls-verify", if (Preferences.mpvTlsVerify) "no" else "yes")
        put("user-agent", USER_AGENT)
        Preferences.proxyIp.takeIf { it.isNotBlank() && Preferences.proxyPort != -1 }?.let { ip ->
            if (Preferences.proxyType == HProxySelector.TYPE_HTTP) {
                put("http-proxy", "http://$ip:${Preferences.proxyPort}")
            }
        }
        if (Preferences.mpvInterpolation) {
            put("interpolation", "yes")
            put("tscale", "oversample")
            put("video-sync", "display-resample")
        }
    }

    private fun parseCustomMpvParams(): Map<String, String> = buildMap {
        Preferences.customMpvParams.split(';').forEach { entry ->
            val parts = entry.trim().split(',', limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                put(parts[0].trim(), parts[1].trim())
            }
        }
    }

    private val videoOutput: String
        get() = if (Preferences.enableGPUNextRenderer) "gpu-next" else "gpu"
}
