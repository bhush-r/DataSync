package com.bhushan.datasync.utils

/**
 * Generic wrapper used by every Repository -> ViewModel -> UI data flow.
 *
 * Every asynchronous operation (auth, Firestore read/write, device data
 * read, sync) emits [Resource.Loading] first, then either [Resource.Success]
 * or [Resource.Error]. UI layers collect a [kotlinx.coroutines.flow.StateFlow]
 * of this type and render the loading spinner / content / error view
 * accordingly, which is how requirement "Loading & Error handling on every
 * screen" is satisfied uniformly across the app.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()

    val isLoading get() = this is Loading

    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): Resource<T> {
        if (this is Error) action(message)
        return this
    }
}
