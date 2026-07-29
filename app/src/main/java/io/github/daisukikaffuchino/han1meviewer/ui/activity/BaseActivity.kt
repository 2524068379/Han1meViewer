package io.github.daisukikaffuchino.han1meviewer.ui.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import io.github.daisukikaffuchino.han1meviewer.ui.theme.HanimeTheme
import io.github.daisukikaffuchino.han1meviewer.Preferences
import io.github.daisukikaffuchino.utils.SonnerToast

abstract class BaseActivity : AppCompatActivity() {

    protected open fun beforeSuperOnCreate(savedInstanceState: Bundle?) = Unit

    protected open fun onActivityCreated(savedInstanceState: Bundle?) = Unit

    final override fun onCreate(savedInstanceState: Bundle?) {
        beforeSuperOnCreate(savedInstanceState)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        setSecureMode(Preferences.secureMode)
    }

    fun setSecureMode(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    protected fun setHanimeContent(content: @Composable () -> Unit) {
        setContent {
            HanimeTheme {
                content()
                SonnerToast.Host()
            }
        }
    }
}
