package com.suhel.mycoolllama.features

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Router @Inject constructor() {
    enum class Route {
        Models,
        Completion
    }

    private val _currentRoute = MutableStateFlow<Route>(Route.Models)
    val currentRoute = _currentRoute.asStateFlow()

    fun navigateTo(route: Route) {
        _currentRoute.value = route
    }
}
