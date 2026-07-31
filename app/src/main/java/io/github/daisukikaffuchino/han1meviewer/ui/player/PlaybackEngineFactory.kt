package io.github.daisukikaffuchino.han1meviewer.ui.player

import android.content.Context

object PlaybackEngineFactory {
    fun create(context: Context, kernel: PlayerKernel): PlaybackEngine = when (kernel) {
        PlayerKernel.MediaPlayer -> SystemPlaybackEngine(context)
        PlayerKernel.ExoPlayer -> ExoPlaybackEngine(context)
        PlayerKernel.MpvPlayer -> MpvPlaybackEngine(context)
    }
}
