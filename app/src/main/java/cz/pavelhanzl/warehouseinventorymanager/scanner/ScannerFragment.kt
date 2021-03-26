package cz.pavelhanzl.warehouseinventorymanager.scanner

import android.content.pm.PackageManager
import android.os.Bundle
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

        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                Toast.makeText(activity, it.text, Toast.LENGTH_LONG).show()
                viewModel._barcodeValue.postValue(it.text)
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