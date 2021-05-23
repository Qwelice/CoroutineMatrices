import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import signals.Signal
import java.io.IOException
import java.net.Socket

class Client : SignalAdapter {
    private var cSocket: Socket? = null
    private var sio: SocketIO? = null
    private var stop = false

    /**
     * Trying to connect to the server */
    fun connect(host: String = "localhost", port: Int = 5803) : Boolean{
        println("[CLIENT]: Trying to connect...")
        return try{
            cSocket = Socket(host, port)
            sio = SocketIO(this, cSocket!!.getInputStream(), cSocket!!.getOutputStream())
            println("[CLIENT]: Successful connection")
            true
        }catch (ex: IOException){
            println("[CLIENT]: Failed on connection")
            false
        }
    }

    /**
     * Starting client conversation with server.
     * Please invoke that method only after successful connect method's result */
    suspend fun startClient() = GlobalScope.launch {
        launch { sio?.startInput() }
        launch { sio?.startOutput() }
    }

    override suspend fun inputSignal(signal: Signal, sio: SocketIO) {

    }
}