package repositories

import com.google.gson.JsonArray
import models.BaseModel
import models.MatrixModel
import utils.params.HyperParameter
import utils.params.MatrixId
import java.sql.Connection
import java.sql.SQLException

class Matrices : BaseRepository() {

    private var conn: Connection? = null

    fun getAllMatrices() : JsonArray{
        val array = JsonArray()
        try {
            if(conn == null){
                conn = getConnection()
            }
            conn!!.createStatement().apply {
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
            if(conn == null){
                conn = getConnection()
            }
            conn!!.createStatement().apply {
                executeUpdate("INSERT INTO `matrix_db`.matrices " +
                        "VALUES ('${m.id}', '${m.rows}', '${m.columns}')")
                close()
            }
        }catch (ignore: SQLException){

        }
    }

    override fun getEntry(param: HyperParameter): BaseModel? {
        val p = param as MatrixId
        try{
            if(conn == null){
                conn = getConnection()
            }
            conn!!.createStatement().apply {
                val rs = this.executeQuery("SELECT `id`, `matrix_rows`, `matrix_columns`" +
                        " FROM `matrix_db`.`matrices` WHERE `id` = '${p.id}'")
                rs.next()
                val id = rs.getString(1)
                val rows = rs.getInt(2)
                val columns = rs.getInt(3)
                return MatrixModel(id, rows, columns).also { this.close() }
            }
        }catch (ex: SQLException){
            return null
        }
    }

    override fun updateEntry(model: BaseModel) {
        val m = model as MatrixModel
        try {
            if(conn == null){
                conn = getConnection()
            }
            conn!!.createStatement().apply {
                executeUpdate("UPDATE `matrix_db`.matrices" +
                        " WHERE `id` = ${m.id} SET `matrix_rows`=${m.rows}")
                executeUpdate("UPDATE `matrix_db`.matrices " +
                        "WHERE `id` = ${m.id} SET `matrix_columns`=${m.columns}")
                close()
            }
        }catch (ignore: SQLException){

        }
    }
}