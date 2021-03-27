package cz.pavelhanzl.warehouseinventorymanager.scanner

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode

import cz.pavelhanzl.warehouseinventorymanager.scanner.ScannerFragmentArgs

import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentScannerBinding

import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        codeScanner.scanMode = ScanMode.CONTINUOUS

        viewModel._barcodeValue.postValue("0")


        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                GlobalScope.launch (Dispatchers.IO){

                    vibratePhone()
                    codeScanner.stopPreview()


                    var counter = viewModel._barcodeValue.value!!.toInt()
                    counter++
                    //Toast.makeText(activity, it.text, Toast.LENGTH_LONG).show()
                    viewModel._barcodeValue.postValue(counter.toString())
                    delay(1000)
                    codeScanner.startPreview()

                }


            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
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