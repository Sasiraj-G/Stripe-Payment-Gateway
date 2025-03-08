package com.example.paymentgateway.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paymentgateway.models.LinkedInUser
import com.example.paymentgateway.repository.LinkedInRepository
import com.example.paymentgateway.utils.LinkedInConstants
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LinkedInViewModel : ViewModel() {

    private val repository = LinkedInRepository()

    private val _linkedInUser = MutableLiveData<LinkedInUser>()
    val linkedInUser: LiveData<LinkedInUser> = _linkedInUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getAuthorizationUrl(): String {
        val encodedRedirectUri = URLEncoder.encode(LinkedInConstants.REDIRECT_URI, StandardCharsets.UTF_8.toString())
        return "${LinkedInConstants.AUTHORIZATION_URL}?" +
                "response_type=code&" +
                "client_id=${LinkedInConstants.CLIENT_ID}&" +
                "redirect_uri=$encodedRedirectUri&" +
                "scope=${LinkedInConstants.SCOPE}&" +
                "state=${System.currentTimeMillis()}"
    }

    fun handleAuthorizationResponse(authorizationCode: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val accessToken = repository.getAccessToken(authorizationCode)
                val user = repository.getUserProfile(accessToken)
                _linkedInUser.postValue(user)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
}