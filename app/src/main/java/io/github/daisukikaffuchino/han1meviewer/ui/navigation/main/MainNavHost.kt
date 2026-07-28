package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.ui.activity.MainActivity
import io.github.daisukikaffuchino.han1meviewer.ui.component.IconButton
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.AboutSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.AppearanceSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.DataPrivacySettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.DownloadSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.DownloadSettingsRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HKeyframeSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HKeyframeSettingsRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HKeyframesRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HKeyframesRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HomeSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.HomeSettingsRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.InterfaceInteractionSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.MpvPlayerSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.MpvPlayerSettingsRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.NetworkDownloadSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.NetworkSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.NetworkSettingsRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.OpenSourceLicensesRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.PlayerSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.PlayerSettingsRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.SettingsScaffold
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.SharedHKeyframesRoute
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.SharedHKeyframesRouteScreen
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings.VideoPlaybackSettingsRoute
import io.github.daisukikaffuchino.han1meviewer.ui.screen.account.AccountScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.account.AvatarCropScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.HomeSettingsPage
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.OpenSourceLicensesScreen
import io.github.daisukikaffuchino.han1meviewer.ui.screen.settings.SettingsMainScreen
import io.github.daisukikaffuchino.han1meviewer.ui.theme.materialSharedAxisX
import io.github.daisukikaffuchino.han1meviewer.ui.viewmodel.UserAccountViewModel
import io.github.daisukikaffuchino.utils.VibrationUtil
import kotlinx.serialization.json.Json

private const val PageTransitionOffsetFactor = 0.10f

