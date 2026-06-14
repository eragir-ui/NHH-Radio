package com.example.data

data class RadioStation(
    val id: Int,
    val name: String,
    val streamUrl: String,
    val genre: String,
    val initials: String, // Two letters to display beautifully
    val isHls: Boolean = false,
    val gradientStart: Long,
    val gradientEnd: Long,
    val logoUrl: String? = null
)

object RadioStationRepository {
    val genres = listOf("Tümü")

    val stations = emptyList<RadioStation>()
}
