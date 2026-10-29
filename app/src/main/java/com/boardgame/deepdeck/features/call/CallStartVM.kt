package com.boardgame.deepdeck.features.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.repository.CallRepository
import com.boardgame.deepdeck.data.repository.RoomProblem
import com.boardgame.deepdeck.data.repository.RoomResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ROOM_CODE_LENGTH = 6

data class CallStartUiState(
    val code: String = "",
    val creating: Boolean = false,
    val checking: Boolean = false,
) {
    val canJoin: Boolean get() = code.length == ROOM_CODE_LENGTH && !checking && !creating
}

sealed interface CallStartEffect {
    data class OpenRoom(val code: String, val host: Boolean) : CallStartEffect
    data class Problem(val problem: RoomProblem) : CallStartEffect
}

@HiltViewModel
class CallStartVM @Inject constructor(
    private val repository: CallRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallStartUiState())
    val uiState: StateFlow<CallStartUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CallStartEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<CallStartEffect> = _effects.asSharedFlow()

    fun onCodeChange(raw: String) {
        val code = raw.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }.take(ROOM_CODE_LENGTH)
        _uiState.update { it.copy(code = code) }
    }

    /** Pulls a code out of a pasted invite ("…Room code: K7PXQ2\ndeepdeck://call/K7PXQ2") or a bare code. */
    fun pasteCode(text: String?) {
        val code = text?.let { extractCode(it) } ?: return
        onCodeChange(code)
    }

    fun createRoom() {
        if (_uiState.value.creating) return
        _uiState.update { it.copy(creating = true) }
        viewModelScope.launch {
            val result = repository.createRoom()
            _uiState.update { it.copy(creating = false) }
            when (result) {
                is RoomResult.Ready -> _effects.emit(CallStartEffect.OpenRoom(result.code, host = true))
                is RoomResult.Failed -> _effects.emit(CallStartEffect.Problem(result.problem))
            }
        }
    }

    fun joinRoom() {
        val state = _uiState.value
        if (!state.canJoin) return
        _uiState.update { it.copy(checking = true) }
        viewModelScope.launch {
            val result = repository.checkRoom(state.code)
            _uiState.update { it.copy(checking = false) }
            when (result) {
                is RoomResult.Ready -> _effects.emit(CallStartEffect.OpenRoom(result.code, host = false))
                is RoomResult.Failed -> _effects.emit(CallStartEffect.Problem(result.problem))
            }
        }
    }

    companion object {
        private val LINK = Regex("""call/([A-Za-z0-9]{$ROOM_CODE_LENGTH})""")
        private val BARE = Regex("""\b([A-Z0-9]{$ROOM_CODE_LENGTH})\b""")

        fun extractCode(text: String): String? =
            LINK.find(text)?.groupValues?.get(1)?.uppercase()
                ?: BARE.find(text.trim().uppercase())?.groupValues?.get(1)
    }
}
