package com.saletrac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.saletrac.domain.SalesRepository

class SalesEntryViewModelFactory(private val repository: SalesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