@Composable
fun MainNavHost(
    activity: MainActivity,
    navigationState: MainNavigationState,
    isDrawerOpen: Boolean,
    onOpenDrawer: () -> Unit,
    onDestinationChanged: (MainDestinationSpec) -> Unit,
) {
    val destinationSpec = MainDestinationSpec.fromRoute(navigationState.currentRoute)
    var pendingAvatarCropResult by remember { mutableStateOf<String?>(null) }

    val onBack: () -> Unit = { navigationState.popBackStack() }
    val onNavigateToVideo: (String) -> Unit = { code -> navigationState.navigate(VideoRoute(code)) }
    val onNavigateToLocalVideo: (String, String?) -> Unit =
        { code, uri -> navigationState.navigate(VideoRoute(code, uri)) }

    LaunchedEffect(destinationSpec) {
        destinationSpec?.let(onDestinationChanged)
    }

    NavDisplay(
        backStack = navigationState.backStack,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            materialSharedAxisX(
                initialOffsetX = { (it * PageTransitionOffsetFactor).toInt() },
                targetOffsetX = { -(it * PageTransitionOffsetFactor).toInt() },
            )
        },
        popTransitionSpec = {
            materialSharedAxisX(
                initialOffsetX = { -(it * PageTransitionOffsetFactor).toInt() },
                targetOffsetX = { (it * PageTransitionOffsetFactor).toInt() },
            )
        },
        predictivePopTransitionSpec = { _ ->
            materialSharedAxisX(
                initialOffsetX = { -(it * PageTransitionOffsetFactor).toInt() },
                targetOffsetX = { (it * PageTransitionOffsetFactor).toInt() },
            )
        },
        entryProvider = entryProvider {
        entry<HomeRoute> {
            HomeRouteScreen(
                activity = activity,
                isDrawerOpen = isDrawerOpen,
                onOpenDrawer = onOpenDrawer,
                onNavigateToPreview = { navigationState.navigate(PreviewRoute) },
                onNavigateToSearch = { query -> navigationState.navigate(SearchRoute(query = query)) },
                onNavigateToSearchAdvanced = { params ->
                    navigationState.navigate(
                        SearchRoute(advancedSearchJson = Json.encodeToString(params))
                    )
                },
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<WatchHistoryRoute> {
            WatchHistoryRouteScreen(
                onBack = onBack,
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<MyFavVideoRoute> {
            FavVideoRouteScreen(
                onBack = onBack,
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<MyWatchLaterRoute> {
            WatchLaterRouteScreen(
                onBack = onBack,
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<MyPlaylistRoute> {
            MyPlaylistRouteScreen(
                onBack = onBack,
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<SubscriptionRoute> {
            SubscriptionRouteScreen(
                onBack = onBack,
                onNavigateToSearch = { query -> navigationState.navigate(SearchRoute(query = query)) },
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<DailyCheckInRoute> {
            DailyCheckInRouteScreen(
                activity = activity,
                onBack = onBack,
            )
        }
        entry<DownloadRoute> {
            DownloadRouteScreen(
                onBack = onBack,
                onNavigateToVideo = onNavigateToVideo,
                onNavigateToLocalVideo = onNavigateToLocalVideo,
            )
        }
        entry<AccountRoute> {
            val accountViewModel: UserAccountViewModel = viewModel()
            AccountScreen(
                viewModel = accountViewModel,
                onBack = onBack,
                onOpenAvatarCrop = { sourceUri ->
                    navigationState.navigate(AvatarCropRoute(sourceUri))
                },
                pendingAvatarCropResult = pendingAvatarCropResult,
                onAvatarCropResultConsumed = { pendingAvatarCropResult = null },
                onRefreshHome = { activity.viewModel.getHomePage() },
                onLogout = { activity.showLogoutConfirmDialog(closeCurrentPageOnConfirm = true) },
            )
        }
        entry<AvatarCropRoute> { route ->
            AvatarCropScreen(
                sourceUri = route.sourceUri,
                onBack = onBack,
                onConfirm = { file ->
                    pendingAvatarCropResult = file.absolutePath
                    onBack()
                },
            )
        }
        entry<HomeSettingsRoute> {
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = HomeRoute,
            ) {
                SettingsMainScreen(
                    onOpenVideoPlayback = { navigationState.navigate(VideoPlaybackSettingsRoute) },
                    onOpenPlayerSettings = { navigationState.navigate(PlayerSettingsRoute) },
                    onOpenNetworkDownload = { navigationState.navigate(NetworkDownloadSettingsRoute) },
                    onOpenAppearance = { navigationState.navigate(AppearanceSettingsRoute) },
                    onOpenInterfaceInteraction = {
                        navigationState.navigate(InterfaceInteractionSettingsRoute)
                    },
                    onOpenDataPrivacy = { navigationState.navigate(DataPrivacySettingsRoute) },
                    onOpenAbout = { navigationState.navigate(AboutSettingsRoute) },
                )
            }
        }
        entry<VideoPlaybackSettingsRoute> {
            SettingsScaffold(navigationState, HomeSettingsRoute) {
                HomeSettingsRouteScreen(
                    activity = activity,
                    page = HomeSettingsPage.VideoPlayback,
                    onNavigateToHKeyframes = { navigationState.navigate(HKeyframesRoute) },
                    onNavigateToSharedHKeyframes = { navigationState.navigate(SharedHKeyframesRoute) },
                )
            }
        }
        entry<NetworkDownloadSettingsRoute> {
            SettingsScaffold(navigationState, HomeSettingsRoute) {
                HomeSettingsRouteScreen(
                    activity = activity,
                    page = HomeSettingsPage.NetworkDownload,
                )
            }
        }
        entry<AppearanceSettingsRoute> {
            SettingsScaffold(navigationState, HomeSettingsRoute) {
                HomeSettingsRouteScreen(
                    activity = activity,
                    page = HomeSettingsPage.Appearance,
                )
            }
        }
        entry<InterfaceInteractionSettingsRoute> {
            SettingsScaffold(navigationState, HomeSettingsRoute) {
                HomeSettingsRouteScreen(
                    activity = activity,
                    page = HomeSettingsPage.InterfaceInteraction,
                )
            }
        }
        entry<DataPrivacySettingsRoute> {
            SettingsScaffold(navigationState, HomeSettingsRoute) {
                HomeSettingsRouteScreen(
                    activity = activity,
                    page = HomeSettingsPage.DataPrivacy,
                )
            }
        }
        entry<AboutSettingsRoute> {
            SettingsScaffold(navigationState, HomeSettingsRoute) {
                HomeSettingsRouteScreen(
                    activity = activity,
                    page = HomeSettingsPage.About,
                    onNavigateToOpenSourceLicenses = {
                        navigationState.navigate(OpenSourceLicensesRoute)
                    },
                )
            }
        }
        entry<OpenSourceLicensesRoute> {
            var searchMode by remember { mutableStateOf(false) }
            BackHandler(enabled = searchMode) {
                searchMode = false
            }
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = AboutSettingsRoute,
                onNavigateBack = {
                    if (searchMode) {
                        searchMode = false
                        true
                    } else {
                        false
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = !searchMode,
                        enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
                            scaleIn(MaterialTheme.motionScheme.fastSpatialSpec()),
                        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                            scaleOut(MaterialTheme.motionScheme.fastSpatialSpec()),
                    ) {
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = { searchMode = true },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = stringResource(R.string.search),
                            )
                        }
                    }
                },
            ) {
                OpenSourceLicensesScreen(
                    searchMode = searchMode,
                )
            }
        }
        entry<PlayerSettingsRoute> {
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = HomeSettingsRoute,
            ) {
                PlayerSettingsRouteScreen(
                    onNavigateToMpvSettings = { navigationState.navigate(MpvPlayerSettingsRoute) },
                )
            }
        }
        entry<NetworkSettingsRoute> {
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = NetworkDownloadSettingsRoute,
            ) {
                NetworkSettingsRouteScreen()
            }
        }
        entry<DownloadSettingsRoute> {
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = NetworkDownloadSettingsRoute,
            ) {
                DownloadSettingsRouteScreen()
            }
        }
        entry<MpvPlayerSettingsRoute> {
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = PlayerSettingsRoute,
            ) {
                MpvPlayerSettingsRouteScreen()
            }
        }
        entry<HKeyframesRoute> {
            var showImportDialog by remember { mutableStateOf(false) }
            val view = LocalView.current
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = VideoPlaybackSettingsRoute,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            VibrationUtil.performHapticFeedback(view)
                            showImportDialog = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = stringResource(R.string.h_keyframes_import_shared),
                        )
                    }
                },
            ) {
                HKeyframesRouteScreen(
                    onOpenVideo = onNavigateToVideo,
                    showImportDialog = showImportDialog,
                    onImportDialogDismiss = { showImportDialog = false },
                )
            }
        }
        entry<SharedHKeyframesRoute> {
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = VideoPlaybackSettingsRoute,
            ) {
                SharedHKeyframesRouteScreen(
                    onOpenVideo = onNavigateToVideo,
                )
            }
        }
        entry<HKeyframeSettingsRoute> {
            SettingsScaffold(
                navigationState = navigationState,
                fallbackDestination = VideoPlaybackSettingsRoute,
            ) {
                HKeyframeSettingsRouteScreen(
                    onNavigateToHKeyframes = { navigationState.navigate(HKeyframesRoute) },
                    onNavigateToSharedHKeyframes = { navigationState.navigate(SharedHKeyframesRoute) },
                )
            }
        }
        entry<SearchRoute> { route ->
            SearchRouteScreen(
                route = route,
                onBack = onBack,
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<PreviewRoute> {
            PreviewRouteScreen(
                activity = activity,
                onBack = onBack,
                onNavigateToGetchuPreview = {
                    navigationState.navigate(GetchuPreviewRoute)
                },
                onNavigateToPreviewComment = { date, dateCode ->
                    navigationState.navigate(PreviewCommentRoute(date, dateCode))
                },
                onNavigateToVideo = onNavigateToVideo,
            )
        }
        entry<GetchuPreviewRoute> {
            GetchuPreviewRouteScreen(
                onBack = onBack,
                onNavigateToDetail = { id -> navigationState.navigate(GetchuPreviewDetailRoute(id)) },
            )
        }
        entry<GetchuPreviewDetailRoute> { route ->
            GetchuPreviewDetailRouteScreen(
                route = route,
                onBack = onBack,
                onNavigateToDetail = { id -> navigationState.navigate(GetchuPreviewDetailRoute(id)) },
                onNavigateToVideoUrl = { url -> navigationState.navigate(VideoRoute("-1", url)) },
            )
        }
        entry<PreviewCommentRoute> { route ->
            PreviewCommentRouteScreen(
                activity = activity,
                route = route,
                onBack = onBack,
            )
        }
        entry<VideoRoute> { route ->
            VideoRouteScreen(
                activity = activity,
                route = route,
            )
        }
        },
    )
}
