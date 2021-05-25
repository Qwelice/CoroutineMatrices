package repositories

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