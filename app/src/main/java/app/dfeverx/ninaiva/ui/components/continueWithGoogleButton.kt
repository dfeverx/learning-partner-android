package app.dfeverx.ninaiva.ui.components

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import app.dfeverx.ninaiva.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@Composable
fun ContinueWithGoogleButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit,
    updateLoading: (Boolean) -> Unit
) {
    val auth = Firebase.auth


    val context = LocalContext.current

    val gso: GoogleSignInOptions = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(context, R.string.web_client_id))
        .requestEmail()
        .requestProfile()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("TAG", "ContinueWithGoogleButton: ${result.resultCode},${Activity.RESULT_OK}")
//            if (result.resultCode == Activity.RESULT_OK) {
            Log.d("TAG", "ContinueWithGoogleButton: result ok ${result.data}")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data!!)
            // Handle successful sign in with task

            Log.d("TAG", "ContinueWithGoogleButton: isSuccess")
            try {
                // Google SignIn was successful, authenticate with Firebase
                val account: GoogleSignInAccount =
                    task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)


                if (auth.currentUser?.isAnonymous == true) {
                    auth.currentUser?.linkWithCredential(credential)
                        ?.addOnSuccessListener {
                            Log.d("TAG", "ContinueWithGoogleButton: $it")
                            onSuccess(it.user!!.uid)
//                            update name and dp
                            val user = it.user
                            user?.let {
                                val name = it.displayName
                                val photoUrl = it.photoUrl

                                if (name == null || photoUrl == null) {
                                    updateUserProfile(account)
                                }
                            }

                        }?.addOnFailureListener {
                            Log.d("TAG", "ContinueWithGoogleButton: $it")
                            onFailure(it)
                        }
                } else {
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            Log.d("TAG", "ContinueWithGoogleButton: $it")
                            onSuccess(it.user!!.uid)
                        }.addOnFailureListener {
                            Log.d("TAG", "ContinueWithGoogleButton: $it")
                            onFailure(it)
                        }
                }


            } catch (e: Exception) {
                Log.d("TAG", "ContinueWithGoogleButton: ${e.message}")
                onFailure(e)

            }
            Log.d("TAG", "ContinueWithGoogleButton: end")
        }




    Button(
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
        onClick = {
//            updateLoading(true)
            launcher.launch(googleSignInClient.signInIntent)
        }) {
        Image(
            painter = painterResource(id = R.drawable.google_logo),
            contentDescription = "",
            Modifier
                .padding(end = 8.dp)
                .size(30.dp)
        )
        Text(
            modifier = Modifier.padding(6.dp),
            text = "Continue with Google",
            color = MaterialTheme.colorScheme.surface
        )
    }
}

fun updateUserProfile(account: GoogleSignInAccount) {
    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(account.displayName)
        .setPhotoUri(account.photoUrl)
        .build()

    FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)
        ?.addOnCompleteListener { task ->
            Log.d("TAG", "updateUserProfile: success")
            /* if (task.isSuccessful) {
                 // Profile updated successfully
             } else {
                 // Handle error
             }*/
        }
}



