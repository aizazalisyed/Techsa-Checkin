package java.com.techsacheckin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*


lateinit var userImage: ImageView
lateinit var editTextName: EditText
lateinit var editTextFatherName: EditText
lateinit var editTextAddress: EditText
lateinit var editDateOfBirth: EditText
lateinit var editTextPhone: EditText
lateinit var saveButton: Button
lateinit var progressBar: ProgressBar
lateinit var addImageView: ImageView
private const val GALLERY_REQUEST_CODE = 100
private lateinit var storageReference: StorageReference
private lateinit var auth: FirebaseAuth
private lateinit var firestore: FirebaseFirestore
private lateinit var currentUserId: String


class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        userImage = findViewById(R.id.imageView)
        editTextName = findViewById(R.id.editTextName)
        editTextFatherName = findViewById(R.id.editTextFatherName)
        editTextAddress = findViewById(R.id.editTextAddress)
        editDateOfBirth = findViewById(R.id.editDateOfBirth)
        saveButton = findViewById(R.id.saveButton)
        progressBar = findViewById(R.id.progressBar)
        addImageView = findViewById(R.id.addImageView)
        editTextPhone = findViewById(R.id.editTextPhone)

        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""


        addImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)

        }


        saveButton.setOnClickListener {
            val userName = editTextName.text.toString().trim()
            val fatherName = editTextFatherName.text.toString().trim()
            val address = editTextAddress.text.toString().trim()
            val dateOfBirth = editDateOfBirth.text.toString().trim()
            val phoneNumer = editTextPhone.text.toString().trim()

            if (userName.isNotEmpty() && fatherName.isNotEmpty() && address.isNotEmpty() && dateOfBirth.isNotEmpty() && phoneNumer.isNotEmpty()) {
                progressBar.visibility = ProgressBar.VISIBLE

                // Upload the image to Firebase Cloud Storage
                val imageUri = getImageUriFromImageView()
                if (imageUri != null) {
                    val imageFileName = UUID.randomUUID().toString()
                    val imageRef = storageReference.child("images/$imageFileName")
                    val uploadTask = imageRef.putFile(imageUri)

                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        imageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            val imageUrl = downloadUri.toString()

                            // Create a User object with the profile data and image URL
                            val user = User(userName, fatherName, address, phoneNumer, dateOfBirth, imageUrl)

                            // Save the user profile data in the Firestore collection
                            firestore.collection("users").document(currentUserId)
                                .set(user)
                                .addOnSuccessListener {
                                    progressBar.visibility = ProgressBar.GONE
                                    // Show success message or perform any other actions
                                    Toast.makeText(this, "profile complete", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this,LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    progressBar.visibility = ProgressBar.GONE
                                    // Show failure message or perform any other actions
                                }
                        } else {
                            progressBar.visibility = ProgressBar.GONE
                            // Show failure message or perform any other actions
                            Toast.makeText(this, "could not upload user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    progressBar.visibility = ProgressBar.GONE
                    // Handle the case when no image is selected
                    Toast.makeText(this, "kindly set the image", Toast.LENGTH_SHORT).show()
                }

            } else {
                // Handle the case when any of the fields are empty
                Toast.makeText(this, "incomplete information", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            userImage.setImageURI(imageUri)
        }
    }

    private fun getImageUriFromImageView(): Uri? {
        val drawable = userImage.drawable
        return if (drawable != null) {
            val bitmap = drawable.toBitmap()
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "Image",
                null
            )
            Uri.parse(path)
        } else {
            null
        }
    }
}