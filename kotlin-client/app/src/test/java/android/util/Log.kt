package android.util

object Log {
    @JvmStatic
    fun isLoggable(tag: String, level: Int): Boolean = true

    @JvmStatic
    fun v(tag: String, msg: String): Int {
        return 0
    }

    @JvmStatic
    fun d(tag: String, msg: String): Int {
        println("DEBUG: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun i(tag: String, msg: String): Int {
        println("INFO: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun w(tag: String, msg: String): Int {
        println("WARN: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun w(tag: String, msg: String, exception: Throwable): Int {
        println("WARN: $tag: $msg , $exception")
        return 0
    }

    @JvmStatic
    fun e(tag: String, msg: String): Int {
        println("ERROR: $tag: $msg")
        return 0
    }

    @JvmStatic
    fun e(tag: String, msg: String, exception: Throwable): Int {
        println("ERROR: $tag: $msg , $exception")
        return 0
    }
}