package xyz.techmush.birdhouse_peeper.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.BleScanListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xyz.techmush.birdhouse_peeper.R
import xyz.techmush.birdhouse_peeper.databinding.BirdhouseFindFragmentBinding
import xyz.techmush.birdhouse_peeper.databinding.ViewScanresultBinding
import xyz.techmush.birdhouse_peeper.vm.BirdhouseFindViewModel
import java.lang.Exception

@AndroidEntryPoint
class BirdhouseFindFragment : Fragment() {

//    companion object {
//        fun newInstance() = BirdhouseFindFragment()
//    }

    private lateinit var viewModel: BirdhouseFindViewModel
    private lateinit var binding: BirdhouseFindFragmentBinding
    private lateinit var espManager: ESPProvisionManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        viewModel = ViewModelProvider(this).get(BirdhouseFindViewModel::class.java)
        binding = BirdhouseFindFragmentBinding.inflate(inflater, container,false)
        binding.lifecycleOwner = viewLifecycleOwner // set it here, onViewCreated is too late
        binding.vm = viewModel
        return binding.root
    }


    // permissions managed in IntroActivity
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.peripherals.adapter = ScanResultListAdapter(viewModel)

        viewModel.scanning.postValue(true)
        espManager = ESPProvisionManager.getInstance(requireContext())
        espManager.searchBleEspDevices("PROV_", object: BleScanListener {
            override fun scanStartFailed() {
                Timber.e("scanStartFailed")
                Toast.makeText(context, "scanStartFailed", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(e: Exception?) {
                Timber.e(e, "onFailure")
                Toast.makeText(context, "onFailure", Toast.LENGTH_LONG).show()
            }

            override fun onPeripheralFound(device: BluetoothDevice?, scanResult: ScanResult?) {
                Timber.d("onPeripheralFound: $device, $scanResult")

                if (null != device && null != scanResult && null != scanResult.scanRecord && scanResult.scanRecord!!.serviceUuids.isNotEmpty()) {
                    viewModel.onScanResult(scanResult)
                } else {
                    Timber.w("Missing BluetoothDevice or ScanResult or ScanRecord or service UUIDs")
                }
            }

            override fun scanCompleted() {
                Timber.d("scanCompleted")
                viewModel.scanning.postValue(false)
                if (viewModel.peripherals.value.isNullOrEmpty()) {
                    Timber.w("no devices found")
                    Toast.makeText(context, "No birdhouse peepers found!", Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.event.observe(viewLifecycleOwner, { event ->
            when (event) {
                is BirdhouseFindViewModel.Event.NavigateCreateBirdhouse -> {
                    findNavController().navigate(BirdhouseFindFragmentDirections
                        .actionBirdhouseFindFragmentToBirdhouseConfigFragment(
                            event.address, event.uuid))
                }
            }
        })
    }


    // permissions managed in IntroActivity
    @SuppressLint("MissingPermission")
    override fun onDestroyView() {
        super.onDestroyView()
        espManager.stopBleScan()
    }

} // BirdhouseFindFragment


class ScanResultListAdapter(private val viewModel: BirdhouseFindViewModel):
    ListAdapter<ScanResult, ScanResultListAdapter.ViewHolder>(DiffCallback()) {


    class DiffCallback : DiffUtil.ItemCallback<ScanResult>() {
        override fun areItemsTheSame(old: ScanResult, new: ScanResult) =
            old.device.address == new.device.address
        override fun areContentsTheSame(old: ScanResult, new: ScanResult) =
            old.device.address == new.device.address
    }


    class ViewHolder constructor(private val binding: ViewScanresultBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scanResult: ScanResult, viewModel: BirdhouseFindViewModel, adapter: ScanResultListAdapter) {

            binding.scanResult = scanResult
            binding.vm = viewModel
            binding.executePendingBindings()

            if (viewModel.scanResult.value?.device?.address == scanResult.device.address) {
                binding.root.setBackgroundResource(R.color.purple_200)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }

            binding.root.setOnClickListener {
                viewModel.onScanResultClick(scanResult)
                adapter.notifyDataSetChanged() // forced a redraw
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewScanresultBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, this)
    }

} // ScanResultListAdapter


@BindingAdapter("items")
fun setItems(listView: RecyclerView, items: List<ScanResult>?) {
    items?.let { (listView.adapter as ScanResultListAdapter).submitList(it) }
}
