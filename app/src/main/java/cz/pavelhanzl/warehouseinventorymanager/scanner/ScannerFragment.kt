package cz.pavelhanzl.warehouseinventorymanager.scanner

import android.animation.Animator
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.Result
import cz.pavelhanzl.warehouseinventorymanager.MainActivity
import cz.pavelhanzl.warehouseinventorymanager.R
import cz.pavelhanzl.warehouseinventorymanager.dashboard.DashboardFragmentDirections
import cz.pavelhanzl.warehouseinventorymanager.databinding.FragmentScannerBinding
import cz.pavelhanzl.warehouseinventorymanager.warehouse.warehouseDetail.WarehousesDetailFragmentViewModel
import cz.pavelhanzl.warehouseinventorymanager.repository.Constants
import cz.pavelhanzl.warehouseinventorymanager.repository.hideKeyboard
import cz.pavelhanzl.warehouseinventorymanager.repository.vibratePhoneError
import cz.pavelhanzl.warehouseinventorymanager.repository.vibratePhoneSuccess
import cz.pavelhanzl.warehouseinventorymanager.service.BaseFragment
import cz.pavelhanzl.warehouseinventorymanager.service.observeInLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class ScannerFragment : BaseFragment() {
    private lateinit var codeScanner: CodeScanner
    private val CAMERA_REQUEST_CODE = 1016

    private val args: ScannerFragmentArgs by navArgs()
    private lateinit var binding: FragmentScannerBinding
    lateinit var viewModel: ScannerFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //předá argumenty do viewmodelu
        if (savedInstanceState == null) {
            viewModel = ViewModelProvider(this).get(ScannerFragmentViewModel::class.java)
            viewModel.warehouseObject.value = args.warehouseObject

            //pokud nepřicházíme z lokace, která nám nepředává objekt skladu, tak si přejeme načíst všechny aktuální položky na skladě
            if (args.warehouseObject!=null){
               GlobalScope.launch(Dispatchers.IO ) {
                   viewModel.getListOfActualWarehouseItems() }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setupPermissions()
        //binduje a přiřazuje viewmodel
        binding = FragmentScannerBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        registerObservers()

        //nastaví fragment do požadovaného módu
        when (args.mode) {
            Constants.ADDING_STRING -> runFragmentInAddingMode()
            Constants.REMOVING_STRING -> runFragmentInRemovingMode()
            Constants.READING_STRING -> runFragmentInReadingMode()
        }

        val scannerView = binding.scannerView
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.formats = CodeScanner.ONE_DIMENSIONAL_FORMATS
        codeScanner.scanMode = ScanMode.SINGLE

        //callback pro úspěšně naskenovaný čárový kod
        codeScanner.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread() {
                viewModel.decodeCode(it)
                handleDecodeCallbackIfScannerIsInReadingMode(it)
            }
        }

        //callback pro neúspěšně naskenovaný čárový kod
        codeScanner.errorCallback = ErrorCallback {
            Log.e("camera error", it.message!!)
        }

        setUpSliderForScanningSpeed()

        return binding.root
    }

    private fun handleDecodeCallbackIfScannerIsInReadingMode(it: Result) {

        if (viewModel.scannerMode == Constants.READING_STRING) {
            playBeepSound()
            vibratePhone()
            // vloží do argumentu výsledek skennování a předá observeru ve fragmentu, který vyvolal skenner ve čtecím režimu
            findNavController().previousBackStackEntry?.savedStateHandle?.set("scannedBarcode", it.text)
            //pokud se nacházíme ve čtecím režimu tak se chceme hned dostat zpět na lokaci odkud jsme na skenner přišli
            findNavController().navigateUp()
        }
    }

    private fun runFragmentInReadingMode() {
        hideKeyboard(requireActivity())
        viewModel.scannerMode = Constants.READING_STRING


        binding.guideline2.setGuidelinePercent(1F)

        binding.llSwitchContinuouslyScan.visibility = View.GONE
        binding.fabStartScaning.hide()


        binding.toggleButtonsScannerFragment.visibility = View.GONE

    }

    private fun runFragmentInRemovingMode() {
        viewModel.scannerMode = Constants.REMOVING_STRING
        binding.toggleButtonsScannerFragment.check(R.id.removingbutton_scannerFragment)
    }

    private fun runFragmentInAddingMode() {
        viewModel.scannerMode = Constants.ADDING_STRING
        binding.toggleButtonsScannerFragment.check(R.id.addingbutton_scannerFragment)
    }

    private fun registerObservers() {
        //observer na změnu přepínače přidávání/odebírání
       binding.toggleButtonsScannerFragment.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
          if(checkedId == R.id.removingbutton_scannerFragment) {
             viewModel.scannerMode = Constants.REMOVING_STRING
          } else if (checkedId == R.id.addingbutton_scannerFragment) {
              viewModel.scannerMode = Constants.ADDING_STRING
          } else{
              viewModel.scannerMode = Constants.READING_STRING
          }
        }

        //observer na switch pro kontinuální skenování
        viewModel.continuouslyScaning.observe(viewLifecycleOwner, Observer { it ->
            if (it || args.mode == Constants.READING_STRING) {//skenujeme kontinuálně nebo jsme v reading modu
                //při switchi ze single modu chceme aktivovat scanner
                viewModel.scannerStartPreview()
                binding.fabStartScaning.hide()
            } else {
                Log.d("fabik", "if")
                binding.fabStartScaning.show()
            }

        })

 /*       //při úspěšném decodu provede tyto akce
        viewModel.barcodeScanned.observe(viewLifecycleOwner, Observer { it ->
            if (it) {
                Log.d("skener", "hraju na city")
                playBeepSound()
                vibratePhone()
            }
        })*/

        //zapíná preview v kontinuálním módu po uběhnutí časového limitu
        viewModel.scannerStartPreview.observe(viewLifecycleOwner, Observer { it ->
            if (it) {
                codeScanner.startPreview()
                Log.d("skenerek", "zapinam skener")
            } else {
                codeScanner.stopPreview()
                Log.d("skenerek", "vypinam skener")
            }
        })
    }

    private fun setUpSliderForScanningSpeed() {
        //nastavuje rychlost skenování při posunu slideru, složité na realizaci v MVVM, proto change listener
        binding.sliderScanningSpeed.addOnChangeListener { slider, value, fromUser ->

            viewModel.scanningSpeed.value = value

            viewModel._scanningProgress.value = if (value.toInt() == 0) {
                viewModel.minimumSpeed * 1000
            } else {
                value * 1000
            }

            setUpSpeedOfLottieAnimation(value)

        }

        //nastavuje label u slideru pro rychlost automatického snímání
        binding.sliderScanningSpeed.setLabelFormatter { value: Float ->
            val format = resources.getString(R.string.scanningSpeedLabel) + "${value.toInt()}" + resources.getString(R.string.secondsShotFormat)
            format.format(value.toDouble())
        }
    }

    private fun setUpSpeedOfLottieAnimation(value: Float) {
        if (value.toInt() == 0) {
            binding.lottieSucessErrorAnimScannerFragment.speed = 2F / viewModel.minimumSpeed
        } else {
            binding.lottieSucessErrorAnimScannerFragment.speed = 2F / value
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun playBeepSound() {
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
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("práva", "práva ke kameře neudělena")
                    Toast.makeText(requireContext(), resources.getString(R.string.needsCameraPermission), Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                } else {
                    Log.d("práva", "práva ke kameře udělena")
                    // permisions successfuly granted
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    ScannerFragmentViewModel.Event.NavigateBack -> {
                        findNavController().navigateUp()
                        hideKeyboard(activity as MainActivity)
                    }

                    ScannerFragmentViewModel.Event.PlaySuccessAnimation -> {
                        playSuccessErrorAnimation(true)
                    }
                    ScannerFragmentViewModel.Event.PlayErrorAnimation -> {
                        playSuccessErrorAnimation(false)
                    }
                    is ScannerFragmentViewModel.Event.NonExistingItem -> {
                        //nastaví skener na single scan
                        codeScanner.scanMode=ScanMode.SINGLE
                        //freezne kameru s naskenovaným kódem
                        codeScanner.stopPreview()



                        //zobrazí dialog s výzvou k opuštění cizího skladu
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Neexistující položka")
                            .setMessage("Položka s čárovým kódem "+ it.scannedBarcode +" zatím neexistuje. \n\nPřejete si ji vytvořit?")
                            .setNegativeButton(R.string.no) { dialog, which ->
                              // zrušení dialogu
                                codeScanner.startPreview()
                            }
                            .setPositiveButton(R.string.yes) { dialog, which ->
                                val action = ScannerFragmentDirections.actionScannerFragmentToCreateEditItemFragment(warehouseId = viewModel.warehouseObject.value!!.warehouseID, scannedBarcodeValue = it.scannedBarcode, warehouseItemObject = null)
                                findNavController().navigate(action)
                            }
                            .show()

                    }
                    is ScannerFragmentViewModel.Event.SendToast -> {
                        Log.d("skenerek", "vypinam skener")
                        Toast.makeText(requireContext(),it.toastMessage,Toast.LENGTH_LONG).show()
                    }
                    ScannerFragmentViewModel.Event.PlayBeepSoundAndVibrate -> {
                        playBeepSound()
                        vibratePhone()
                    }

                }
            }.observeInLifecycle(viewLifecycleOwner)


    }

    private fun playSuccessErrorAnimation(success: Boolean) {

        //nastaví odpovídající animaci
        if (success) {
            Log.d("skenerek", "budu prehravat animaci uspechu")
            binding.lottieSucessErrorAnimScannerFragment.setAnimation("success.json")


        } else {
            binding.lottieSucessErrorAnimScannerFragment.setAnimation("error.json")
            Log.d("skenerek", "budu prehravat animaci neuspechu")
            //zavibruje error
            vibratePhoneError(requireContext())
        }

        //zobrazí a přehraje animaci
        binding.lottieSucessErrorAnimScannerFragment.visibility = View.VISIBLE
        binding.lottieSucessErrorAnimScannerFragment.playAnimation()
        Log.d("skenerek", "prehravam animaci")

        binding.lottieSucessErrorAnimScannerFragment.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                //Log.d("Animation:", "start")

            }

            override fun onAnimationEnd(animation: Animator?) {
                //Log.d("Animation:", "end")
                //skryje animaci po dokončení
                try {
                    Log.d("skenerek", "animace skoncila")
                    binding.lottieSucessErrorAnimScannerFragment.visibility = View.GONE
                } catch (ex: Exception) {
                    ex.toString()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                try {
                    Log.d("skenerek", "animace zrusena")
                    binding.lottieSucessErrorAnimScannerFragment.visibility = View.GONE
                } catch (ex: Exception) {
                    ex.toString()
                }
            }

            override fun onAnimationRepeat(animation: Animator?) {
                //Log.e("Animation:", "repeat")
            }
        })

    }

}