package xyz.techmush.birdhouse_peeper.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
            is BirdhousePeeperViewModel.Event.Navigate -> {
                Timber.d("URL: ${event.url}")
                webView.loadUrl(event.url)
            }
        } })
    }
}