package com.parentalcare.parent.ui.screen

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.parentalcare.parent.R

fun getGoogleSignInClient(context: Context): com.google.android.gms.auth.api.signin.GoogleSignInClient {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    val clientId = if (resId != 0) context.getString(resId) else "281374491538-654i110rae7knod8dh6bifal3smmgufb.apps.googleusercontent.com"
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}
