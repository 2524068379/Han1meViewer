package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.HanimeScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
object HomeSettingsRoute : HanimeScreen

@Serializable
object VideoPlaybackSettingsRoute : HanimeScreen

@Serializable
object NetworkDownloadSettingsRoute : HanimeScreen

@Serializable
object AppearanceSettingsRoute : HanimeScreen

@Serializable
object InterfaceInteractionSettingsRoute : HanimeScreen

@Serializable
object DataPrivacySettingsRoute : HanimeScreen

@Serializable
object AboutSettingsRoute : HanimeScreen

@Serializable
object OpenSourceLicensesRoute : HanimeScreen

@Serializable
object PlayerSettingsRoute : HanimeScreen

@Serializable
object NetworkSettingsRoute : HanimeScreen

@Serializable
object DownloadSettingsRoute : HanimeScreen

@Serializable
object MpvPlayerSettingsRoute : HanimeScreen

@Serializable
object HKeyframesRoute : HanimeScreen

@Serializable
object SharedHKeyframesRoute : HanimeScreen

@Serializable
object HKeyframeSettingsRoute : HanimeScreen

enum class SettingsDestinationSpec(
    val routeKey: String,
    val titleRes: Int,
    val screenClassName: String,
    val routeClass: KClass<*>,
    val showToolbar: Boolean = true,
) {
    Home(
        routeKey = "home",
        titleRes = R.string.settings,
        screenClassName = "HomeSettingsScreen",
        routeClass = HomeSettingsRoute::class,
    ),
    VideoPlayback(
        routeKey = "video_playback",
        titleRes = R.string.settings_video_playback,
        screenClassName = "HomeSettingsScreen.VideoPlayback",
        routeClass = VideoPlaybackSettingsRoute::class,
    ),
    NetworkDownload(
        routeKey = "network_download",
        titleRes = R.string.settings_network_download,
        screenClassName = "HomeSettingsScreen.NetworkDownload",
        routeClass = NetworkDownloadSettingsRoute::class,
    ),
    Appearance(
        routeKey = "appearance",
        titleRes = R.string.settings_appearance,
        screenClassName = "HomeSettingsScreen.Appearance",
        routeClass = AppearanceSettingsRoute::class,
    ),
    InterfaceInteraction(
        routeKey = "interface_interaction",
        titleRes = R.string.settings_interface_interaction,
        screenClassName = "HomeSettingsScreen.InterfaceInteraction",
        routeClass = InterfaceInteractionSettingsRoute::class,
    ),
    DataPrivacy(
        routeKey = "data_privacy",
        titleRes = R.string.settings_data_privacy,
        screenClassName = "HomeSettingsScreen.DataPrivacy",
        routeClass = DataPrivacySettingsRoute::class,
    ),
    About(
        routeKey = "about",
        titleRes = R.string.about,
        screenClassName = "HomeSettingsScreen.About",
        routeClass = AboutSettingsRoute::class,
    ),
    OpenSourceLicenses(
        routeKey = "open_source_licenses",
        titleRes = R.string.open_source_license,
        screenClassName = "OpenSourceLicensesScreen",
        routeClass = OpenSourceLicensesRoute::class,
    ),
    Player(
        routeKey = "player",
        titleRes = R.string.player_settings,
        screenClassName = "PlayerSettingsScreen",
        routeClass = PlayerSettingsRoute::class,
    ),
    Network(
        routeKey = "network",
        titleRes = R.string.network_settings,
        screenClassName = "NetworkSettingsScreen",
        routeClass = NetworkSettingsRoute::class,
    ),
    Download(
        routeKey = "download",
        titleRes = R.string.download_settings,
        screenClassName = "DownloadSettingsScreen",
        routeClass = DownloadSettingsRoute::class,
    ),
    Mpv(
        routeKey = "mpv",
        titleRes = R.string.mpv_advanced_settings,
        screenClassName = "MpvPlayerSettingsScreen",
        routeClass = MpvPlayerSettingsRoute::class,
    ),
    HKeyframes(
        routeKey = "h_keyframes",
        titleRes = R.string.h_keyframe_manage,
        screenClassName = "HKeyframesScreen",
        routeClass = HKeyframesRoute::class,
    ),
    SharedHKeyframes(
        routeKey = "shared_h_keyframes",
        titleRes = R.string.shared_h_keyframe_manage,
        screenClassName = "SharedHKeyframesScreen",
        routeClass = SharedHKeyframesRoute::class,
    ),
    HKeyframeSettings(
        routeKey = "h_keyframe_settings",
        titleRes = R.string.h_keyframe_settings,
        screenClassName = "HKeyframeSettingsScreen",
        routeClass = HKeyframeSettingsRoute::class,
    );

    val route: HanimeScreen
        get() = when (this) {
            Home -> HomeSettingsRoute
            VideoPlayback -> VideoPlaybackSettingsRoute
            NetworkDownload -> NetworkDownloadSettingsRoute
            Appearance -> AppearanceSettingsRoute
            InterfaceInteraction -> InterfaceInteractionSettingsRoute
            DataPrivacy -> DataPrivacySettingsRoute
            About -> AboutSettingsRoute
            OpenSourceLicenses -> OpenSourceLicensesRoute
            Player -> PlayerSettingsRoute
            Network -> NetworkSettingsRoute
            Download -> DownloadSettingsRoute
            Mpv -> MpvPlayerSettingsRoute
            HKeyframes -> HKeyframesRoute
            SharedHKeyframes -> SharedHKeyframesRoute
            HKeyframeSettings -> HKeyframeSettingsRoute
        }

    companion object {
        fun fromRouteKey(routeKey: String?): SettingsDestinationSpec? =
            entries.firstOrNull { it.routeKey == routeKey }

        fun fromRoute(route: HanimeScreen?): SettingsDestinationSpec? =
            entries.firstOrNull { spec -> route != null && spec.routeClass.isInstance(route) }
    }
}
