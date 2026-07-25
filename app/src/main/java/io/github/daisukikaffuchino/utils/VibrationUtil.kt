package io.github.daisukikaffuchino.utils

import android.view.HapticFeedbackConstants
import android.view.View
import io.github.daisukikaffuchino.han1meviewer.Preferences

object VibrationUtil {
    fun performHapticFeedback(
        view: View,
        feedbackConstant: Int = HapticFeedbackConstants.CONTEXT_CLICK,
    ) {
        if (Preferences.hapticFeedbackEnabled) {
            view.performHapticFeedback(feedbackConstant)
        }
    }
}
