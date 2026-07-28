package com.kanhaji.upasthiti.features.home.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kanhaji.upasthiti.util.KToast

@Composable
fun UpdateButton() {
    val context = LocalContext.current

    IconButton(onClick = {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/kanha321/Upastithi/releases")
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            KToast.show(context, "Unable to open browser")
        }
    }) {
        Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = "Update Available",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
