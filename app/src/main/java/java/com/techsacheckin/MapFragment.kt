import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.InputType
import retrofit2.Call
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import retrofit2.Response
import java.com.techsacheckin.R
import java.com.techsacheckin.RetrofitClient
import java.com.techsacheckin.WeatherResponse
import java.io.IOException
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1122
    private val DEFAULT_ZOOM = 10
    private val defaultLocation = LatLng(0.0, 0.0)
    private var locationPermissionGranted = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var map: GoogleMap? = null
    private var lastKnownLocation: Location? = null
    private val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    private lateinit var locationManager: LocationManager

    //Place info
    private var temp_c: Double = 0.0
    private var temp_f : Double = 0.0
    private  var condition: String = "null"
    val API_KEY = "87af37dd59a246489ac42408222612"


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Called when the location has changed
            lastKnownLocation = location
            // Update the map camera to the new location
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), DEFAULT_ZOOM.toFloat()
                )
            )
            map?.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude,location.longitude))
                    .title(getPlaceNameFromLocation(location)))
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Called when the provider status changes
        }

        override fun onProviderEnabled(provider: String) {
            // Called when the provider is enabled by the user
        }

        override fun onProviderDisabled(provider: String) {
            // Called when the provider is disabled by the user
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.frg) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Initialize the SDK
        Places.initialize(requireContext(), getString(R.string.MAPS_API_KEY))

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(requireContext())

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment)  as AutocompleteSupportFragment
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")

                // Retrieve the location from the selected place
                val location = place.latLng
                // Update the map camera to the new location


                if (location != null) {
                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), DEFAULT_ZOOM.toFloat()
                        )
                    )

                    // Add a marker to the selected place
                    map?.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(place.name))


                }
                // Show the check-in dialog with the updated place name
                place.name?.let { showCheckInDialog(it) }

            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationPermission()
        updateLocationUI()

        // Set a marker click listener
        map?.setOnMarkerClickListener { marker ->
            showCheckInDialog(getPlaceNameFromLocation(lastKnownLocation!!))
            true
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Location permission is granted
            Log.d(TAG, "locationPermissionGranted : inside getLocationPermission()")
            locationPermissionGranted = true
            getDeviceLocation()
        } else {
            // Location permission is not granted, request it
            Log.d(TAG, "locationPermissionGranted = false: inside getLocationPermission()")
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        // Request the ACCESS_FINE_LOCATION permission
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
        Log.d(TAG, "inside requestLocationPermission()")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        Toast.makeText(requireContext(), "onRequestPermissionsResult is called", Toast.LENGTH_SHORT).show()

        // Handle the result of the permission request
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission Granted : inside onRequestPermissionsResult")

                locationPermissionGranted = true
                getDeviceLocation()
                startLocationUpdates()
            } else {
                Log.d(TAG, "Permission Denied : inside onRequestPermissionsResult")
                locationPermissionGranted = false
                // Handle permission denial case
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {

                            val placeName = getPlaceNameFromLocation(lastKnownLocation!!)

                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                            map?.addMarker(
                                MarkerOptions()
                                    .position(LatLng(lastKnownLocation!!.latitude,lastKnownLocation!!.longitude))
                                    .title(getPlaceNameFromLocation(lastKnownLocation!!)))

                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: ${task.exception}")
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            } else {
                Log.d(TAG, " locationPermissionGranted = false : inside getDeviceLocation()")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Exception: ${e.message}", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                Log.d(TAG, "Updating UI")
                getDeviceLocation()
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Exception: ${e.message}", e)
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // Check if location updates are enabled
        if (locationPermissionGranted) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
            )
        }
    }


    companion object {
        private const val TAG = "MapFragment"
    }

    fun getWeatherInfo(placeName : String){

        RetrofitClient().getUserService()
            ?.getCurrentWeatherAsync(apiKey = API_KEY, location = placeName, aqi= "no")?.enqueue(
                object : retrofit2.Callback<WeatherResponse> {
                    override fun onResponse(
                        call: Call<WeatherResponse>,
                        response: Response<WeatherResponse>
                    ) {
                        if(response.isSuccessful){
                            temp_c = response.body()!!.current.temp_c
                            temp_f = response.body()!!.current.temp_f
                            condition = response.body()!!.current.condition.text

                            Toast.makeText(requireContext(), "temp = $temp_f F, $temp_c C : condition = $condition", Toast.LENGTH_SHORT).show()
                        }


                    }

                    override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                        TODO("Not yet implemented")
                    }


                })

        }
    private fun getPlaceNameFromLocation(location: Location): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    return addresses[0].getAddressLine(0) ?: ""
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
    private fun showCheckInDialog(placeName : String) {

        getWeatherInfo(placeName)

// Create a new AlertDialog Builder
        val builder = AlertDialog.Builder(requireContext())

        // Set the dialog view
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.check_in_dialogue, null)
        builder.setView(dialogView)

        // Initialize the dialog views
        val temperatureTextView = dialogView.findViewById<TextView>(R.id.temprature)
        val conditionTextView = dialogView.findViewById<TextView>(R.id.condition)
        val checkInTimeTextView = dialogView.findViewById<TextView>(R.id.checkin_time)
        val checkInEditText = dialogView.findViewById<EditText>(R.id.checkInEditText)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        // Set the temperature and condition values from the API response
        temperatureTextView.text = "Temperature: ${temp_c}°C"
        conditionTextView.text = "Condition: $condition"

        // Set the current check-in time
        val currentTime = Calendar.getInstance().time.toString()
        checkInTimeTextView.text = "Check-in Time: $currentTime"

        val dialog = builder.create()

        // Set a click listener for the cancel button
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        // Set a click listener for the save button
        saveButton.setOnClickListener {
            val checkInPurpose = checkInEditText.text.toString().trim()
            if (checkInPurpose.isNotEmpty()) {
                // Save the check-in purpose
                Toast.makeText(requireContext(), "Check-in Purpose: $checkInPurpose", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a check-in purpose", Toast.LENGTH_SHORT).show()
            }
        }

        // Create and show the dialog
        dialog.show()
    }

    }
