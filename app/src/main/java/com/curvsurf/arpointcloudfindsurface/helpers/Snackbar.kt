package com.curvsurf.arpointcloudfindsurface.helpers

import android.app.Activity
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Snackbar(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {

    private val backgroundColor = Color(0xBF323232)

    private enum class DismissBehavior { HIDE, SHOW, FINISH }

    private var _maxLines: Int = 2
    private var lastMessage: String = ""

    fun isShowing(): Boolean = hostState.currentSnackbarData != null

    fun setMaxLines(lines: Int) { _maxLines = lines }

    fun isDurationIndefinite(): Boolean =
        hostState.currentSnackbarData?.visuals?.duration == SnackbarDuration.Indefinite

    fun showMessage(activity: Activity?, message: String) {
        if (message.isNotEmpty() && (!isShowing() || lastMessage != message)) {
            lastMessage = message
            showInternal(message, DismissBehavior.HIDE, SnackbarDuration.Indefinite, activity)
        }
    }

    fun showMessageWithDismiss(activity: Activity?, message: String) {
        showInternal(message, DismissBehavior.SHOW, SnackbarDuration.Indefinite, activity)
    }

    fun showMessageForShortDuration(activity: Activity?, message: String) {
        showInternal(message, DismissBehavior.SHOW, SnackbarDuration.Short, activity)
    }

    fun showMessageForLongDuration(activity: Activity?, message: String) {
        showInternal(message, DismissBehavior.SHOW, SnackbarDuration.Long, activity)
    }

    fun showError(activity: Activity?, errorMessage: String) {
        showInternal(errorMessage, DismissBehavior.FINISH, SnackbarDuration.Indefinite, activity)
    }

    fun hide(@Suppress("UNUSED_PARAMETER") activity: Activity?) {
        lastMessage = ""
        hostState.currentSnackbarData?.dismiss()
    }

    private fun showInternal(
        message: String,
        dismissBehavior: DismissBehavior,
        duration: SnackbarDuration,
        activity: Activity?
    ) {
        scope.launch {
            val actionLabel =
                if (dismissBehavior != DismissBehavior.HIDE && duration == SnackbarDuration.Indefinite)
                    "Dismiss" else null

            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = actionLabel != null,
                duration = duration
            )

            if (dismissBehavior == DismissBehavior.FINISH) {
                activity?.finish()
            }
        }
    }

    @Composable
    fun Host(modifier: Modifier = Modifier) {
        SnackbarHost(
            hostState = hostState,
            modifier = modifier
        ) { data ->
            Snackbar(
                containerColor = backgroundColor,
                action = {
                    data.visuals.actionLabel?.let { label ->
                        TextButton(onClick = { data.performAction() }) {
                            Text(label)
                        }
                    }
                }
            ) {
                // maxLines 적용
                Text(text = data.visuals.message, maxLines = _maxLines)
            }
        }
    }
}

@Composable
fun rememberSnackbarHelper(): Snackbar {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) { Snackbar(hostState, scope) }
}