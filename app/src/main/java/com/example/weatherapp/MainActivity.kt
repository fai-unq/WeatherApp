package com.example.weatherapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.view.inputmethod.InputMethodManager
import android.content.Context

import com.bumptech.glide.Glide
import com.example.weatherapp.api.RetrofitClient
import com.example.weatherapp.model.WeatherResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : Activity() {

    // Interface components
    private lateinit var citySearch: EditText
    private lateinit var searchButton: Button

    private lateinit var cityText: TextView
    private lateinit var weatherMainText: TextView
    private lateinit var weatherDescriptionText: TextView

    private lateinit var weatherIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    // Replace with your actual OpenWeatherMap API key.
    private val apiKey = "66f270353c93ddfab4b594599e0cf983"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Connect XML components to Kotlin.
        citySearch = findViewById(R.id.citySearch)
        searchButton = findViewById(R.id.searchButton)

        cityText = findViewById(R.id.cityText)
        weatherMainText = findViewById(R.id.weatherMainText)
        weatherDescriptionText =
            findViewById(R.id.weatherDescriptionText)

        weatherIcon = findViewById(R.id.weatherIcon)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)

        // Set the initial city.
        citySearch.setText("New York")

        // Search button click event.
        searchButton.setOnClickListener {

            val enteredCity = citySearch.text.toString().trim()

            if (enteredCity.isEmpty()) {

                showError("Please enter a city name.")

            } else {

                hideKeyboard()

                getWeather(enteredCity)
            }
        }

        // Allow searching using the keyboard Search button.
        citySearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                searchButton.performClick()

                true

            } else {

                false
            }
        }

        // Load the default city's weather.
        getWeather("New York")
    }

    // Retrieve weather information for the entered city.
    private fun getWeather(cityName: String) {

        if (apiKey == "YOUR_API_KEY" || apiKey.isBlank()) {

            showError("Please enter your OpenWeatherMap API key.")

            return
        }

        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE

        searchButton.isEnabled = false

        val call = RetrofitClient.weatherApi
            .getCurrentWeather(cityName, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {

            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {

                progressBar.visibility = View.GONE
                searchButton.isEnabled = true

                if (response.isSuccessful) {

                    val weatherResponse = response.body()

                    if (weatherResponse != null) {

                        displayWeather(weatherResponse)

                    } else {

                        showError("Weather information is empty.")
                    }

                } else {

                    when (response.code()) {

                        401 -> showError(
                            "Invalid or inactive API key."
                        )

                        404 -> showError(
                            "City not found. Please check the spelling."
                        )

                        429 -> showError(
                            "API request limit reached. Try again later."
                        )

                        else -> showError(
                            "Server error: ${response.code()}"
                        )
                    }

                    Log.e(
                        "WeatherApp",
                        "API error: ${response.code()}"
                    )
                }
            }

            override fun onFailure(
                call: Call<WeatherResponse>,
                t: Throwable
            ) {

                progressBar.visibility = View.GONE
                searchButton.isEnabled = true

                showError(
                    "Unable to connect. Check your internet connection."
                )

                Log.e(
                    "WeatherApp",
                    "Request failed",
                    t
                )
            }
        })
    }

    // Display the retrieved weather information.
    private fun displayWeather(
        weatherResponse: WeatherResponse
    ) {

        val condition = weatherResponse.weather?.firstOrNull()

        if (condition == null) {

            showError("No weather information available.")

            return
        }

        // Display city name.
        cityText.text = weatherResponse.name ?: "Unknown City"

        // Display main weather condition.
        weatherMainText.text =
            condition.main ?: "Unavailable"

        // Display detailed weather description.
        weatherDescriptionText.text =
            condition.description ?: "Unavailable"

        // Get weather icon code.
        val iconCode = condition.icon

        if (!iconCode.isNullOrBlank()) {

            val iconUrl =
                "https://openweathermap.org/img/wn/${iconCode}@2x.png"

            // Download weather icon using Glide.
            Glide.with(this)
                .load(iconUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(weatherIcon)

        } else {

            Glide.with(this).clear(weatherIcon)
            weatherIcon.setImageDrawable(null)
        }

        errorText.visibility = View.GONE
    }

    // Display an error message.
    private fun showError(message: String) {

        progressBar.visibility = View.GONE

        errorText.text = message
        errorText.visibility = View.VISIBLE
    }

    // Hide the keyboard after clicking Search.
    private fun hideKeyboard() {

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(
            citySearch.windowToken,
            0
        )
    }
}
