package com.gamelauncher.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.data.preference.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val settingsPreferences: SettingsPreferences
) : ViewModel() {

    val onboardingCompleted: StateFlow<Boolean> = settingsPreferences.onboardingCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isDarkTheme: StateFlow<Boolean> = settingsPreferences.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsPreferences.setOnboardingCompleted()
        }
    }
}
