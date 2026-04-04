package com.idlestables.app.idlestables.ui

sealed class ScreenRoute(val route: String) {
    data object Dashboard : ScreenRoute("dashboard")
    data object Stable : ScreenRoute("stable")
    data object Tracks : ScreenRoute("tracks")
    data object Breed : ScreenRoute("breed")
    data object Silks : ScreenRoute("silks")

    data object TrackDetail : ScreenRoute("tracks/{id}") {
        fun create(id: String) = "tracks/$id"
    }

    data object Race : ScreenRoute("race/{id}") {
        fun create(id: String) = "race/$id"
    }
}
