package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import android.content.Intent
import kotlinx.serialization.json.Json

private val loginRequiredDrawerItems = setOf(
    MainDrawerDestination.FavVideo,
    MainDrawerDestination.WatchLater,
    MainDrawerDestination.Playlist,
    MainDrawerDestination.Subscription,
)

const val EXTRA_OPEN_DAILY_CHECK_IN = "openDailyCheckIn"

fun TopLevelBackStack<HanimeScreen>.navigateDrawerDestination(
    destination: MainDrawerDestination,
    isLoggedIn: Boolean,
    onRequireLogin: () -> Unit,
): Boolean {
    if (destination in loginRequiredDrawerItems && !isLoggedIn) {
        onRequireLogin()
        return false
    }

    addTopLevel(destination.route)
    return true
}

fun TopLevelBackStack<HanimeScreen>.handleMainIntent(intent: Intent) {
    if (intent.action == Intent.ACTION_VIEW) {
        val uri = intent.data ?: return
        when (uri.scheme) {
            "http", "https" -> {
                val videoCode = uri.getQueryParameter("v")
                if (videoCode != null) {
                    add(VideoRoute(videoCode))
                }
            }

            "file", "content" -> {
                add(VideoRoute("-1", uri.toString()))
            }
        }
        return
    }

    if (intent.getBooleanExtra(EXTRA_OPEN_DAILY_CHECK_IN, false)) {
        intent.removeExtra(EXTRA_OPEN_DAILY_CHECK_IN)
        add(DailyCheckInRoute, launchSingleTop = true)
        return
    }

    intent.getStringExtra("startSearchFromTag")?.let { tag ->
        intent.removeExtra("startSearchFromTag")
        add(SearchRoute(query = tag))
        return
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    val map = intent.getSerializableExtra("startSearchFromMap") as? HashMap<String, String>
    if (map != null) {
        intent.removeExtra("startSearchFromMap")
        add(SearchRoute(advancedSearchJson = Json.encodeToString(map)))
        return
    }

    val videoCode = intent.getStringExtra("startVideoCode")
    if (!videoCode.isNullOrEmpty()) {
        intent.removeExtra("startVideoCode")
        add(VideoRoute(videoCode))
    }
}
