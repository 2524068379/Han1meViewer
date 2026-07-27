package io.github.daisukikaffuchino.han1meviewer.ui.player

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.view.Surface
import io.github.daisukikaffuchino.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SystemPlaybackEngine(
    private val context: Context,
) : PlaybackEngine,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnVideoSizeChangedListener,
    MediaPlayer.OnErrorListener {
    private val mutableState = MutableStateFlow(PlaybackEngineState())
    private var mediaPlayer: MediaPlayer? = null
    private var currentSurface: Surface? = null
    private var pendingRequest: PlaybackRequest? = null
    private var released = false
    private var requestedSpeed = PlayerDefaults.DEFAULT_SPEED
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null

    override val state: StateFlow<PlaybackEngineState> = mutableState.asStateFlow()

    override fun load(request: PlaybackRequest) {
        check(!released) { "Playback engine has already been released" }
        pendingRequest = request
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener(this@SystemPlaybackEngine)
            setOnCompletionListener(this@SystemPlaybackEngine)
            setOnBufferingUpdateListener(this@SystemPlaybackEngine)
            setOnVideoSizeChangedListener(this@SystemPlaybackEngine)
            setOnErrorListener(this@SystemPlaybackEngine)
            currentSurface?.let(::setSurface)
            setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
            setDataSource(context, Uri.parse(request.uri), request.headers)
            isLooping = request.looping
            prepareAsync()
        }
        mutableState.value = PlaybackEngineState(
            phase = PlaybackPhase.Preparing,
            isBuffering = true,
        )
    }

    override fun play() {
        mediaPlayer?.start()
        publishPlaybackState()
    }

    override fun pause() {
        mediaPlayer?.pause()
        publishPlaybackState()
    }

    override fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt())
        publishPlaybackState()
    }

    override fun setPlaybackSpeed(speed: Float) {
        requestedSpeed = speed.coerceIn(0.25f, 5f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            mutableState.value.phase == PlaybackPhase.Ready
        ) {
            mediaPlayer?.playbackParams = PlaybackParams().setSpeed(requestedSpeed)
        }
        mutableState.value = mutableState.value.copy(playbackSpeed = requestedSpeed)
    }

    override fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
    }

    override fun attachSurface(surface: Surface) {
        currentSurface = surface
        mediaPlayer?.setSurface(surface)
    }

    override fun detachSurface(surface: Surface) {
        if (currentSurface == surface) currentSurface = null
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.setSurface(null)
        }
    }

    override fun release() {
        if (released) return
        released = true
        mediaPlayer?.setSurface(null)
        mediaPlayer?.release()
        mediaPlayer = null
        currentSurface = null
        pendingRequest = null
        progressJob?.cancel()
        scope.cancel()
        mutableState.value = PlaybackEngineState()
    }

    override fun onPrepared(player: MediaPlayer) {
        val request = pendingRequest ?: return
        if (request.startPositionMs > 0L) {
            player.seekTo(request.startPositionMs.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            player.playbackParams = PlaybackParams().setSpeed(requestedSpeed)
        }
        if (request.playWhenReady) player.start()
        mutableState.value = mutableState.value.copy(
            phase = PlaybackPhase.Ready,
            isPlaying = request.playWhenReady,
            isBuffering = false,
            durationMs = player.duration.toLong().coerceAtLeast(0L),
            hasRenderedFirstFrame = false,
        )
        startProgressUpdates()
    }

    override fun onCompletion(player: MediaPlayer) {
        progressJob?.cancel()
        mutableState.value = mutableState.value.copy(
            phase = PlaybackPhase.Ended,
            isPlaying = false,
            isBuffering = false,
            positionMs = player.duration.toLong().coerceAtLeast(0L),
        )
    }

    override fun onBufferingUpdate(player: MediaPlayer, percent: Int) {
        val duration = player.duration.takeIf { it > 0 } ?: return
        mutableState.value = mutableState.value.copy(
            bufferedPositionMs = duration.toLong() * percent.coerceIn(0, 100) / 100L,
            isBuffering = mutableState.value.phase == PlaybackPhase.Preparing && percent < 100,
        )
    }

    override fun onVideoSizeChanged(player: MediaPlayer, width: Int, height: Int) {
        mutableState.value = mutableState.value.copy(
            videoWidth = width,
            videoHeight = height,
            hasRenderedFirstFrame = width > 0 && height > 0,
        )
    }

    override fun onError(player: MediaPlayer, what: Int, extra: Int): Boolean {
        progressJob?.cancel()
        LogUtil.e(TAG, "Playback failed: what=$what extra=$extra")
        mutableState.value = mutableState.value.copy(
            phase = PlaybackPhase.Error,
            isPlaying = false,
            isBuffering = false,
            errorMessage = "MediaPlayer error $what/$extra",
        )
        return true
    }

    private fun publishPlaybackState() {
        val player = mediaPlayer ?: return
        mutableState.value = mutableState.value.copy(
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.toLong().coerceAtLeast(0L),
            durationMs = player.duration.toLong().coerceAtLeast(0L),
        )
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive) {
                publishPlaybackState()
                delay(250L)
            }
        }
    }

    private companion object {
        const val TAG = "SystemPlaybackEngine"
    }
}
