package io.github.daisukikaffuchino.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import io.github.daisukikaffuchino.han1meviewer.HANIME_LOGIN_URL
import io.github.daisukikaffuchino.han1meviewer.HanimeConstants.HANIME_URL
import io.github.daisukikaffuchino.han1meviewer.Preferences.cloudFlareCookie
import io.github.daisukikaffuchino.han1meviewer.Preferences.cloudFlareCookieHost
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.USER_AGENT
import io.github.daisukikaffuchino.han1meviewer.logic.NetworkRepo
import io.github.daisukikaffuchino.han1meviewer.logic.network.CloudflareVerificationCoordinator
import io.github.daisukikaffuchino.han1meviewer.logic.state.WebsiteState
import io.github.daisukikaffuchino.han1meviewer.login
import io.github.daisukikaffuchino.han1meviewer.ui.screen.login.LoginDialog
import io.github.daisukikaffuchino.han1meviewer.ui.screen.login.LoginScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.login.ManualInputCookiesScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.web.CloudflareScreen
import io.github.daisukikaffuchino.han1meviewer.util.CookieString
import io.github.daisukikaffuchino.utils.LogUtil
import io.github.daisukikaffuchino.utils.SonnerToast
import kotlinx.coroutines.launch

class AuthActivity : BaseActivity() {

