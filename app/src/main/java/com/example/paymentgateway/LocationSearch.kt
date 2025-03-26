package com.example.paymentgateway

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.paymentgateway.databinding.ActivityLocationSearchBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient

class LocationSearch : AppCompatActivity() {

    private lateinit var binding: ActivityLocationSearchBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var placesAdapter: ArrayAdapter<String>
    private val placesList = mutableListOf<AutocompletePrediction>()
    private var suggestionsDisplayList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLocationSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initializeWithNewPlacesApiEnabled(applicationContext, getString(R.string.google_maps_key))

        placesClient = Places.createClient(this)
        setUpData()
        setupAutoCompleteTextChangedListener()
    }

    private fun setUpData(){

        // Set up the adapter for autocomplete
        placesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestionsDisplayList)
        Log.d("REQ1", "Place Adapter: ${placesAdapter}")

        binding.autocompleteSearch.threshold = 1
        binding.autocompleteSearch.setAdapter(placesAdapter)

        binding.autocompleteSearch.dropDownWidth = LinearLayout.LayoutParams.MATCH_PARENT
        Log.d("REQ1", "Sugeestion size Adapter: ${suggestionsDisplayList.size}")


        binding.autocompleteSearch.setOnItemClickListener { _, _, position, _ ->

            if (position < placesList.size) {
                val selectedPlace = placesList[position]
                fetchPlaceDetails(selectedPlace.placeId)
            }
        }

    }

    private fun setupAutoCompleteTextChangedListener() {
        binding.autocompleteSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("REQ1location", "onTextChanged: $s")
                if ((s?.length ?: 0) >= 3) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        performSearch(s.toString()) }, 1500)
                } else {
                    suggestionsDisplayList.clear()
                    placesList.clear()
                    placesAdapter.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun clearSuggestions() {
        suggestionsDisplayList.clear()
        placesList.clear()
        placesAdapter.notifyDataSetChanged()
        binding.autocompleteSearch.dismissDropDown()
    }

    private fun performSearch(query: String) {

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(AutocompleteSessionToken.newInstance())
            .setTypeFilter(TypeFilter.ADDRESS)
            .build()


        Log.d("REQ1",request.toString())



        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                Log.d("REQ1location", "sucess___" + response.toString())

                clearSuggestions()


                for (prediction in response.autocompletePredictions){
                    placesList.add(prediction)
                    val fullText = prediction.getPrimaryText(null).toString()
                    suggestionsDisplayList.add(fullText)
                    placesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestionsDisplayList)
                    binding.autocompleteSearch.setAdapter(placesAdapter)
                    placesAdapter.notifyDataSetChanged()
                    Log.d("REQ1", "Adapter: ${suggestionsDisplayList}")
                    Log.d("REQ1", "Response: ${prediction.getFullText(null)}")
                }
                runOnUiThread {
                       placesAdapter.notifyDataSetChanged()

                    if (suggestionsDisplayList.isNotEmpty()) {
                        binding.autocompleteSearch.showDropDown()
                        Log.d("REQ1", "Contains Data")
                    } else {
                        Log.d("REQ1", "No Data")
                    }
                }
            }

            .addOnFailureListener { exception: Exception ->
                Log.e("REQ1", "Response failed: ${exception.message}")
                placesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestionsDisplayList)
                binding.autocompleteSearch.setAdapter(placesAdapter)
                Log.d("checkListisadd",suggestionsDisplayList.size.toString()+"\n"+suggestionsDisplayList.toString())
                Toast.makeText(this,"Response failed ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun fetchPlaceDetails(placeId: String) {

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.LAT_LNG
        )
        val request = com.google.android.libraries.places.api.net.FetchPlaceRequest
            .builder(placeId, placeFields)
            .setSessionToken(AutocompleteSessionToken.newInstance())
            .build()


        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                displayDetailsFields(place)

                binding.autocompleteSearch.dismissDropDown()
                binding.autocompleteSearch.visibility = View.GONE
                binding.detailsContainer.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Log.e("PlaceSearch", "Place details fetch failed: ${exception.message}")
            }
    }

    private fun displayDetailsFields(place: Place) {

        binding.cityName.setText(place.name ?: "")

        var street = ""
        var city = ""
        var state = ""
        var postalCode = ""
        var country = ""

        place.addressComponents?.asList()?.forEach { component ->
            when {
                component.types.contains("street_number") -> {
                    street = component.name + " " + street
                }
                component.types.contains("route") -> {
                    street += component.name
                }
                component.types.contains("locality") -> {
                    city = component.name
                }
                component.types.contains("administrative_area_level_1") -> {
                    state = component.name
                }
                component.types.contains("postal_code") -> {
                    postalCode = component.name
                }
                component.types.contains("country") -> {
                    country = component.name
                }
            }
        }

        if (street.isEmpty() && place.address != null) {
            val addressParts = place.address!!.split(",")
            if (addressParts.isNotEmpty()) {
                street = addressParts[0].trim()
            }
            if (addressParts.size > 1 && city.isEmpty()) {
                city = addressParts[1].trim()
            }
        }


        binding.streetName.setText(street)
        binding.cityName.setText(city)
        binding.stateName.setText(state)
        binding.zipCode.setText(postalCode)
        binding.countryName.setText(country)
    }
}