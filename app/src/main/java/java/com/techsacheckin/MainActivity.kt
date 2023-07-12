package java.com.techsacheckin

import MapFragment
import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1122

class MainActivity : AppCompatActivity() {

    //widgets declaration
    private lateinit var bottomNavigationView : BottomNavigationView

    //fragments init
    private val mapFragment = MapFragment()
    private val profileFragment = ProfileFragment()
    private val checkInFragment = CheckinFragment()
    private var active : Fragment = mapFragment

    //FragmentManager init
   private val fm = getSupportFragmentManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestLocationPermission()

        //widget init
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        //setting default item for bottom nav
        bottomNavigationView.selectedItemId = R.id.map_page

        //hide other fragments
        hideFragments()


        //bottom nav listener
        bottomNavigationView.setOnNavigationItemSelectedListener {item ->
            when(item.itemId){

                R.id.map_page ->{
                    fm.beginTransaction().hide(active).show(mapFragment).commit();
                    active = mapFragment
                }
                R.id.profile ->{
                    fm.beginTransaction().hide(active).show(profileFragment).commit();
                    active = profileFragment
                }
                R.id.checkinList ->{
                    fm.beginTransaction().hide(active).show(checkInFragment).commit();
                    active = checkInFragment
                }
            }
          true
        }
    }


    //hide other fragments
    private fun hideFragments(){
        fm.beginTransaction().add(R.id.main_container,profileFragment,"profileFragment")
            .hide(profileFragment)
            .commit()
        fm.beginTransaction().add(R.id.main_container,checkInFragment,"checkInFragment")
            .hide(checkInFragment)
            .commit()
        fm.beginTransaction().add(R.id.main_container,mapFragment,"mapFragment").commit()
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, handle it in the MapFragment
                val fragment = supportFragmentManager.findFragmentById(R.id.main_container)
                if (fragment is MapFragment) {
                    fragment.handleLocationPermissionGranted()
                }
            } else {
                // Permission denied
                // Handle permission denial case
            }
        }
    }

    }