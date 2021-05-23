import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import signals.Signal
import java.io.*
import java.util.concurrent.Executors

/**
 * Input/Output communicator */
class SocketIO (private val adapter: SignalAdapter, input: InputStream, output: OutputStream){
    private val output = AsyncOutput(output)
    private val input = AsyncInput(input)
    private val signals = Channel<Signal>()

    suspend fun sendData(data: Signal){
        signals.send(data)
    }

    suspend fun startInput(){
        input.start()
    }

    suspend fun startOutput(){
        output.start()
    }


    private inner class AsyncInput(inputStream: InputStream) : AsyncStream(inputStream){
        private val stream = try{
            ObjectInputStream(inputStream)
        }catch (ex: IOException){
            null
        }

        suspend fun start() = withContext(Executors.newSingleThreadExecutor().asCoroutineDispatcher()){
            while (!stopped){
                try{
                    val signal = stream?.let { Signal.fromBytesStream(it) }
                    if(signal != null){
                        adapter.inputSignal(signal, this@SocketIO)
                    }else{
                        throw IOException("Signal cannot be null")
                    }
                }catch (ex: IOException){
                    println(ex.message)
                    break
                }
            }
        }

        fun stop(){
            super.interrupt()
        }
    }

    private inner class AsyncOutput(outputStream: OutputStream) : AsyncStream(outputStream){
        private val stream = try{
            ObjectOutputStream(outputStream)
        }catch (ex: IOException){
            null
        }

        suspend fun start() = withContext(Executors.newSingleThreadExecutor().asCoroutineDispatcher()){
            while(!stopped){
                try{
                    val signal = signals.receive()
                    if (stream != null) {
                        signal.toBytesStream(stream)
                    }else{
                        throw IOException("Output stream cannot be null")
                    }
                }catch (ex: IOException){
                    println(ex.message)
                    break
                }
            }
        }

        fun stop(){
            super.interrupt()
        }
    }
}