import java.io.Closeable

abstract class AsyncStream(private val stream: Closeable) {
    private var stop: Boolean = false
    val stopped: Boolean
        get() = stop

    protected fun interrupt(){
        stop = true
        stream.close()
    }
}