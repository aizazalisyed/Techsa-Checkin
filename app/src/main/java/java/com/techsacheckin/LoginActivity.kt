package java.com.techsacheckin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
    private lateinit var progressBar : ProgressBar
    private lateinit var googleSignIn : Button
    private val RC_SIGN_IN = 123


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
        progressBar = findViewById(R.id.progressBar)
        googleSignIn = findViewById(R.id.googleSignIn)

        //Function calling
        singUpButtonClick()
        loginButtOnclick()
        googleSignInButtonClick()

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

                progressBar.visibility = ProgressBar.VISIBLE
                signIn(email = emailEditText.text.toString(),
                    password = passwordEditText.text.toString()
                )
            }
        }
    }


    private fun signIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                progressBar.visibility = ProgressBar.GONE
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

    private fun googleSignInButtonClick() {


        googleSignIn.setOnClickListener {

         //   progressBar.visibility = View.VISIBLE
            // Configure Google Sign-In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            // Create a GoogleSignInClient object with the options specified by gso.
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                // Google Sign-In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val i = Intent(this@LoginActivity, UserProfileActivity::class.java)
                    finish()
                    startActivity(i)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

}