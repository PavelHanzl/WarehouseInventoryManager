package cz.pavelhanzl.warehouseinventorymanager.about

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * About fragment
 * This fragment uses webview, which dynamically loads website of project into the screen.
 * Temporary disconected
 *
 * @constructor Create empty About fragment
 */
class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Overrides default behavior of pressing back button of device
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (aboutWebView.canGoBack()) aboutWebView.goBack() else findNavController().navigateUp()
            }
        })

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webViewSetUp()
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Web view set up
     *
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewSetUp(){
        aboutWebView.webViewClient = WebViewClient()
        aboutWebView.apply {
            loadUrl(Constants.PROJECTS_WEBSITE_URL)
            settings.javaScriptEnabled = true
        }
    }

}