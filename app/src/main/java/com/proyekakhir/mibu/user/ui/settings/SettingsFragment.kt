package com.proyekakhir.mibu.user.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.proyekakhir.mibu.R
import com.proyekakhir.mibu.bidan.ui.auth.BidanLoginActivity
import com.proyekakhir.mibu.databinding.FragmentBidanSettingsBinding
import com.proyekakhir.mibu.databinding.FragmentSettingsBinding
import com.proyekakhir.mibu.user.factory.ViewModelFactory
import com.proyekakhir.mibu.user.firebase.FirebaseRepository
import com.proyekakhir.mibu.user.ui.home.HomeViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val repository = FirebaseRepository()
        val factory = ViewModelFactory(repository)
        homeViewModel = ViewModelProvider(requireActivity(), factory).get(HomeViewModel::class.java)

        val username = FirebaseAuth.getInstance().currentUser?.displayName
        val email = FirebaseAuth.getInstance().currentUser?.email

        binding.username.text = username
        binding.email.text = email

        binding.itemLogout.setOnClickListener {
            logout()
        }

        binding.itemAbout.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_aboutFragment)
        }

        binding.itemDataPribadi.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_dataPribadiFragment2)
        }

        homeViewModel.fetchUserData()
        homeViewModel.userData.observe(viewLifecycleOwner, Observer { data ->
            if (!data?.profileImage.isNullOrEmpty()) {
                Glide.with(requireActivity())
                    .load(data?.profileImage)
                    .into(binding.profileImage)
            } else {
                binding.profileImage.setImageResource(R.drawable.cam_placeholder_logo)
            }
        })

        return root
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), BidanLoginActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


}