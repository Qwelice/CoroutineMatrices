package database

import com.ibatis.common.jdbc.ScriptRunner
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.StringBuilder
import java.sql.Connection
import java.sql.SQLException


abstract class Database {
    @Throws(SQLException::class)
    protected abstract fun getConnection(): Connection?
    private val isConnected: Boolean
        get() = connection != null && !connection!!.isClosed
    private var connection: Connection? = null
    private var dump: ArrayList<String>? = null
    init {
        connection = null
        dump = ArrayList()
    }

    /**
     * Метод, добавляющий sql-запрос в последовательность запросов
     * @param sql запрос
     */
    open fun addBatch(sql: String) {
        // добавление запроса в список
        dump!!.add(sql)
    }
    private fun clearDump() {
        dump!!.clear()
    }

    /**
     * Метод для отправки последовательности запросов на сервер к бд  */
    open fun executeDump() {
        try {
            if (!isConnected) {
                connection = getConnection()
            }
            val st = connection?.createStatement()
            for (s in dump!!) {
                st?.addBatch(s)
            }
            st?.executeBatch()
        } catch (ex: SQLException) {
            println("Failed execute batch")
        }
        clearDump()
    }

    /**
     * Метод для отправки запроса на сервер к бд с получением результата запроса
     * @param sql запрос
     * @return результат запроса
     */
    fun executeQuery(sql: String): String? {
        return try {
            if (!isConnected) {
                connection = getConnection()
            }
            val st = connection?.createStatement()
            val rs = st?.executeQuery(sql)
            val columnsCount = rs?.metaData?.columnCount ?: throw SQLException()
            val lst = ArrayList<String>()
            val sb = StringBuilder()
            while (rs.next()) {
                lst.clear()
                for (i in 1..columnsCount) {
                    lst.add(rs.getString(i))
                }
                sb.append(lst.joinToString(";")).append('\n')
            }
            sb.toString()
        } catch (ex: SQLException) {
            null
        }
    }
    /**
     * Метод запускающий sql-скрипт
     * @param url путь к sql-файлу со скриптом*/
    fun executeScript(url: String) {
        try {
            val sr = ScriptRunner(getConnection(), false, false)
            sr.runScript(BufferedReader(FileReader(url)))
        } catch (ex: SQLException) {
            println("Failed on script running")
        } catch (ex: IOException) {
            println("Failed on script running")
        }
    }
}