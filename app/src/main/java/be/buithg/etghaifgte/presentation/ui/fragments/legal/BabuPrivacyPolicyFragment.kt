package be.buithg.etghaifgte.presentation.ui.fragments.legal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import be.buithg.etghaifgte.databinding.FragmentBabuPrivacyPolicyBinding
import be.buithg.etghaifgte.databinding.NetworkErrorLayoutBinding
import be.buithg.etghaifgte.presentation.ui.fragments.main.HomeFragment
import be.buithg.etghaifgte.presentation.ui.fragments.onboarding.WelcomeFragment
import be.buithg.etghaifgte.utils.BabuAppConstants
import be.buithg.etghaifgte.utils.BabuAppConstants.getBabuPreferences
import be.buithg.etghaifgte.utils.BabuAppConstants.showBabuFragment

class BabuPrivacyPolicyFragment(private val urlOffer: String) : Fragment() {

    private lateinit var babuPolicyBinding: FragmentBabuPrivacyPolicyBinding
    private lateinit var babuNetworkErrorBinding: NetworkErrorLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        babuPolicyBinding = FragmentBabuPrivacyPolicyBinding.inflate(inflater, container, false)
        babuNetworkErrorBinding = NetworkErrorLayoutBinding.bind(babuPolicyBinding.root)
        return babuPolicyBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        setupBabuWebView()
        handleInitPrivacy()
        setupDownloadListener()
        setupBackNavigationListener()
    }

    private fun navigateToProjectFragment() {
        val launchedBefore = context?.getBabuPreferences()?.getBoolean(BabuAppConstants.BABU_WELCOME_KEY, false) == true
        if (launchedBefore) {
            parentFragmentManager.showBabuFragment(HomeFragment())
        } else {
            parentFragmentManager.showBabuFragment(WelcomeFragment())
        }
    }

    private fun handleInitPrivacy() {
        if (urlOffer.contains("https://sites.google.com/")) {
            babuPolicyBinding.homeBabuPolicyButton.visibility = View.VISIBLE
            babuPolicyBinding.homeBabuPolicyButton.setOnClickListener {
                navigateToProjectFragment()
            }
        } else {
            babuPolicyBinding.homeBabuPolicyButton.visibility = View.GONE
        }
    }

    private fun setupBabuWebView() {
        ViewCompat.setOnApplyWindowInsetsListener(babuPolicyBinding.root) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val finalPadding = (imeHeight - navBarHeight).coerceAtLeast(0)

            view.setPadding(0, 0, 0, finalPadding)
            insets
        }
        babuPolicyBinding.babuPrivacyView.apply {
            configureWebView()
            loadUrl(urlOffer)
            setWebChromeClient(this, requireActivity())
        }
    }

    private fun setupDownloadListener() {
        babuPolicyBinding.babuPrivacyView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
            setupDownloadManager(url, contentDisposition, mimeType, requireActivity())
        }
    }

    private fun setupBackNavigationListener() {
        babuPolicyBinding.babuPrivacyView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && babuPolicyBinding.babuPrivacyView.canGoBack()) {
                babuPolicyBinding.babuPrivacyView.goBack()
                true
            } else {
                false
            }
        }
    }

    private fun setupDownloadManager(
        url: String, contentDisposition: String, mimeType: String, activity: Activity
    ) {
        val request = DownloadManager.Request(url.toUri()).apply {
            setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            setDescription("Downloading file...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType)
            )
            setAllowedOverMetered(true)
        }
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Downloading file...", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WebView.configureWebView() {
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            useWideViewPort = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = userAgentString.replace("; wv", "")
            setSupportZoom(true)
            setSupportMultipleWindows(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        webViewClient = createWebViewClient()
        acceptCookies()
    }

    private fun createWebViewClient() = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) =
            handleUrlLoading(request.url.toString())

        override fun onReceivedError(
            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
        ) {
            babuPolicyBinding.babuProgressBarView.visibility = View.GONE
            if (!isInternetAvailable()) {
                showNoInternetScreen()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            babuPolicyBinding.babuProgressBarView.visibility = View.GONE
        }
    }

    private fun isInternetAvailable(): Boolean {
        val activity = context as? Activity ?: return true

        if (activity.isFinishing || activity.isDestroyed) return true

        if ((context as? LifecycleOwner)?.lifecycle?.currentState == Lifecycle.State.CREATED) return true

        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetScreen() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        babuNetworkErrorBinding.apply {
            rootNetworkErrorLayout.visibility = View.VISIBLE
            rootNetworkErrorLayout.setOnClickListener { }
            reconnectButton.setOnClickListener {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                babuPolicyBinding.babuPrivacyView.reload()
                rootNetworkErrorLayout.visibility = View.GONE
            }
            offlineModeButton.setOnClickListener {
                navigateToProjectFragment()
            }
        }
    }

    private fun handleUrlLoading(url: String): Boolean {
        return when {
            url.startsWith("tel:") -> launchAppIntent(
                Intent.ACTION_DIAL, url, "Unable to launch Phone app."
            )

            url.startsWith("mailto:") -> launchAppIntent(
                Intent.ACTION_SENDTO, url, "Unable to launch Mail Client."
            )

            url.startsWith("viber:") -> launchAppIntent(
                Intent.ACTION_VIEW, url, "Unable to launch Viber app."
            )

            url.startsWith("tg:") -> launchAppIntent(
                Intent.ACTION_VIEW, url, "Unable to launch Telegram app."
            )

            url.startsWith("whatsapp:") -> launchAppIntent(
                Intent.ACTION_VIEW, url, "Unable to launch WhatsApp app."
            )

            url.startsWith("line://") -> launchAppIntent(
                Intent.ACTION_VIEW, url, "Unable to launch Line app."
            )

            url.startsWith("https://diia.app") -> launchDiiaApp(url)

            url.startsWith("intent://") -> launchIntentLink(url)

            else -> {
                babuPolicyBinding.babuPrivacyView.loadUrl(url)
                false
            }
        }
    }

    private fun launchIntentLink(url: String): Boolean {
        context?.let {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            launchIntent(it, intent, "Unable to launch app")
        }
        return true
    }

    private fun launchAppIntent(action: String, url: String, errorMessage: String): Boolean {
        context?.let {
            launchIntent(it, Intent(action, url.toUri()), errorMessage)
        }
        return true
    }

    private fun launchDiiaApp(url: String): Boolean {
        context?.let {
            launchIntent(
                it, Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    setPackage("ua.gov.diia.app")
                }, "Unable to launch Diia app."
            )
        }
        return true
    }

    private fun launchIntent(context: Context, intent: Intent, errorMessage: String) {
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun WebView.acceptCookies() {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@acceptCookies, true)
        }
    }

    private fun setWebChromeClient(webView: WebView, activity: FragmentActivity) {
        var uploadMessage: ValueCallback<Array<Uri>>? = null
        val activityResultLauncher = activity.activityResultRegistry.register(
            "Image selector", ActivityResultContracts.StartActivityForResult()
        ) { result ->
            uploadMessage?.onReceiveValue(result.data?.let { arrayOf(it.data!!) })
            uploadMessage = null
        }

        webView.webChromeClient = object : WebChromeClient() {
            private var customView: View? = null
            private var fullScreenCallback: CustomViewCallback? = null

            @SuppressLint("SetJavaScriptEnabled")
            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?
            ): Boolean {
                view?.let {
                    val context = view.context
                    val newWebView = WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.userAgentString = settings.userAgentString.replace("; wv", "")
                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onCloseWindow(window: WebView?) {
                                (activity.window.decorView as? FrameLayout)?.removeView(window?.parent as? View)
                            }
                        }
                    }

                    val decorView = activity.window.decorView as FrameLayout

                    activity.window?.let { it1 ->
                        WindowCompat.setDecorFitsSystemWindows(
                            it1, false
                        )
                    }

                    val webViewContainer = FrameLayout(context).apply {
                        setBackgroundColor(Color.TRANSPARENT)
                        addView(
                            newWebView, FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )

                        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                            val systemBarsInsets =
                                insets.getInsets(WindowInsetsCompat.Type.systemBars())

                            val finalPadding = maxOf(imeHeight, systemBarsInsets.bottom)
                            view.setPadding(0, systemBarsInsets.top, 0, finalPadding)

                            insets
                        }

                        activity.onBackPressedDispatcher.addCallback(
                            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                                override fun handleOnBackPressed() {
                                    if (newWebView.canGoBack()) {
                                        newWebView.goBack()
                                    } else {
                                        isEnabled = false
                                        (newWebView.parent as? ViewGroup)?.removeView(newWebView)
                                    }
                                }
                            })
                    }

                    decorView.addView(
                        webViewContainer, FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )

                    ViewCompat.requestApplyInsets(webViewContainer)

                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                    transport?.webView = newWebView
                    resultMsg?.sendToTarget()

                    return true
                } ?: return false
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                customView?.let {
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view
                fullScreenCallback = callback
                (activity.window.decorView as FrameLayout).addView(
                    customView, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }

            override fun onHideCustomView() {
                (activity.window.decorView as FrameLayout).removeView(customView)
                customView = null
                fullScreenCallback?.onCustomViewHidden()
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                uploadMessage?.onReceiveValue(null)
                uploadMessage = filePathCallback
                activityResultLauncher.launch(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }, "Select Image"))
                return true
            }
        }
    }
}