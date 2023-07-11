package java.com.techsacheckin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CheckinFragment : Fragment() {
    private lateinit var placeList: MutableList<PlaceData>
    private lateinit var placeDataAdapter: PlaceDataAdapter
    private lateinit var firestoreListener: ListenerRegistration
    val TAG = "CheckinFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_checkin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize the placeList and placeDataAdapter
        placeList = mutableListOf()
        placeDataAdapter = PlaceDataAdapter(placeList)

        // Set up the RecyclerView and connect it to the adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = placeDataAdapter

        // Set up the Firestore listener for real-time updates
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        if (uid != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            val placesRef = userRef.collection("places")

            // Attach the listener
            firestoreListener = placesRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening to Firestore", exception)
                    return@addSnapshotListener
                }

                // Clear the previous list
                placeList.clear()

                // Iterate through the snapshot and add the documents to the list
                for (document in snapshot!!) {
                    val placeData = document.toObject(PlaceData::class.java)
                    placeList.add(placeData)
                }

                // Notify the adapter about the data change
                placeDataAdapter.notifyDataSetChanged()
            }
        } else {
            Log.e(TAG, "User not found")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()

        // Detach the Firestore listener when the fragment is destroyed
        firestoreListener.remove()
    }

}