package com.waseefakhtar.doseapp.feature.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waseefakhtar.doseapp.analytics.AnalyticsHelper
import com.waseefakhtar.doseapp.domain.model.Medication
import com.waseefakhtar.doseapp.extension.toFormattedDateString
import com.waseefakhtar.doseapp.feature.home.usecase.GetMedicationsUseCase
import com.waseefakhtar.doseapp.feature.home.usecase.UpdateMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set


    private val _selectedDate = MutableStateFlow(Date())
    private val _medications = getMedicationsUseCase.getMedications()



    val homeUiState  = combine(_selectedDate,_medications)  { selectedDate , medications ->
        val filteredMedications = medications.filter {
            it.medicationTime.toFormattedDateString() == selectedDate.toFormattedDateString()
        }.sortedBy { it.medicationTime }

        HomeState(
            medications = filteredMedications
        )

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeState()
    )



    fun updateSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    init {
        loadMedications()
    }

    fun getUserName() {
        state = state.copy(userName = "Kathryn")
        // TODO: Get user name from DB
    }

    fun getGreeting() {
        state = state.copy(greeting = "Greeting")
        // TODO: Get greeting by checking system time
    }

    fun loadMedications() {
        viewModelScope.launch {
            getMedicationsUseCase.getMedications().onEach { medicationList ->
                state = state.copy(
                    medications = medicationList
                )
            }.launchIn(viewModelScope)
        }
    }

    fun takeMedication(medication: Medication) {
        viewModelScope.launch {
            updateMedicationUseCase.updateMedication(medication)
        }
    }

    fun getUserPlan() {
        // TODO: Get user plan
    }

    fun logEvent(eventName: String) {
        analyticsHelper.logEvent(eventName = eventName)
    }
}
