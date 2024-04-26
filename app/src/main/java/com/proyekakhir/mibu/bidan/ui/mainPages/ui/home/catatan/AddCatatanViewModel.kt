package com.proyekakhir.mibu.bidan.ui.mainPages.ui.home.catatan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.proyekakhir.mibu.bidan.ui.firebase.FirebaseRepository
import com.proyekakhir.mibu.bidan.ui.mainPages.ui.home.catatan.model.AddDataAnak
import com.proyekakhir.mibu.bidan.ui.mainPages.ui.home.catatan.model.AddKesehatanKehamilanData
import com.proyekakhir.mibu.bidan.ui.mainPages.ui.home.catatan.model.AddNifasData

class AddCatatanViewModel(val repository: FirebaseRepository) : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()
    val successMessage = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()
    fun uploadCatatanKesehatan(uid: String, formData: AddKesehatanKehamilanData) {
        isLoading.value = true
        repository.uploadCatatanKesehatan(uid, formData, { success ->
            if (success) {
                // Handle success case
                successMessage.value = "Form uploaded successfully"
                isLoading.value = false
            } else {
                // Handle failure case
                isLoading.value = false
                errorMessage.value = "Failed to upload form"
            }
        }, { exception ->
            // Handle exception
            errorMessage.value = exception.message ?: "An error occurred"
        })
    }

    fun uploadCatatanNifas(uid: String, formData: AddNifasData) {
        isLoading.value = true
        repository.uploadCatatanNifas(uid, formData, { success ->
            if (success) {
                // Handle success case
                successMessage.value = "Form uploaded successfully"
                isLoading.value = false
            } else {
                // Handle failure case
                isLoading.value = false
                errorMessage.value = "Failed to upload form"
            }
        }, { exception ->
            // Handle exception
            errorMessage.value = exception.message ?: "An error occurred"
        })
    }

    fun uploadCatatanAnak(uid: String, formData: AddDataAnak) {
        isLoading.value = true
        repository.uploadDataAnak(uid, formData, { success ->
            if (success) {
                // Handle success case
                successMessage.value = "Form uploaded successfully"
                isLoading.value = false
            } else {
                // Handle failure case
                isLoading.value = false
                errorMessage.value = "Failed to upload form"
            }
        }, { exception ->
            // Handle exception
            errorMessage.value = exception.message ?: "An error occurred"
        })
    }
}