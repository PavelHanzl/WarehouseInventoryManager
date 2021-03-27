package cz.pavelhanzl.warehouseinventorymanager.scanner

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentScannerBinding
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

class ScannerFragment : BaseFragment() {
    private lateinit var codeScanner: CodeScanner
    private val CAMERA_REQUEST_CODE = 101

    private val args: ScannerFragmentArgs by navArgs()
    private lateinit var binding: FragmentScannerBinding
    lateinit var viewModel: ScannerFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermissions()

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel = ViewModelProvider(this).get(ScannerFragmentViewModel::class.java)
            viewModel._barcodeValue.postValue(args.mode)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binduje a přiřazuje viewmodel
        binding = FragmentScannerBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        val scannerView = binding.scannerView
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)

        codeScanner.scanMode = ScanMode.SINGLE

        //observer na switch pro kontinuální skenování
        viewModel.continuouslyScaning.observe(viewLifecycleOwner, Observer { it ->
            if (it){
                Log.d("Switch", "true")
                codeScanner.startPreview()
                //codeScanner.scanMode = ScanMode.CONTINUOUS
            } else {
                Log.d("Switch", "false")
               // codeScanner.scanMode = ScanMode.SINGLE
            }
        })

        viewModel._barcodeValue.postValue("0")

        Log.d("Scanner", "pred callbackem" )
        codeScanner.decodeCallback = DecodeCallback {
            Log.d("Scanner", "v callbacku pred corutinou" )



            activity.runOnUiThread {

                Log.d("Scanner", "zacatek uithreadu" )
                GlobalScope.launch(Dispatchers.IO){
                    Log.d("Scanner", "zacatek corutine" )
                    vibratePhone()
                    playBeepSound()

                    Log.d("Scanner", "uprostred mezi ifama pred delay" )
                    var counter = viewModel._barcodeValue.value!!.toInt()
                    counter++

                    viewModel._barcodeValue.postValue(counter.toString())


                    withContext(Main){
                    Toast.makeText(activity, it.text, Toast.LENGTH_LONG).show()
                    }

                    if(viewModel.continuouslyScaning.value!!) {
                        delay(viewModel.scanningSpeed.value!!*1000L)
                        codeScanner.startPreview()
                    }


                    Log.d("Scanner", "konec corutine" )

                }

                Log.d("Scanner", "konec ui threadu" )
            }
            Log.d("Scanner", "v callbacku za corutine" )
        }



        binding.fabStartScaning.setOnClickListener {
            codeScanner.startPreview()
        }


        binding.sliderScanningSpeed.addOnChangeListener { slider, value, fromUser ->
            viewModel.scanningSpeed.value=value.toInt()
        }
















        return binding.root
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun playBeepSound(){
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

     private fun vibratePhone() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    private fun setupPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            reqestCameraPermission()
        }
    }

    private fun reqestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Aplikace potřebuje získat povolení ke kameře, abyste mohli využívat tuto funkci.", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                } else {
                    // permisions successfuly granted
                }
            }
        }

    }
}