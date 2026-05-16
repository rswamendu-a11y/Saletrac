package com.exclusive.saletrac.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.exclusive.saletrac.data.AppDatabase
import com.exclusive.saletrac.databinding.FragmentDashboardBinding
import com.exclusive.saletrac.domain.SalesRepository
import com.exclusive.saletrac.util.SalesAnalyticsEngine
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: DashboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val engine = SalesAnalyticsEngine()
        val repository = SalesRepository(database.productCatalogDao(), database.transactionDao(), engine)

        val factory = DashboardViewModelFactory(repository, engine)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = DashboardAdapter()
        binding.rvDashboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDashboard.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dashboardItems.collect { items ->
                    if (items.isEmpty()) {
                        binding.rvDashboard.visibility = View.GONE
                        binding.emptyStateContainer.visibility = View.VISIBLE
                    } else {
                        binding.emptyStateContainer.visibility = View.GONE
                        binding.rvDashboard.visibility = View.VISIBLE
                        adapter.items = items
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
