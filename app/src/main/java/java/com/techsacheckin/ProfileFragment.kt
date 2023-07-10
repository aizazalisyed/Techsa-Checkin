package java.com.techsacheckin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    lateinit var imageView : ImageView
    lateinit var personTextView : TextView
    lateinit var fatherTextView : TextView
    lateinit var addressTextView : TextView
    lateinit var phoneTextView : TextView
    lateinit var dateOfBirthTextView : TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String
    private lateinit var progressBar : ProgressBar
    private lateinit var logOutIcon : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.imageView)
        personTextView = view.findViewById(R.id.personTextView)
        fatherTextView = view.findViewById(R.id.fatherTextView)
        addressTextView = view.findViewById(R.id.addressTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        dateOfBirthTextView = view.findViewById(R.id.dateOfBirthTextView)
        progressBar = view.findViewById(R.id.progressBar)
        logOutIcon = view.findViewById(R.id.logOutIcon)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        fetchUserProfile()

        logOutIcon.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun fetchUserProfile() {
        progressBar.visibility = View.VISIBLE

        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    user?.let {
                        // Set the user data in the views
                        personTextView.text = it.userName
                        fatherTextView.text = it.fatherName
                        addressTextView.text = it.address
                        phoneTextView.text = it.phoneNumber
                        dateOfBirthTextView.text = it.dateOfBirth

                        // Load the user image using Glide
                        Glide.with(requireContext())
                            .load(it.photoUrl)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.default_user) // Add a placeholder image
                                    .error(R.drawable.default_user) // Add an error image
                                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image
                            )
                            .into(imageView)
                    }
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                // Handle the failure case
                progressBar.visibility = View.GONE
            }
    }
}