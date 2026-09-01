package com.parentalcare.child.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parentalcare.child.pipeline.IncomingRequestHandler
import com.parentalcare.core.data.model.ScreenshotRequestDoc
import com.parentalcare.core.data.request.ScreenshotRequestRepository
import com.parentalcare.core.notifications.SafePayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val incomingRequestHandler: IncomingRequestHandler,
    private val requestRepository: ScreenshotRequestRepository
) : ViewModel() {

    val activeRequest: StateFlow<ScreenshotRequestDoc?> = incomingRequestHandler.active

    fun acceptRequest() {
        // Just clear from handler. Navigation logic will take over.
        incomingRequestHandler.handleCancelled(SafePayload(type = "CANCEL"))
    }

    fun cancelRequest() {
        viewModelScope.launch {
            activeRequest.value?.let { req ->
                requestRepository.updateStatus(req, "FAILED", "Child cancelled the request.")
                incomingRequestHandler.handleCancelled(SafePayload(type = "CANCEL"))
            }
        }
    }
}
