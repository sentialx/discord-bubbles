package com.sential.discordbubbles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.sential.discordbubbles.chatheads.OverlayService
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.*
import android.webkit.PermissionRequest
import com.sential.discordbubbles.client.Client
import com.sential.discordbubbles.utils.runOnMainLoop
import android.content.SharedPreferences
import android.widget.Toast

val REQUEST_CODE = 5469

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    private lateinit var cookieManager: CookieManager

    private lateinit var prefs: SharedPreferences

    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))

        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(intent, REQUEST_CODE)
        }

        webView = findViewById(R.id.webview)
        prefs = getSharedPreferences("data", Context.MODE_PRIVATE)

        token = prefs.getString("token", null)

        if (token == null) {
            showLogin()
        } else {
            login()
            destroyWebView()
        }
    }

    private fun showLogin() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                if (request.url.toString() == "https://discordapp.com/api/v6/users/@me/library") {
                    token = request.requestHeaders.getValue("Authorization")
                    val editor = prefs.edit()

                    editor.putString("token", token)
                    editor.apply()

                    runOnMainLoop {
                        Toast.makeText(
                            this@MainActivity, "Obtained token, logging in...",
                            Toast.LENGTH_LONG
                        ).show()

                        login()

                        destroyWebView()
                    }
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.proceed()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.domStorageEnabled = true

        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        WebStorage.getInstance().deleteAllData()

        webView.loadUrl("https://discordapp.com/login")
    }

    fun destroyWebView() {
        WebStorage.getInstance().deleteAllData()
        webView.destroy()
        (webView.parent as ViewGroup).removeView(webView)
    }

    fun login() {
        if (token != null && !OverlayService.initialized && Settings.canDrawOverlays(this)) {
            val service = Intent(this, OverlayService::class.java)
            service.putExtra("token", token)
            startService(service)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && Settings.canDrawOverlays(this)) {
            login()
        }
    }
}

