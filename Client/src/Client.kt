import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import signals.*
import java.io.IOException
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths

class Client : SignalAdapter {
    private val names = Channel<String>()
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
                /*val mtx = JsonObject()
                mtx.addProperty("id", "newMatrix")
                mtx.addProperty("rows", 3)
                mtx.addProperty("columns", 3)
                val lst = String(Files.readAllBytes(Paths.get("client\\src\\testing.csv"))).split("\n").toMutableList()
                lst.removeIf { x -> x.isBlank() }
                val data = JsonArray()
                for(l in lst){
                    val n = l.split(";")
                    val ja = JsonArray()
                    for(k in n){
                        ja.add(k)
                    }
                    data.add(ja)
                }
                mtx.add("data", data)
                sio!!.sendData(SignalNewMatrix(mtx.toString()))*/
                val nms = names.receive()
                val s = nms.split(" ")
                sio!!.sendData(SignalMultiplication(s[0], s[1]))
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
            "Solve" -> {
                val s = signal as SignalSolve
                val vectors = JsonParser.parseString(s.vectors).asJsonObject
                when(vectors["operation"].asString){
                    "addition" -> {
                        val fs = vectors["first"].asJsonArray
                        val sc = vectors["second"].asJsonArray
                        val result = JsonArray()
                        for(i in 0 until fs.size()){
                            result.add(fs[i].asDouble + sc[i].asDouble)
                        }
                        sio.sendData(SignalAnswer(s.taskId, result.toString()))
                    }
                    "multiplication" -> {
                        val fs = vectors["first"].asJsonArray
                        val sc = vectors["second"].asJsonArray
                        val result = JsonArray()
                        var temp = 0.0
                        for(i in 0 until fs.size()){
                            temp += fs[i].asDouble * sc[i].asDouble
                        }
                        result.add(temp)
                        sio.sendData(SignalAnswer(s.taskId, result.toString()))
                    }
                }
            }
            "Ready" -> {
                println("Matrix is ready!")
            }
        }
    }

    fun appendMatrices(nms: String) = GlobalScope.launch {
        names.send(nms)
    }
}