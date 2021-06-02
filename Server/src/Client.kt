import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import signals.Signal

class Client(private val adapter: ClientAdapter) : SignalAdapter {
    private var sio: SocketIO? = null
    private var busy: Boolean = false
    val locked: Boolean
        get() = busy
    private var task = false
    val tasked: Boolean
        get() = task
    companion object{
        private var count = 0
    }
    private var inputJob: Job? = null
    private var outputJob: Job? = null
    private val identifier: Int
    val id:Int
        get() = identifier
    init {
        count++
        identifier = count
    }

    fun task(){
        task = true
    }

    fun untask(){
        task = false
    }

    fun lock(){
        busy = true
    }

    fun unlock(){
        busy = false
    }

    suspend fun sendData(data: Signal){
        sio!!.sendData(data)
    }

    fun addSocketIO(sio: SocketIO){
        this.sio = sio
        startSocketIO()
    }

    private fun startSocketIO() = GlobalScope.launch {
        inputJob = launch { sio!!.startInput() }
        outputJob = launch { sio!!.startOutput() }
    }

    suspend fun stop(){
        inputJob?.cancelAndJoin()
        outputJob?.cancelAndJoin()
    }

    override suspend fun inputSignal(signal: Signal, sio: SocketIO) {
        when(signal.getType()){
            "NewMatrix" -> {
                adapter.inputClientData(signal, this)
            }
            "Addition" -> {
                adapter.inputClientData(signal, this)
            }
            "Multiplication" -> {
                adapter.inputClientData(signal, this)
            }
            "Answer" -> {
                adapter.inputClientData(signal, this)
            }
        }
    }
}