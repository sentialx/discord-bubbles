package com.sential.discordbubbles

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
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    var service: Intent? = null

    private lateinit var webView: WebView

    private lateinit var cookieManager: CookieManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))

        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(intent, 5469)
        }

        webView = findViewById(R.id.webview)

        service = Intent(this, OverlayService::class.java)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                if (request.url.toString() == "https://discordapp.com/api/v6/users/@me/library") {
                    val token = request.requestHeaders.getValue("Authorization")
                    login(token)
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

        clearWebViewData()

        webView.loadUrl("https://discordapp.com/login")
    }

    fun clearWebViewData() {
        WebStorage.getInstance().deleteAllData()
    }

    fun login(token: String?) {
        runOnMainLoop {
            if (token != null) {
                Client(token)

                clearWebViewData()
                webView.destroy()
                (webView.parent as ViewGroup).removeView(webView)

                Toast.makeText(
                    this, "Logged in successfully",
                    Toast.LENGTH_LONG
                ).show()

                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startService(service)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (service == null && Settings.canDrawOverlays(this)) {
            service = Intent(this, OverlayService::class.java)
            startService(service)
        }
    }
}

