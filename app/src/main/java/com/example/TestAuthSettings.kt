
package com.example
import com.google.firebase.auth.FirebaseAuth
fun test() {
    val auth = FirebaseAuth.getInstance()
    auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
}
