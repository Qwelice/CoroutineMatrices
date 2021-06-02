import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import services.MatrixService
import services.TaskService
import signals.*
import utils.tasks.*
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.Executors

class Server(port: Int) : ClientAdapter, TaskAdapter{
    private val services = HashMap<String, Any>()
    private val coroutines = HashMap<String, Job>()
    private val connected = ArrayList<Client>()
    private val sSocket = ServerSocket(port)
    private var stop = false

    init {
        runBlocking {
            println("[SERVER_GLOBAL]: All is starting...")
            services["matrices"] = MatrixService()
            services["tasks"] = TaskService(this@Server)
            coroutines["accept"] = launch { startClientAcceptance() }
            println("[SERVER_GLOBAL]: Started successfully")
        }
    }

    private suspend fun startClientAcceptance() = withContext(Executors.newSingleThreadExecutor().asCoroutineDispatcher()){
        println("[SERVER_ACCEPT]: Acceptance coroutine is starting...")
        val mutex = Mutex()
        while(!stop){
            try{
                println("[SERVER_ACCEPT]: Waiting for new connection...")
                val cSocket = sSocket.accept()
                val c = Client(this@Server).apply {
                    addSocketIO(SocketIO(this, cSocket.getInputStream(), cSocket.getOutputStream()))
                }
                println("[SERVER_ACCEPT]: New connection is accepted!")
                c.sendData((services["matrices"] as MatrixService).getMatrixList())
                mutex.withLock {
                    connected.add(c)
                }
                (services["tasks"] as TaskService).newClient()
            }catch (ex: IOException){
                println("[SERVER_ACCEPT]: Something went wrong with new connection")
            }
        }
    }

    suspend fun stop(){
        val mutex = Mutex()
        println("[SERVER_GLOBAL]: All is shutting down...")
        stop = true
        mutex.withLock {
            connected.forEach {
                it.stop()
            }
        }
        coroutines.values.forEach { it.cancelAndJoin() }
        println("[SERVER_GLOBAL]: Goodbye!")
    }

    /**
     * Handling input signals */
    override suspend fun inputClientData(data: Signal, client: Client) {
        val s = services["matrices"] as MatrixService
        when(data.getType()){
            "NewMatrix" -> {
                s.appendMatrix(data as SignalNewMatrix)
                client.sendData(s.getMatrixList())
            }
            "Addition" -> {
                if(!client.tasked){
                    client.task()
                    val d = data as SignalAddition
                    val name = "${d.first} + ${d.second}"
                    val fs = s.getMatrix(d.first)
                    val sc = s.getMatrix(d.second)
                    s.appendMatrix(SignalNewMatrix(JsonObject().apply {
                        addProperty("id", name)
                        addProperty("rows", fs!!.rows)
                        addProperty("columns", fs.columns)
                        add("data", JsonArray())
                    }.toString()))
                    val t = services["tasks"] as TaskService
                    t.appendTask(client.id, AdditionTask(JsonObject().apply {
                        addProperty("id", fs!!.id)
                        addProperty("rows", fs.rows)
                        addProperty("columns", fs.columns)
                    }.toString(), JsonObject().apply {
                        addProperty("id", sc!!.id)
                        addProperty("rows", sc.rows)
                        addProperty("columns", sc.columns)
                    }.toString()))
                }else{
                    client.sendData(SignalAlreadyTasked())
                }
            }
            "Multiplication" -> {
                if(!client.tasked){
                    client.task()
                    val d = data as SignalMultiplication
                    val name = "${d.first} * ${d.second}"
                    val fs = s.getMatrix(d.first)
                    val sc = s.getMatrix(d.second)
                    s.appendMatrix(SignalNewMatrix(JsonObject().apply {
                        addProperty("id", name)
                        addProperty("rows", fs!!.rows)
                        addProperty("columns", sc!!.columns)
                        add("data", JsonArray())
                    }.toString()))
                    val t = services["tasks"] as TaskService
                    t.appendTask(client.id, MultiplicationTask(JsonObject().apply {
                        addProperty("id", fs!!.id)
                        addProperty("rows", fs.rows)
                        addProperty("columns", fs.columns)
                    }.toString(), JsonObject().apply {
                        addProperty("id", sc!!.id)
                        addProperty("rows", sc.rows)
                        addProperty("columns", sc.columns)
                    }.toString()))
                }else{
                    client.sendData(SignalAlreadyTasked())
                }
            }
            "Answer" -> {
                client.unlock()
                val ans = data as SignalAnswer
                val t = services["tasks"] as TaskService
                t.appendAnswer(ans.taskId, JsonParser.parseString(ans.answer).asJsonArray)
            }
        }
    }

