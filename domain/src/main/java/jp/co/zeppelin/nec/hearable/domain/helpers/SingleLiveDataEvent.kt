package jp.co.zeppelin.nec.hearable.domain.helpers

/**
 * Prevent "stale" liveData from being re-consumed by active observer (e.g. re-navigating to
 * detached but not destroyed fragment, which may trigger unwanted navigation event)
 *
 * Ref: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
class SingleLiveDataEvent<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
