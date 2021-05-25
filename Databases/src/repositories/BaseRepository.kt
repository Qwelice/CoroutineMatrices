package repositories

import models.BaseModel
import utils.params.HyperParameter
import java.sql.Connection
import java.sql.DriverManager

abstract class BaseRepository {
    protected fun getConnection() : Connection{
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/matrix_db?serverTimezone=UTC", "root", "root")
    }
    protected abstract fun createEntry(model: BaseModel)
    protected abstract fun getEntry(param: HyperParameter) : BaseModel?
    protected abstract fun updateEntry(model: BaseModel)
}