    override suspend fun inputPromotion(tp: TaskPromotion, taskId: Int) {
        tryPromote(tp, taskId)
    }

    override suspend fun inputAnswer(tp: TaskPromotion, answer: JsonArray) {
        tryAppendAnswer(tp, answer).await()
    }

    override suspend fun inputReady(taskId: Int) {
        tryInputReady(taskId).await()
    }

    private fun tryInputReady(taskId: Int) = GlobalScope.async {
        val mutex = Mutex()
        mutex.withLock {
            for(c in connected){
                if(c.id == taskId){
                    c.sendData(SignalReady())
                    c.untask()
                    break
                }
            }
        }
    }

    private fun tryAppendAnswer(tp: TaskPromotion, answer: JsonArray) = GlobalScope.async {
        val s = services["matrices"] as MatrixService
        when(tp.task.operation){
            Operation.ADDITION -> {
                val name = "${tp.task.firstId} + ${tp.task.secondId}"
                val i = (tp.task as AdditionTask).currRow
                val columns = tp.task.firstColumnsCount
                for(j in 0 until columns){
                    val elem = answer[j].asDouble
                    s.appendElement(name, i, j, elem)
                }
            }
            Operation.MULTIPLICATION -> {
                val name = "${tp.task.firstId} * ${tp.task.secondId}"
                val i = (tp.task as MultiplicationTask).currRow
                val j = (tp.task as MultiplicationTask).currColumn
                val elem = answer[0].asDouble
                s.appendElement(name, i, j, elem)
            }
            else -> {

            }
        }
    }

    private fun tryPromote(tp: TaskPromotion, taskId: Int) = GlobalScope.launch {
        val mutex = Mutex()
        mutex.withLock {
            if(connected.size == 1){
                connected[connected.size - 1].apply {
                    if(!locked){
                        lock()
                        sendData(SignalSolve(taskId, getData(tp.task).toString()))
                        tp.addQuery()
                    }
                }
            } else if(connected.size > 1){
                var count = 0
                for(c in connected){
                    if(!c.locked){
                        if(count < 2){
                            count++
                            c.lock()
                            c.sendData(SignalSolve(taskId, getData(tp.task).toString()))
                            tp.addQuery()
                            continue
                        }
                        break
                    }
                }
            }
        }
    }

    private fun getData(task: Task) : JsonObject{
        val s = services["matrices"] as MatrixService
        when(task.operation){
            Operation.ADDITION -> {
                val t = task as AdditionTask
                val first = s.getByRow(t.firstId, t.currRow)["data"].asJsonArray
                val second = s.getByRow(t.secondId, t.currRow)["data"].asJsonArray
                return JsonObject().apply {
                    add("first", first)
                    add("second", second)
                    addProperty("operation", "addition")
                }
            }
            Operation.MULTIPLICATION -> {
                val t = task as MultiplicationTask
                val first = s.getByRow(t.firstId, t.currRow)["data"].asJsonArray
                val second = s.getByColumn(t.secondId, t.currColumn)["data"].asJsonArray
                return JsonObject().apply {
                    add("first", first)
                    add("second", second)
                    addProperty("operation", "multiplication")
                }
            }
            else -> {
                return JsonObject().apply {
                    add("first", JsonArray())
                    add("second", JsonArray())
                    addProperty("operation", "nothing")
                }
            }
        }
    }
}