// Flowtune Music by abhiram79
// github.com/abhiram79

package com.github.kugou

import kotlin.coroutines.cancellation.CancellationException

internal fun <T> Result<T>.recoverIfCancelled(): Result<T>? {
    return when (exceptionOrNull()) {
        is CancellationException -> null
        else -> this
    }
}
