package com.idlestables.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.idlestables.core.demo.DemoRepository
import com.idlestables.app.idlestables.ui.IdleStablesApp
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // MWA client lives for the Activity lifetime.
    private val walletAdapter by lazy {
        MobileWalletAdapter(
            connectionIdentity = ConnectionIdentity(
                identityUri = Uri.parse("https://idlestables.com"),
                iconUri = Uri.parse("favicon.ico"),
                identityName = "IdleStables"
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val repo = remember { DemoRepository() }
            var authPreview by remember { mutableStateOf(walletAdapter.authToken?.take(10)) }

            MaterialTheme {
                Surface(modifier = Modifier) {
                    IdleStablesApp(
                        repo = repo,
                        onConnectWallet = {
                            onConnectWallet { authPreview = walletAdapter.authToken?.take(10) }
                        },
                        authTokenPreview = authPreview,
                    )
                }
            }
        }
    }

    private fun onConnectWallet(onDone: () -> Unit) {
        val sender = ActivityResultSender(this)
        lifecycleScope.launch {
            walletAdapter.connect(sender)
            // For now we just refresh the preview; full auth/account surfacing later.
            onDone()
        }
    }
}
