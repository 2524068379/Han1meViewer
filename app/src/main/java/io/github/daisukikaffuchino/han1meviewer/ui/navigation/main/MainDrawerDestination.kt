package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.daisukikaffuchino.han1meviewer.R

enum class MainDrawerDestination(
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val titleRes: Int,
    val requiresLogin: Boolean = false,
) {
    Home(
        iconRes = R.drawable.ic_home,
        titleRes = R.string.home_page,
    ),
    Settings(
        iconRes = R.drawable.ic_settings,
        titleRes = R.string.settings,
    ),
    DailyCheckIn(
        iconRes = R.drawable.ic_thumb_up_off_alt,
        titleRes = R.string.check_in_feature_name,
    ),
    WatchLater(
        iconRes = R.drawable.ic_access_time,
        titleRes = R.string.watch_later,
        requiresLogin = true,
    ),
    FavVideo(
        iconRes = R.drawable.ic_favorite_border,
        titleRes = R.string.fav_video,
        requiresLogin = true,
    ),
    Playlist(
        iconRes = R.drawable.ic_format_list_bulleted,
        titleRes = R.string.play_list,
        requiresLogin = true,
    ),
    Subscription(
        iconRes = R.drawable.ic_subscribtion,
        titleRes = R.string.my_subscribe,
        requiresLogin = true,
    ),
    WatchHistory(
        iconRes = R.drawable.ic_history,
        titleRes = R.string.watch_history,
    ),
    Download(
        iconRes = R.drawable.ic_download,
        titleRes = R.string.download,
    ),
}
