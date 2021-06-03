package services

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import repositories.Elements
import repositories.Matrices
import signals.SignalMatrixList
import signals.SignalNewMatrix

class MatrixService {
    private val elements = Elements()
    private val matrices = Matrices()

    fun appendMatrix(signal: SignalNewMatrix) = GlobalScope.async{
        val s = JsonParser.parseString(signal.fullMatrix).asJsonObject
        val ms = matrices.getAllMatrices()
        if(!ms.contains(s["id"])){
            val name = s["id"].asString
            val rows = s["rows"].asInt
            val columns = s["columns"].asInt
            val data = s["data"].asJsonArray
            matrices.addMatrix(name, rows, columns)
            if(data.size() > 0){
                for(i in 0 until rows){
                    for(j in 0 until columns){
                        val elem = data[i].asJsonArray[j].asDouble
                        elements.addElement(name, i, j, elem)
                    }
                }
            }
        }
    }

    fun getMatrixList() : SignalMatrixList = SignalMatrixList(matrices.getAllMatrices().toString())
    fun getByRow(matrixName: String, row: Int) = elements.getByRow(matrixName, row)
    fun getByColumn(matrixName: String, column: Int) = elements.getByColumn(matrixName, column)
    fun getMatrix(matrixName: String) = matrices.getMatrix(matrixName)
    fun appendElement(matrixName: String, i: Int, j: Int, value: Double){
        elements.addElement(matrixName, i, j, value)
    }
}