package io.github.daisukikaffuchino.han1meviewer.ui.navigation.main

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Stable
class MainNavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    val currentRoute: NavKey
        get() = backStack.last()

    fun navigate(route: NavKey, launchSingleTop: Boolean = false) {
        if (launchSingleTop && backStack.lastOrNull() == route) return

        val existingIndex = backStack.indexOfLast { it == route }
        if (existingIndex >= 0) {
            while (backStack.lastIndex > existingIndex) {
                backStack.removeAt(backStack.lastIndex)
            }
            return
        }
        backStack.add(route)
    }

    fun popBackStack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun popBackStack(route: NavKey, inclusive: Boolean): Boolean {
        val routeIndex = backStack.indexOfLast { it == route }
        if (routeIndex < 0) return false

        val targetSize = routeIndex + if (inclusive) 0 else 1
        if (targetSize == backStack.size) return false
        while (backStack.size > targetSize.coerceAtLeast(1)) {
            backStack.removeAt(backStack.lastIndex)
        }
        return true
    }
}
