import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import services.MatrixService
import signals.Signal
import signals.SignalNewMatrix
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.Executors

class Server(port: Int) : SignalAdapter {
    private val services = HashMap<String, Any>()
    private val coroutines = HashMap<String, Job>()
    private val connections = Channel<SocketIO>()
    private val connected = HashMap<SocketIO, Pair<Job, Job>>()
    private val sSocket = ServerSocket(port)
    private var stop = false

    init {
        runBlocking {
            println("[SERVER_GLOBAL]: All is starting...")
            services["matrices"] = MatrixService()
            coroutines["accept"] = launch { startClientAcceptance() }
            coroutines["receive"] = startConnectionReceive()
            println("[SERVER_GLOBAL]: Started successfully")
        }
    }

    private suspend fun startClientAcceptance() = withContext(Executors.newSingleThreadExecutor().asCoroutineDispatcher()){
        println("[SERVER_ACCEPT]: Acceptance coroutine is starting...")
        while(!stop){
            try{
                println("[SERVER_ACCEPT]: Waiting for new connection...")
                val cSocket = sSocket.accept()
                val sio = SocketIO(this@Server, cSocket.getInputStream(), cSocket.getOutputStream())
                println("[SERVER_ACCEPT]: New connection is accepted!")
                connections.send(sio)
            }catch (ex: IOException){
                println("[SERVER_ACCEPT]: Something went wrong with new connection")
            }
        }
    }

    private suspend fun startConnectionReceive() = GlobalScope.launch(Dispatchers.IO){
        println("[SERVER_RECEIVE]: Receive coroutine is starting...")
        while(!stop){
            println("[SERVER_RECEIVE]: Waiting for new accepted connection...")
            val sio = connections.receive()
            println("[SERVER_RECEIVE]: New connection received!")
            val ji = launch { sio.startInput() }
            val jo = launch { sio.startOutput() }
            connected[sio] = Pair(ji, jo)
            sio.sendData((services["matrices"] as MatrixService).getMatrixList())
        }
    }

    suspend fun stop(){
        println("[SERVER_GLOBAL]: All is shutting down...")
        stop = true
        connected.values.forEach {
            it.apply {
                first.cancelAndJoin()
                second.cancelAndJoin()
            }
        }
        coroutines.values.forEach { it.cancelAndJoin() }
        println("[SERVER_GLOBAL]: Goodbye!")
    }

    /**
     * Handling input signals */
    override suspend fun inputSignal(signal: Signal, sio: SocketIO) {
        when(signal.getType()){
            "NewMatrix" -> {
                val ser = services["matrices"] as MatrixService
                ser.appendMatrix(signal as SignalNewMatrix)
                sio.sendData(ser.getMatrixList())
            }
        }
    }
}