package com.example.weatherapp.model

data class WeatherResponse(
    val name: String?,
    val weather: List<WeatherCondition>?
)

data class WeatherCondition(
    val main: String?,
    val description: String?,
    val icon: String?
)