    companion object {
        private const val EXTRA_MODE = "auth_mode"
        private const val EXTRA_URL = "request_url"
        private const val EXTRA_VERIFICATION_HOST = "verification_host"
        private const val EXTRA_COOKIE = "cookie"

        fun loginIntent(context: Context): Intent =
            createIntent(context, AuthMode.Login)

        fun cloudflareIntent(context: Context, url: String, host: String): Intent =
            createIntent(context, AuthMode.Cloudflare).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_VERIFICATION_HOST, host)
            }

        private fun manualCookiesIntent(context: Context): Intent =
            createIntent(context, AuthMode.ManualCookies)

        private fun createIntent(context: Context, mode: AuthMode): Intent =
            Intent(context, AuthActivity::class.java).putExtra(EXTRA_MODE, mode.value)
    }

    private var mode = AuthMode.Login
    private var webView: WebView? = null

    private lateinit var cookieInputLauncher: ActivityResultLauncher<Intent>
    private var isRefreshing by mutableStateOf(true)
    private var showLoginDialog by mutableStateOf(false)
    private var isLoggingIn by mutableStateOf(false)

    private val progressState = mutableIntStateOf(0)
    private val tipTextState = mutableStateOf("")
    private var verificationHost: String? = null
    private var verificationCompleted = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mode = AuthMode.fromValue(intent.getStringExtra(EXTRA_MODE))
        title = getString(
            when (mode) {
                AuthMode.Login -> R.string.login
                AuthMode.Cloudflare -> R.string.complete_cloudflare_verification
                AuthMode.ManualCookies -> R.string.title_activity_qrcode_scanner
            }
        )

        when (mode) {
            AuthMode.Login -> showLoginContent()
            AuthMode.Cloudflare -> showCloudflareContent()
            AuthMode.ManualCookies -> showManualCookiesContent()
        }
    }

    private fun showLoginContent() {
        cookieInputLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val cookie = result.data?.getStringExtra(EXTRA_COOKIE)
                LogUtil.i("AuthActivity", "Manual cookie result: $cookie")
                login(cookie.toString())
                setResult(RESULT_OK)
                finish()
            }
        }

        setHanimeContent {
            if (showLoginDialog) {
                LoginDialog(
                    isLoggingIn = isLoggingIn,
                    onDismiss = { showLoginDialog = false },
                    onLogin = { username, password -> handleLogin(username, password) },
                )
            }
            LoginScreen(
                isRefreshing = isRefreshing,
                onBack = { onBackPressedDispatcher.onBackPressed() },
                onRefresh = { webView?.loadUrl(HANIME_LOGIN_URL) },
                onOpenQrScanner = {
                    cookieInputLauncher.launch(manualCookiesIntent(this))
                },
                webViewFactory = ::createLoginWebView,
            )
        }
    }

    private fun showCloudflareContent() {
        val url = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }
        verificationHost = intent.getStringExtra(EXTRA_VERIFICATION_HOST)
            ?: url.toUri().host?.lowercase()
        tipTextState.value = getString(R.string.complete_cloudflare_verification_with_warning)

        setHanimeContent {
            CloudflareScreen(
                progress = progressState.intValue,
                tipText = tipTextState.value,
                onClose = { finish() },
                webViewFactory = { createCloudflareWebView(url) },
            )
        }
    }

    private fun showManualCookiesContent() {
        setHanimeContent {
            ManualInputCookiesScreen(
                onBack = { finish() },
                onCookieScanned = { scannedCookie ->
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_COOKIE, scannedCookie)
                        LogUtil.i("AuthActivity", scannedCookie)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                },
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createLoginWebView(): WebView = WebView(this).apply {
        webView = this
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.userAgentString = USER_AGENT

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                isRefreshing = false
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val isSameUrl = HANIME_URL.contains(request.url.toString())
                if (request.isRedirect && isSameUrl) {
                    val url = request.url
                    val cookies = CookieManager.getInstance().getCookie(url.host)
                    LogUtil.d("login_cookie", cookies.toString())
                    login(cookies)
                    setResult(RESULT_OK)
                    finish()
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                if (request?.isForMainFrame == true && !isDestroyed && !isFinishing) {
                    isRefreshing = false
                    showLoginDialog = true
                }
            }
        }
        loadUrl(HANIME_LOGIN_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createCloudflareWebView(url: String): WebView = WebView(this).apply {
        webView = this
        val cloudflareWebView = this
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            javaScriptCanOpenWindowsAutomatically = true
            userAgentString = USER_AGENT
        }

        val cookieManager = CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(cloudflareWebView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean = false
        }

        evaluateJavascript("navigator.userAgent") { output ->
            updateWebViewVersionTip(output)
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressState.intValue = newProgress
                if (newProgress >= 90) {
                    view?.postDelayed({
                        view.evaluateJavascript("document.head.innerHTML") { html ->
                            if (!html.contains("#challenge-form") &&
                                !html.contains("#challenge-success-text") &&
                                !html.contains("#challenge-error-text")
                            ) {
                                completeCloudflareVerification(
                                    completedUrl = view.url ?: url,
                                    cookieManager = cookieManager,
                                )
                            }
                        }
                    }, 1000)
                }
            }
        }
        loadUrl(url)
    }

    private fun updateWebViewVersionTip(output: String) {
        val userAgent = output
            .removeSurrounding("\"")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
        val chromePattern = "Chrome/(\\d+\\.\\d+\\.\\d+\\.\\d+)".toRegex()
        val versionCode = chromePattern.find(userAgent)?.groupValues?.getOrNull(1) ?: userAgent
        runOnUiThread {
            var text = getString(R.string.complete_cloudflare_verification_with_warning)
            text += getString(R.string.current_webview_version, versionCode)
            text += try {
                val parts = versionCode.split(".").map { it.toIntOrNull() ?: 0 }
                when {
                    parts.size < 4 -> getString(R.string.webview_version_unknown)
                    parts[0] < 120 -> getString(R.string.webview_version_too_low)
                    else -> ""
                }
            } catch (_: Exception) {
                getString(R.string.version_check_failed)
            }
            tipTextState.value = text
        }
    }

    private fun completeCloudflareVerification(
        completedUrl: String,
        cookieManager: CookieManager,
    ) {
        val cookies = cookieManager.getCookie(completedUrl) ?: ""
        if (!cookies.containsCookie("cf_clearance")) return

        val cookieHost = completedUrl.toUri().host?.lowercase()
            ?: verificationHost
            ?: return
        cloudFlareCookie = CookieString(cookies)
        cloudFlareCookieHost = cookieHost
        cookieManager.flush()
        verificationCompleted = true
        CloudflareVerificationCoordinator.complete(
            verificationHost ?: cookieHost,
            succeeded = true,
        )
        finish()
    }

    private fun handleLogin(username: String, password: String) {
        isLoggingIn = true
        lifecycleScope.launch {
            NetworkRepo.login(username, password).collect { state ->
                when (state) {
                    WebsiteState.Loading -> Unit

                    is WebsiteState.Error -> {
                        isLoggingIn = false
                        state.throwable.printStackTrace()
                        if (state.throwable is IllegalStateException) {
                            SonnerToast.error(R.string.account_or_password_wrong)
                        } else {
                            SonnerToast.error(R.string.login_failed)
                        }
                    }

                    is WebsiteState.Success -> {
                        login(state.info)
                        setResult(RESULT_OK)
                        showLoginDialog = false
                        SonnerToast.success(R.string.login_success)
                        finish()
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mode == AuthMode.Login &&
            keyCode == KeyEvent.KEYCODE_BACK &&
            webView?.canGoBack() == true
        ) {
            webView?.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mode != AuthMode.Login) recreate()
    }

    override fun onDestroy() {
        if (mode == AuthMode.Cloudflare && !verificationCompleted && !isChangingConfigurations) {
            verificationHost?.let {
                CloudflareVerificationCoordinator.complete(it, succeeded = false)
            }
        }
        super.onDestroy()
        webView?.removeAllViews()
        webView?.destroy()
        webView = null
    }

    private fun String.containsCookie(name: String): Boolean =
        split(';').any { it.trim().substringBefore('=') == name }

    private enum class AuthMode(val value: String) {
        Login("login"),
        Cloudflare("cloudflare"),
        ManualCookies("manual_cookies");

        companion object {
            fun fromValue(value: String?): AuthMode =
                entries.firstOrNull { it.value == value } ?: Login
        }
    }
}
