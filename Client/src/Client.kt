import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import signals.Signal
import signals.SignalMatrixList
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
        when(signal.getType()){
            "MatrixList" -> {
                val s = signal as SignalMatrixList
                val lst = JsonParser.parseString(s.list).asJsonArray
                lst.forEach {
                    println(it.asString)
                }
            }
        }
    }
}