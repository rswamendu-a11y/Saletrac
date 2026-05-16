package com.exclusive.saletrac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.exclusive.saletrac.domain.SalesRepository
import com.exclusive.saletrac.util.SalesAnalyticsEngine

class DashboardViewModelFactory(
    private val repository: SalesRepository,
    private val analyticsEngine: SalesAnalyticsEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, analyticsEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
