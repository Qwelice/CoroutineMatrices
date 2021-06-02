import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import signals.*
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
}