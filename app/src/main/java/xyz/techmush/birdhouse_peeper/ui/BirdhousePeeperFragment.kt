package xyz.techmush.birdhouse_peeper.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xyz.techmush.birdhouse_peeper.databinding.BirdhousePeeperFragmentBinding
import xyz.techmush.birdhouse_peeper.vm.BirdhousePeeperViewModel

@AndroidEntryPoint
class BirdhousePeeperFragment : Fragment() {

    private val args: BirdhousePeeperFragmentArgs by navArgs()
    private lateinit var viewModel: BirdhousePeeperViewModel
    private lateinit var binding: BirdhousePeeperFragmentBinding
    private lateinit var webView: WebView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        viewModel = ViewModelProvider(this).get(BirdhousePeeperViewModel::class.java)
        binding = BirdhousePeeperFragmentBinding.inflate(inflater, container,false)
        binding.vm = viewModel

        webView = binding.webView
        webView.setInitialScale(100)
        webView.setBackgroundColor(Color.TRANSPARENT)

        viewModel.setArgs(args)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.event.observe(viewLifecycleOwner, { event -> when (event) {

            is BirdhousePeeperViewModel.Event.LoadUrl -> {
                // todo hack to confirm the URLs got really processed
                Timber.d("URL: ${event.url}")

                val urls = listOf(
                    "http://${event.birdhouse.ip}:${event.birdhouse.port}/control?var=quality&val=4",
                    "http://${event.birdhouse.ip}:${event.birdhouse.port}/control?var=framesize&val=9",
                    "http://${event.birdhouse.ip}:${event.birdhouse.port}/control?var=led_intensity&val=49",
                    event.url)
                val numbersIterator = urls.iterator()

                webView.webViewClient = object: WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        if (numbersIterator.hasNext()) {
                            val nextUrl = numbersIterator.next()
                            webView.loadUrl(nextUrl)
                        }
                    }

                    override fun onReceivedHttpError (
                        view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        Timber.w("onReceivedHttpError: ${request.url}, ${errorResponse.statusCode}: ${errorResponse.reasonPhrase}")
                    }

                    override fun onReceivedError (
                        view: WebView, request: WebResourceRequest, error: WebResourceError) {
                        super.onReceivedError(view, request, error)
                        Timber.w("onReceivedError: ${request.url}, ${error.errorCode}: ${error.description}")
                    }

                }
                webView.loadUrl(numbersIterator.next())
            }
            is BirdhousePeeperViewModel.Event.NavigateEdit -> {
                findNavController().navigate(BirdhousePeeperFragmentDirections
                    .actionBirdhousePeeperFragmentToBirdhouseConfigFragment(event.birdhouse.address, ""))
            }
        } })
    }
}