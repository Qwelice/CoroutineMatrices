import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import signals.Signal
import java.io.IOException
import java.net.Socket

class Client : SignalAdapter {
    private var cSocket: Socket? = null
    private var sio: SocketIO? = null
    private var stop = false

    /**
     * Trying to connect to the server */
    fun connect(host: String = "localhost", port: Int = 5803) {
        println("[CLIENT]: Trying to connect...")
        try{
            cSocket = Socket(host, port)
            sio = SocketIO(this, cSocket!!.getInputStream(), cSocket!!.getOutputStream())
            println("[CLIENT]: Successful connection")
            runBlocking {
                println("[CLIENT]: I'm starting my work...")
                launch { sio?.startInput() }
                launch { sio?.startOutput() }
                println("[CLIENT]: Started successfully!")
            }
            println("[CLIENT]: That`s all")
        }catch (ex: IOException){
            println("[CLIENT]: Failed on connection")
        }
    }

    override suspend fun inputSignal(signal: Signal, sio: SocketIO) {

    }
}