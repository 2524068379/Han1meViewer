package io.github.daisukikaffuchino.han1meviewer.ui.navigation.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeTopAppBar
import io.github.daisukikaffuchino.han1meviewer.ui.component.appbar.HanimeScaffold
import io.github.daisukikaffuchino.han1meviewer.ui.theme.HanimeDefaults

@Composable
fun SettingsScaffold(
    navController: NavController,
    fallbackDestination: Any,
    onNavigateBack: (() -> Boolean)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = SettingsDestinationSpec.fromDestination(backStackEntry?.destination)
        ?: SettingsDestinationSpec.Home

    fun navigateBack() {
        if (onNavigateBack?.invoke() == true) return
        if (!navController.popBackStack()) {
            navController.navigate(fallbackDestination)
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
