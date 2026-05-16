package com.saletrac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saletrac.data.entity.ProductCatalog
import com.saletrac.data.entity.Transaction
import com.saletrac.domain.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

data class SalesEntryState(
    val brand: String = "",
    val model: String = "",
    val variant: String = "",
    val imei: String = "",
    val price: String = "",
    val cashPaid: String = "",
    val cardPaid: String = "",
    val financePaid: String = "",
    val financePartner: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val isDuplicateImei: Boolean = false
)

class SalesEntryViewModel(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesEntryState())
    val uiState: StateFlow<SalesEntryState> = _uiState.asStateFlow()

    val availableProducts: StateFlow<List<ProductCatalog>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSaveEnabled: StateFlow<Boolean> = _uiState.map { state ->
        val price = state.price.toDoubleOrNull() ?: 0.0
        val cash = state.cashPaid.toDoubleOrNull() ?: 0.0
        val card = state.cardPaid.toDoubleOrNull() ?: 0.0
        val finance = state.financePaid.toDoubleOrNull() ?: 0.0
        val totalPaid = cash + card + finance

        // Form valid if all required fields filled, IMEI is valid, and totalPaid == price
        val isFormValid = state.brand.isNotBlank() &&
                state.model.isNotBlank() &&
                state.imei.length == 15 &&
                !state.isDuplicateImei &&
                price > 0 &&
                totalPaid == price
        isFormValid
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    fun updateBrand(brand: String) { _uiState.value = _uiState.value.copy(brand = brand) }
    fun updateModel(model: String) { _uiState.value = _uiState.value.copy(model = model) }
    fun updateVariant(variant: String) { _uiState.value = _uiState.value.copy(variant = variant) }
    fun updatePrice(price: String) { _uiState.value = _uiState.value.copy(price = price) }
    fun updateCash(cash: String) { _uiState.value = _uiState.value.copy(cashPaid = cash) }
    fun updateCard(card: String) { _uiState.value = _uiState.value.copy(cardPaid = card) }
    fun updateFinance(finance: String, partner: String = _uiState.value.financePartner) {
        _uiState.value = _uiState.value.copy(financePaid = finance, financePartner = partner)
    }
    fun updateCustomerDetails(name: String, phone: String) {
        _uiState.value = _uiState.value.copy(customerName = name, customerPhone = phone)
    }

    fun updateImei(imei: String) {
        _uiState.value = _uiState.value.copy(imei = imei)
        if (imei.length == 15) {
            checkDuplicateImei(imei)
        } else {
            // Reset duplicate state if IMEI is not 15 digits
            _uiState.value = _uiState.value.copy(isDuplicateImei = false)
        }
    }

    private fun checkDuplicateImei(imei: String) {
        viewModelScope.launch {
            val exists = repository.isImeiExists(imei)
            _uiState.value = _uiState.value.copy(isDuplicateImei = exists)
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.imei.length != 15 || state.isDuplicateImei) return

        val price = state.price.toDoubleOrNull() ?: 0.0
        val cash = state.cashPaid.toDoubleOrNull() ?: 0.0
        val card = state.cardPaid.toDoubleOrNull() ?: 0.0
        val finance = state.financePaid.toDoubleOrNull() ?: 0.0
        val totalPaid = cash + card + finance

        if (price != totalPaid) return

        val paymentModesJson = JSONObject().apply {
            if (cash > 0) put("Cash", cash)
            if (card > 0) put("Card", card)
            if (finance > 0) {
                val partner = state.financePartner.ifBlank { "Unknown" }
                put("Finance_$partner", finance)
            }
        }.toString()

        val transaction = Transaction(
            timestamp = System.currentTimeMillis(),
            brand = state.brand,
            model = state.model,
            variant = state.variant,
            imei = state.imei,
            price = price,
            paymentMode = paymentModesJson,
            customerName = state.customerName,
            customerPhone = state.customerPhone
        )

        viewModelScope.launch {
            repository.insertTransaction(transaction)
            // Reset state after successful save
            _uiState.value = SalesEntryState()
        }
    }
}
