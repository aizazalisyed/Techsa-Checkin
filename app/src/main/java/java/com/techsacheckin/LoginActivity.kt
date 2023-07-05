package java.com.techsacheckin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {



    //TAG for logging
    private val TAG = "LoginActivity"

    //fire base auth
    private lateinit var auth: FirebaseAuth

    //widgets declaration
    private lateinit var emailEditText : EditText
    private lateinit var passwordEditText : EditText
    private lateinit var signUpButton : Button
    private lateinit var loginButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //initialization of Firebase Auth
        auth = FirebaseAuth.getInstance()

        //initialization of widgets
        signUpButton = findViewById(R.id.signUpButton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)


        //Function calling
        singUpButtonClick()
        loginButtOnclick()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun singUpButtonClick(){

        val i = Intent(this@LoginActivity, SignUpActivity::class.java)
        signUpButton.setOnClickListener {
            startActivity(i)
        }
    }

    private fun loginButtOnclick(){

        loginButton.setOnClickListener {
            if(emailEditText.text.isBlank() || passwordEditText.text.isBlank()){
                Toast.makeText(this, "Information Incomplete", Toast.LENGTH_SHORT).show()
            }
            else{
                signIn(email = emailEditText.text.toString(),
                    password = passwordEditText.text.toString()
                )
            }
        }
    }


    private fun signIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    val i = Intent(this@LoginActivity, MainActivity::class.java)
                    finish()
                    startActivity(i)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}