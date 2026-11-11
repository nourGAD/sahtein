package com.nourgad.sahtein

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import com.nourgad.sahtein.notify.Alarms
import com.nourgad.sahtein.ui.AppRoot
import com.nourgad.sahtein.ui.AppVM
import com.nourgad.sahtein.ui.SahteinTheme

class MainActivity : ComponentActivity() {
    private val vm: AppVM by viewModels()
    private val openRequest = mutableStateOf<String?>(null)

    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openRequest.value = intent?.getStringExtra(Alarms.EXTRA_OPEN)
        setContent {
            SahteinTheme {
                AppRoot(
                    vm = vm,
                    openRequest = openRequest.value,
                    onOpenHandled = { openRequest.value = null },
                    requestNotifications = {
                        if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(Alarms.EXTRA_OPEN)?.let { openRequest.value = it }
    }
}
