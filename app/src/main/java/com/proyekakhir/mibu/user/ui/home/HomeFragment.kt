package com.proyekakhir.mibu.user.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyekakhir.mibu.R
import com.proyekakhir.mibu.bidan.ui.network.NetworkConnection
import com.proyekakhir.mibu.databinding.FragmentHomeBinding
import com.proyekakhir.mibu.user.api.UserPreference
import com.proyekakhir.mibu.user.api.dataStore
import com.proyekakhir.mibu.user.api.response.DataArtikelItem
import com.proyekakhir.mibu.user.api.response.IbuResponse
import com.proyekakhir.mibu.user.auth.viewmodel.LoginViewModel
import com.proyekakhir.mibu.user.factory.ViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ListArtikelHomeAdapter
    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    private val loginViewModel: LoginViewModel by activityViewModels {
        ViewModelFactory.getInstance(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        lifecycleScope.launch {
            val dataStore: DataStore<Preferences> = requireContext().dataStore
            val userPreference = UserPreference.getInstance(dataStore)
            val name = userPreference.getSession().firstOrNull()?.name ?: 0
            binding.tvUsername.text = name.toString()
        }

        binding.catatanKehamilan.setOnClickListener {
            findNavController().navigate(R.id.navigation_catatan_kehamilan_navbar)
            activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        }

        binding.catatanAnak.setOnClickListener {
            findNavController().navigate(R.id.navigation_catatan_anak)
            activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        }

        binding.artikel.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_artikelFragment)
            activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        }

        adapter = ListArtikelHomeAdapter(listOf())
        val rvArtikel = binding.rvArtikelHome
        rvArtikel.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvArtikel.setHasFixedSize(true)
        rvArtikel.adapter = adapter

        viewModel.artikel.observe(viewLifecycleOwner, Observer { response ->
            val list = response.dataArtikel
            if (list != null) {
                adapter.listArtikel = list
                binding.noDataHome.visibility = View.GONE
            } else {
                binding.noDataHome.visibility = View.VISIBLE
            }

            Log.d("artikelapi", response.dataArtikel.toString())
        })

        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer { loginResponse ->
            // When login result is updated, trigger data fetch
            viewModel.getHpl()
        })

        viewModel.hpl.observe(viewLifecycleOwner, Observer { response ->
            getHplDate(response)
        })

        adapter.listener = object : ListArtikelHomeAdapter.OnItemClickListenerHome {
            override fun onItemClick(item: DataArtikelItem) {
                val bundle = Bundle()
                bundle.putSerializable("itemData", item)
                findNavController().navigate(
                    R.id.action_navigation_home_to_detailArtikelFragment2,
                    bundle
                )
            }
        }

        val progressBar = binding.pbArtikelHome
        viewModel.isLoading.observe(requireActivity(), Observer { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        })

        binding.fabHubungiBidan.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_chatBidanFragment)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //check connection
        val networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(requireActivity()) {
            if (isAdded) {
                if (it) {
                    binding.ivConnection.visibility = View.GONE
                    binding.tvConnection.visibility = View.GONE
                    binding.textView4.visibility = View.VISIBLE
                    binding.cardView.visibility = View.VISIBLE
                    binding.fabHubungiBidan.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "Not Connected", Toast.LENGTH_SHORT).show()
                    binding.ivConnection.visibility = View.VISIBLE
                    binding.tvConnection.visibility = View.VISIBLE
                    binding.textView4.visibility = View.GONE
                    binding.cardView.visibility = View.GONE
                    binding.pbArtikelHome.visibility = View.GONE
                    binding.fabHubungiBidan.visibility = View.GONE

                }
            }
        }
    }

    private fun getHplDate(response: IbuResponse) {
        lifecycleScope.launch {
            val dataStore: DataStore<Preferences> = requireContext().dataStore
            val userPreference = UserPreference.getInstance(dataStore)
            val userId = userPreference.getSession().firstOrNull()?.id ?: 0
            Log.d("useridget", userId.toString())

            // Filter the list based on the user ID
            val filteredList = response.dataIbuHamils?.filter { it?.id == userId } ?: emptyList()
            Log.d("kesehatanapi", filteredList.toString())

            // Check if the filtered list has any items
            if (filteredList.isNotEmpty()) {
                // Assuming there's only one item in the filtered list
                val ibuHamil = filteredList[0]
                val tanggalHpl = ibuHamil?.tanggalHpl
                Log.e("getHplDate", tanggalHpl.toString())
                if (!tanggalHpl.isNullOrEmpty()) {
                    calculateAndDisplayCountdown(tanggalHpl)
                } else {
                    Log.e("getHplDate", "Tanggal HPL is null or empty")
                }
            } else {
                Log.e("getHplDate", "No matching data found for user ID: $userId")
            }
        }
    }


    private fun calculateAndDisplayCountdown(edbDateString: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Ensure this matches your date format
        try {
            val edbDate = sdf.parse(edbDateString)
            if (edbDate != null) {
                val currentDate = Date()
                val diff = edbDate.time - currentDate.time
                val daysDiff = (diff / (1000 * 60 * 60 * 24)).toInt()

                // Ensure the daysDiff is not negative
                if (daysDiff >= 0) {
                    binding.tvHpl.text = "$daysDiff Days"
                    binding.tvDescHpl.text = "Until your estimated day of birth on $edbDateString"
                } else {
                    binding.tvHpl.text = "The due date has passed."
                    binding.tvDescHpl.visibility = View.GONE
                }
            } else {
                binding.tvHpl.text = "Invalid EDB date format."
                binding.tvDescHpl.visibility = View.GONE
            }
        } catch (e: Exception) {
            binding.tvHpl.text = "Error parsing EDB date."
            binding.tvDescHpl.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}