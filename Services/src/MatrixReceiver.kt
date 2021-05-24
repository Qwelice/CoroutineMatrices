import database.Database
import exceptions.SuchNameIsUsedException
import kotlinx.coroutines.channels.Channel
import signals.Signal
import signals.SignalError
import signals.SignalMatrixRow
import signals.SignalNewMatrix
import java.io.IOException
import java.lang.NumberFormatException

class MatrixReceiver(private val db: Database){
    private val signals = Channel<Pair<Signal, SocketIO>>()

    suspend fun sendData(data: Signal, sio: SocketIO){
        signals.send(Pair(data, sio))
    }

    private suspend fun receiveData(){
        while(true){
            val signal = signals.receive()
            when(signal.first.getType()){
                "NewMatrix" -> {
                    val s = signal.first as SignalNewMatrix
                    try{
                        val count = db.executeQuery("SELECT COUNT(`id`) as COUNT FROM `matrices` WHERE `id` = '${s.matrixName}'")
                            ?.split("\n")?.get(0)?.toInt() ?: -1
                        when(count){
                            0 -> {
                                db.addBatch("INSERT INTO `matrices` VALUES ('${s.matrixName}', '${s.rows}', '${s.columns}')")
                                db.executeDump()
                            }
                            1 -> {
                                throw NumberFormatException()
                            }
                            else -> {
                                throw IOException()
                            }
                        }
                    }catch (ex: NumberFormatException){
                        signal.second.sendData(SignalError(Exception("Failed on adding matrix")))
                    }catch (ex: IOException){
                        signal.second.sendData(SignalError(SuchNameIsUsedException()))
                    }
                }
                "MatrixRow" -> {
                    val s = signal.first as SignalMatrixRow
                    try{
                        val rowsCount = db.executeQuery("SELECT `matrix_rows` FROM `matrices`" +
                                "WHERE `id` = '${s.matrixName}'")?.split("\n")?.get(0)?.toInt() ?: -1
                        val columnsCount = db.executeQuery("SELECT `matrix_columns` FROM `matrices`" +
                                "WHERE `id` = '${s.matrixName}'")?.split("\n")?.get(0)?.toInt() ?: -1
                        if(rowsCount > 0){
                            if(s.index < rowsCount){
                                var j = 0
                                s.row.split(";").forEach {
                                    if(j < columnsCount){
                                        db.addBatch("INSERT INTO `matrix_elements`" +
                                                " VALUES('${s.matrixName}', '${s.index}', '$j', '$it')")
                                    }
                                    j++
                                }
                            }
                        }
                    }catch (ex: NumberFormatException){
                        signal.second.sendData(SignalError(Exception("Failed on adding row")))
                    }
                }
            }
        }
    }
}