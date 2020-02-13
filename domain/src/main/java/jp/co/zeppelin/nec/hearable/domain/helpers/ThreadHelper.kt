package jp.co.zeppelin.nec.hearable.domain.helpers

object ThreadHelper {
    fun threadId(thread: Thread): String {
        return "thread $thread id: ${thread.id}, name: ${thread.name}"
    }
}
