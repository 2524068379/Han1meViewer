package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeTopAppBar
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeScaffold
import io.github.daisukikaffuchino.han1meviewer.ui.navigation.main.MainNavigationState
import io.github.daisukikaffuchino.han1meviewer.ui.theme.HanimeDefaults

@Composable
fun SettingsScaffold(
    navigationState: MainNavigationState,
    fallbackDestination: NavKey,
    onNavigateBack: (() -> Boolean)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val currentDestination = SettingsDestinationSpec.fromRoute(navigationState.currentRoute)
        ?: SettingsDestinationSpec.Home

    fun navigateBack() {
        if (onNavigateBack?.invoke() == true) return
        if (!navigationState.popBackStack()) {
            navigationState.navigate(fallbackDestination, launchSingleTop = true)
        }
    }

    HanimeScaffold(
        topBar = {
            if (currentDestination.showToolbar) {
                HanimeTopAppBar(
                    title = stringResource(currentDestination.titleRes),
                    onBack = ::navigateBack,
                    actions = actions,
                )
            }
        },
        contentHorizontalPadding = 0.dp,
        floatingActionButton = floatingActionButton,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HanimeDefaults.Spacing.contentHorizontal),
        ) {
            content()
        }
    }
}
