package repositories

import com.google.gson.JsonArray
import models.BaseModel
import models.MatrixModel
import utils.params.HyperParameter
import utils.params.MatrixId
import java.sql.SQLException

class Matrices : BaseRepository() {

    fun getAllMatrices() : JsonArray{
        val array = JsonArray()
        try {
            val conn = getConnection()
            conn.createStatement().apply {
                val rs = executeQuery("SELECT `id` FROM `matrix_db`.matrices")
                while(rs.next()){
                    array.add(rs.getString("id"))
                }
            }
        }catch (ignore: SQLException){

        }
        return array
    }

    fun getMatrix(matrixName: String) : MatrixModel? {
        val m = getEntry(MatrixId(matrixName))
        return m as MatrixModel?
    }

    operator fun get(matrixName: String) : MatrixModel?{
        val m = getEntry(MatrixId(matrixName))
        return m as MatrixModel?
    }

    fun addMatrix(matrixName: String, rows: Int, columns: Int){
        createEntry(MatrixModel(matrixName, rows, columns))
    }

    fun setMatrix(matrixName: String, rows: Int, columns: Int){
        updateEntry(MatrixModel(matrixName, rows, columns))
    }

    override fun createEntry(model: BaseModel) {
        val m = model as MatrixModel
        try{
            val conn = getConnection()
            conn.createStatement().apply {
                executeUpdate("INSERT INTO `matrix_db`.matrices " +
                        "VALUES ('${m.id}', '${m.rows}', '${m.columns}')")
            }
        }catch (ignore: SQLException){

        }
    }

    override fun getEntry(param: HyperParameter): BaseModel? {
        val p = param as MatrixId
        try{
            val conn = getConnection()
            conn.createStatement().apply {
                val rs = this.executeQuery("SELECT `id`, `matrix_rows`, `matrix_columns`" +
                        " FROM `matrix_db`.`matrices` WHERE `id` = ${p.id}")
                rs.first() // <- move to first entry, cause there is necessary data
                val id = rs.getString(1)
                val rows = rs.getInt(2)
                val columns = rs.getInt(3)
                return MatrixModel(id, rows, columns)
            }
        }catch (ex: SQLException){
            return null
        }
    }

    override fun updateEntry(model: BaseModel) {
        val m = model as MatrixModel
        try {
            val conn = getConnection()
            conn.createStatement().apply {
                executeUpdate("UPDATE `matrix_db`.matrices" +
                        " WHERE `id` = ${m.id} SET `matrix_rows`=${m.rows}")
                executeUpdate("UPDATE `matrix_db`.matrices " +
                        "WHERE `id` = ${m.id} SET `matrix_columns`=${m.columns}")
            }
        }catch (ignore: SQLException){

        }
    }
}