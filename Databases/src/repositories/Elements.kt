package repositories

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import models.BaseModel
import models.ElementModel
import utils.params.HyperParameter
import utils.params.Element
import java.sql.SQLException

class Elements : BaseRepository() {

    fun getElement(matrixName: String, i: Int, j: Int) : Double?{
        val m = getEntry(Element(matrixName, i, j))
        return if(m != null){
            (m as ElementModel).element
        }else{
            null
        }
    }

    fun addElement(matrixName: String, i: Int, j: Int, elem: Double){
        createEntry(ElementModel(matrixName, i, j, elem))
    }

    fun setElement(matrixName: String, i: Int, j: Int, elem: Double){
        updateEntry(ElementModel(matrixName, i, j, elem))
    }

    fun getByRow(matrixName: String, row: Int) : JsonObject{
        val jo = JsonObject()
        jo.addProperty("id", matrixName)
        jo.addProperty("row", row)
        jo.addProperty("columnsCount", getColumnsCount(matrixName, row))
        val data = JsonArray()
        for(j in 0 until jo["columnsCount"].asInt){
            getElement(matrixName, row, j)?.let { data.add(it) }
        }
        jo.add("data", data)
        return jo
    }

    fun getByColumn(matrixName: String, column: Int) : JsonObject{
        val jo = JsonObject()
        jo.addProperty("id", matrixName)
        jo.addProperty("column", column)
        jo.addProperty("rowsCount", getRowsCount(matrixName, column))
        val data = JsonArray()
        for(i in 0 until jo["rowsCount"].asInt){
            getElement(matrixName, i, column)?.let { data.add(it) }
        }
        jo.add("data", data)
        return jo
    }

    fun getColumnsCount(matrixName: String, byRow: Int) : Int?{
        try{
            val conn = getConnection()
            conn.createStatement().apply {
                val rs = executeQuery("SELECT COUNT(`j`) AS count FROM `matrix_db`.matrix_elements " +
                            "WHERE `matrix_id` = $matrixName AND `i` = $byRow")
                rs.first()
                return rs.getInt("count")
            }
        }catch (ex: SQLException){
            return null
        }
    }

    fun getRowsCount(matrixName: String, byColumn: Int) : Int?{
        try{
            val conn = getConnection()
            conn.createStatement().apply {
                val rs = executeQuery("SELECT COUNT(`i`) AS count FROM `matrix_db`.matrix_elements " +
                            "WHERE `matrix_id` = $matrixName AND `j` = $byColumn")
                rs.first()
                return rs.getInt("count")
            }
        }catch (ex: SQLException){
            return null
        }
    }

    override fun createEntry(model: BaseModel) {
        val m = model as ElementModel
        try{
            val conn = getConnection()
            conn.createStatement().apply {
                executeUpdate("INSERT INTO `matrix_db`.matrix_elements " +
                        "VALUES ('${m.matrixId}', '${m.i}', '${m.j}', '${m.element}')")
            }
        }catch (ex: SQLException){

        }
    }

    override fun getEntry(param: HyperParameter): BaseModel? {
        val p = param as Element
        try{
            val conn = getConnection()
            conn.createStatement().apply {
                val rs = executeQuery("SELECT `element_value` FROM `matrix_db`.matrix_elements " +
                        "WHERE matrix_id = ${p.id} AND i = ${p.row} AND j = ${p.column}")
                rs.first()
                val value = rs.getDouble("element_value")
                return ElementModel(p.id, p.row, p.column, value)
            }
        }catch (ex: SQLException){
            return null
        }
    }

    override fun updateEntry(model: BaseModel) {
        val m = model as ElementModel
        try {
            val conn = getConnection()
            conn.createStatement().apply {
                executeUpdate("UPDATE `matrix_db`.matrix_elements " +
                        "WHERE matrix_id = ${m.matrixId} AND i = ${m.i} AND j = ${m.j}" +
                        " SET element_value = ${m.element}")
            }
        }catch (ignore: SQLException){

        }
    }
}