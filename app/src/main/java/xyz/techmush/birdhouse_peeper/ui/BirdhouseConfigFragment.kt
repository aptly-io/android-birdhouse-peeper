package xyz.techmush.birdhouse_peeper.ui

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.ProvisionListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xyz.techmush.birdhouse_peeper.databinding.BirdhouseConfigFragmentBinding
import xyz.techmush.birdhouse_peeper.vm.BirdhouseConfigViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class BirdhouseConfigFragment: Fragment(), OnMapReadyCallback {

    private val args: BirdhouseConfigFragmentArgs by navArgs()

    private lateinit var viewModel: BirdhouseConfigViewModel
    private lateinit var binding: BirdhouseConfigFragmentBinding
    private lateinit var photoPath: String

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // todo
        // hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        // hasSystemFeature(PackageManager.BLUETOOTH_LE)
        // etc

        viewModel = ViewModelProvider(this).get(BirdhouseConfigViewModel::class.java)
        binding = BirdhouseConfigFragmentBinding.inflate(inflater, container,false)
        binding.lifecycleOwner = viewLifecycleOwner // set it here, onViewCreated is too late
        binding.vm = viewModel

        mapView = binding.birdhouseLocationMap
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        viewModel.setArgs(args)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (isBetterLocation(location, viewModel.location.value)) {
                        Timber.d("Better location: ${location}")
                        viewModel.location.postValue(location)
                        val latLng = LatLng(location.latitude, location.longitude)
                        if (null == marker) {
                            marker = googleMap?.addMarker(MarkerOptions().position(latLng).title("Birdhouse"))
                        } else {
                            marker?.position = latLng
                        }
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    }
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner, { event -> when (event) {
            is BirdhouseConfigViewModel.Event.AddPhoto -> { takePicture() }
            is BirdhouseConfigViewModel.Event.Location -> { takeLocation() }
            is BirdhouseConfigViewModel.Event.Configure -> { configure() }
        } })
    }


    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        mapView.onPause()
    }


    @SuppressLint("MissingPermission") // handled in introActivity
    private fun takeLocation() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper())
    }


    private fun configure() {
        Timber.d("configure")

        // todo on a background thread (especially those 3second connection time)
        // todo refactor the call-back/listeners
        // todo handle credentials properly
        // todo change the device's prefix (e.g. "BHP")
        // todo provision for running an AP or STA
        // todo fail-safe
        // todo release resources
        val bluetoothManager = requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager!!.adapter
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(args.address)

        val espManager = ESPProvisionManager.getInstance(requireContext())
//        val espDevice = espManager.createESPDevice(
//            ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1)
        val espDevice = espManager.createESPDevice(
            ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1)
        espDevice.proofOfPossession = viewModel.pop.value

        espDevice.connectBLEDevice(bluetoothDevice, args.uuid)

        // todo wait in background until BLE is connected!
        Thread.sleep(3000)

        espDevice.provision(viewModel.ssid.value, viewModel.password.value, object: ProvisionListener {

            // progress
            override fun wifiConfigSent() {
                Timber.d("wifiConfigSent")
            }

            override fun wifiConfigApplied() {
                Timber.d("wifiConfigApplied")
            }

            override fun deviceProvisioningSuccess() {
                Timber.d("deviceProvisioningSuccess")
                espDevice.disconnectDevice()
            }

            // failures
            override fun createSessionFailed(e: java.lang.Exception?) {
                Timber.w("provision failed: $e")
            }

            override fun wifiConfigFailed(e: java.lang.Exception?) {
                Timber.w("provision failed in wifiConfig: $e")
                espDevice.disconnectDevice()
            }

            override fun wifiConfigApplyFailed(e: java.lang.Exception?) {
                Timber.w("provision failed in wifiConfigApply: $e")
                espDevice.disconnectDevice()
            }

            override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason?) {
                Timber.w("provision failed from device: $failureReason")
                espDevice.disconnectDevice()
            }

            override fun onProvisioningFailed(e: java.lang.Exception?) {
                Timber.w("provision failed onProvisionFailed: $e")
                espDevice.disconnectDevice()
            }
        })

    }


    // https://stackoverflow.com/questions/37485887/how-to-get-accurate-current-location-in-android
    private val TWO_MINUTES: Long = TimeUnit.MINUTES.toNanos(1)

    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        // Check whether the new location fix is newer or older
        val timeDelta = location.elapsedRealtimeNanos - currentBestLocation.elapsedRealtimeNanos
        val isSignificantlyNewer = timeDelta > TWO_MINUTES
        val isSignificantlyOlder = timeDelta < -TWO_MINUTES
        val isNewer = timeDelta > 0

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false
        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200

        // Check if the old and new location are from the same provider
        val isFromSameProvider = isSameProvider(location.provider, currentBestLocation.provider)

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true
        } else if (isNewer && !isLessAccurate) {
            return true
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true
        }
        return false
    }


    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else {
            provider1 == provider2
        }
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "xyz.techmush.birdhouse_peeper.FileProvider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(takePictureIntent, 42)
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("img_${timeStamp}_", ".jpg", storageDir).apply {
            photoPath = absolutePath // Save a file: path for use with ACTION_VIEW intents
            Timber.d("##### photoPath: $photoPath")
        }
    }


    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }


    private fun setImage() {
        // Get the dimensions of the View
        val targetW: Int = binding.birdhouseImage.width
        val targetH: Int = binding.birdhouseImage.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(photoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(photoPath, bmOptions)?.also { bitmap ->
            binding.birdhouseImage.setImageBitmap(bitmap)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42 && resultCode == RESULT_OK) {
            setImage() // assume photoPath contains a valid capture
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

} // BirdhouseConfigFragment