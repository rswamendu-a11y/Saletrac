package com.saletrac.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.saletrac.R
import com.saletrac.data.AppDatabase
import com.saletrac.databinding.FragmentSalesEntryBinding
import com.saletrac.domain.SalesRepository
import com.saletrac.util.SalesAnalyticsEngine
import kotlinx.coroutines.launch

class SalesEntryFragment : Fragment() {

    private var _binding: FragmentSalesEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SalesEntryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val repository = SalesRepository(database.productCatalogDao(), database.transactionDao(), SalesAnalyticsEngine())
        val factory = SalesEntryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SalesEntryViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.etBrand.addTextChangedListener(createTextWatcher { viewModel.updateBrand(it) })
        binding.etModel.addTextChangedListener(createTextWatcher { viewModel.updateModel(it) })
        binding.etVariant.addTextChangedListener(createTextWatcher { viewModel.updateVariant(it) })
        binding.etPrice.addTextChangedListener(createTextWatcher { viewModel.updatePrice(it) })
        binding.etCash.addTextChangedListener(createTextWatcher { viewModel.updateCash(it) })
        binding.etCard.addTextChangedListener(createTextWatcher { viewModel.updateCard(it) })
        binding.etFinance.addTextChangedListener(createTextWatcher { viewModel.updateFinance(it) })

        // Setup Finance Partner Dropdown
        val partners = resources.getStringArray(R.array.finance_partners)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, partners)
        binding.etFinancePartner.setAdapter(adapter)
        binding.etFinancePartner.addTextChangedListener(createTextWatcher { viewModel.updateFinance(binding.etFinance.text.toString(), it) })

        binding.etCustomerName.addTextChangedListener(createTextWatcher { viewModel.updateCustomerDetails(it, binding.etCustomerPhone.text.toString()) })
        binding.etCustomerPhone.addTextChangedListener(createTextWatcher { viewModel.updateCustomerDetails(binding.etCustomerName.text.toString(), it) })

        binding.etImei.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                viewModel.updateImei(text)
            }
        })

        binding.tilImei.setEndIconOnClickListener {
            // Simulated ML Kit Scanner logic. In reality this opens a camera intent.
            // Using placeholder image since camera testing fails on VM
            try {
                // val image = InputImage.fromBitmap(myBitmap, 0)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
                val scanner = BarcodeScanning.getClient(options)
                Toast.makeText(context, "ML Kit Scanner Launched (Placeholder)", Toast.LENGTH_SHORT).show()
                // scanner.process(image).addOnSuccessListener { barcodes -> ... }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveTransaction()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        if (state.isDuplicateImei) {
                            binding.tilImei.error = getString(R.string.duplicate_imei_error)
                        } else if (state.imei.isNotEmpty() && state.imei.length < 15) {
                            binding.tilImei.error = getString(R.string.imei_length_error)
                        } else {
                            binding.tilImei.error = null
                        }
                    }
                }

                launch {
                    viewModel.isSaveEnabled.collect { isEnabled ->
                        binding.btnSave.isEnabled = isEnabled
                    }
                }

                launch {
                    viewModel.availableProducts.collect { products ->
                        val modelNames = products.map { it.modelName }.distinct()
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modelNames)
                        binding.etModel.setAdapter(adapter)
                    }
                }
            }
        }
    }

    private fun createTextWatcher(onChanged: (String) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onChanged(s?.toString() ?: "")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
