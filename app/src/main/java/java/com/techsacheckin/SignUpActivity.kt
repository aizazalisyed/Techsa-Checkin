package java.com.techsacheckin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {


    //TAG for logging
    private val TAG = "SignUpActivity"

    //fire base auth
    private lateinit var auth: FirebaseAuth

    //widgets declaration
    private lateinit var emailEditText : EditText
    private lateinit var passwordEditText : EditText
    private lateinit var signUpButton : Button
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //initialization of Firebase Auth
        auth = FirebaseAuth.getInstance()

        //initialization of widgets
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        progressBar = findViewById(R.id.progressBar)

        //Function calling
        signUpButtonClick()
    }

    private fun signUpButtonClick(){
        signUpButton.setOnClickListener {

            progressBar.visibility = ProgressBar.VISIBLE
            if(emailEditText.text.isBlank() || passwordEditText.text.isBlank()){
                Toast.makeText(this, "Information Incomplete", Toast.LENGTH_SHORT).show()
            }
            else{
                signUp(email = emailEditText.text.toString(),
                    password = passwordEditText.text.toString()
                )
            }
        }
    }

    private fun signUp(email : String, password: String){

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = ProgressBar.GONE

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val i = Intent(this@SignUpActivity, UserProfileActivity::class.java)
                    startActivity(i)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

}