package be.buithg.etghaifgte.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.buithg.etghaifgte.domain.usecase.GetCurrentMatchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MatchScheduleViewModel @Inject constructor(
    private val getCurrentMatchesUseCase: GetCurrentMatchesUseCase
) : ViewModel() {

    private val _matches = MutableLiveData<List<Data>>(emptyList())
    val matches: LiveData<List<Data>> = _matches

    fun loadMatches(apiKey: String) {
        viewModelScope.launch {
            runCatching { getCurrentMatchesUseCase(apiKey) }
                .onSuccess { _matches.value = it }
                .onFailure { _matches.value = emptyList() }
        }
    }
}

