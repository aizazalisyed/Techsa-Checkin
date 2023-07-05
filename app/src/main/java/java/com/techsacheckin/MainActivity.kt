package java.com.techsacheckin

import MapFragment
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
                    Toast.makeText(this, "listen", Toast.LENGTH_SHORT).show()
                }
                R.id.profile ->{
                    fm.beginTransaction().hide(active).show(profileFragment).commit();
                    active = profileFragment
                    Toast.makeText(this, "listen", Toast.LENGTH_SHORT).show()
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

    }