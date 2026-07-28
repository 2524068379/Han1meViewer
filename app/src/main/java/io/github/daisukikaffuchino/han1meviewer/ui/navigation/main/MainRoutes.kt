package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.navigation3.runtime.NavKey
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.DownloadSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.AboutSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.AppearanceSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.DataPrivacySettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HKeyframeSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HKeyframesRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HomeSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.MpvPlayerSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.NetworkSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.NetworkDownloadSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.OpenSourceLicensesRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.PlayerSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.InterfaceInteractionSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.SharedHKeyframesRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.VideoPlaybackSettingsRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
object HomeRoute : NavKey

@Serializable
object WatchHistoryRoute : NavKey

@Serializable
object MyFavVideoRoute : NavKey

@Serializable
object MyWatchLaterRoute : NavKey

@Serializable
object MyPlaylistRoute : NavKey

@Serializable
object SubscriptionRoute : NavKey

@Serializable
object DailyCheckInRoute : NavKey

@Serializable
object DownloadRoute : NavKey

@Serializable
object AccountRoute : NavKey

@Serializable
data class AvatarCropRoute(
    val sourceUri: String,
) : NavKey

@Serializable
data class SearchRoute(
    val query: String? = null,
    val advancedSearchJson: String? = null,
) : NavKey

@Serializable
object PreviewRoute : NavKey

@Serializable
object GetchuPreviewRoute : NavKey

@Serializable
data class GetchuPreviewDetailRoute(
    val id: String,
) : NavKey

@Serializable
data class PreviewCommentRoute(
    val date: String,
    val dateCode: String,
) : NavKey

@Serializable
data class VideoRoute(
    val videoCode: String,
    val localUri: String? = null,
) : NavKey

enum class MainDestinationSpec(
    val drawerDestination: MainDrawerDestination?,
    val routeClass: KClass<*>,
    val drawerEnabled: Boolean,
) {
    Home(
        drawerDestination = MainDrawerDestination.Home,
        routeClass = HomeRoute::class,
        drawerEnabled = true,
    ),
    WatchHistory(
        drawerDestination = MainDrawerDestination.WatchHistory,
        routeClass = WatchHistoryRoute::class,
        drawerEnabled = false,
    ),
    MyFavVideo(
        drawerDestination = MainDrawerDestination.FavVideo,
        routeClass = MyFavVideoRoute::class,
        drawerEnabled = false,
    ),
    MyWatchLater(
        drawerDestination = MainDrawerDestination.WatchLater,
        routeClass = MyWatchLaterRoute::class,
        drawerEnabled = false,
    ),
    MyPlaylist(
        drawerDestination = MainDrawerDestination.Playlist,
        routeClass = MyPlaylistRoute::class,
        drawerEnabled = false,
    ),
    Subscription(
        drawerDestination = MainDrawerDestination.Subscription,
        routeClass = SubscriptionRoute::class,
        drawerEnabled = false,
    ),
    DailyCheckIn(
        drawerDestination = MainDrawerDestination.DailyCheckIn,
        routeClass = DailyCheckInRoute::class,
        drawerEnabled = false,
    ),
    Download(
        drawerDestination = MainDrawerDestination.Download,
        routeClass = DownloadRoute::class,
        drawerEnabled = false,
    ),
    Account(
        drawerDestination = null,
        routeClass = AccountRoute::class,
        drawerEnabled = false,
    ),
    AvatarCrop(
        drawerDestination = null,
        routeClass = AvatarCropRoute::class,
        drawerEnabled = false,
    ),
    SettingsHome(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = HomeSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsVideoPlayback(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = VideoPlaybackSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsNetworkDownload(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = NetworkDownloadSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsAppearance(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = AppearanceSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsInterfaceInteraction(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = InterfaceInteractionSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsDataPrivacy(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = DataPrivacySettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsAbout(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = AboutSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsOpenSourceLicenses(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = OpenSourceLicensesRoute::class,
        drawerEnabled = false,
    ),
    SettingsPlayer(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = PlayerSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsNetwork(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = NetworkSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsDownload(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = DownloadSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsMpv(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = MpvPlayerSettingsRoute::class,
        drawerEnabled = false,
    ),
    SettingsHKeyframes(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = HKeyframesRoute::class,
        drawerEnabled = false,
    ),
    SettingsSharedHKeyframes(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = SharedHKeyframesRoute::class,
        drawerEnabled = false,
    ),
    SettingsHKeyframeSettings(
        drawerDestination = MainDrawerDestination.Settings,
        routeClass = HKeyframeSettingsRoute::class,
        drawerEnabled = false,
    ),
    Search(
        drawerDestination = null,
        routeClass = SearchRoute::class,
        drawerEnabled = false,
    ),
    Preview(
        drawerDestination = null,
        routeClass = PreviewRoute::class,
        drawerEnabled = false,
    ),
    GetchuPreview(
        drawerDestination = null,
        routeClass = GetchuPreviewRoute::class,
        drawerEnabled = false,
    ),
    GetchuPreviewDetail(
        drawerDestination = null,
        routeClass = GetchuPreviewDetailRoute::class,
        drawerEnabled = false,
    ),
    PreviewComment(
        drawerDestination = null,
        routeClass = PreviewCommentRoute::class,
        drawerEnabled = false,
    ),
    Video(
        drawerDestination = null,
        routeClass = VideoRoute::class,
        drawerEnabled = false,
    );

    companion object {
        fun fromRoute(route: NavKey?): MainDestinationSpec? =
            entries.firstOrNull { spec -> route != null && spec.routeClass.isInstance(route) }
    }
}
