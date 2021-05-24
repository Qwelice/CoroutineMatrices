package database

import java.util.*


class DataSource {
    private val data = HashMap<String, Any>()
    private val defaultUSERNAME = "root"
    private val defaultPASSWORD = "root"
    private val defaultHOST = "localhost"
    private val defaultPORT = 3306
    private val defaultDBNAME = "___testing_db__from__java_program___"
    init {
        data["username"] = defaultUSERNAME
        data["password"] = defaultPASSWORD
        data["host"] = defaultHOST
        data["port"] = defaultPORT
        data["dbname"] = defaultDBNAME
    }
    fun addUsername(username: String) {
        data.replace("username", Objects.requireNonNullElse(username, defaultUSERNAME))
    }
    fun addPassword(password: String) {
        data.replace("password", Objects.requireNonNullElse(password, defaultPASSWORD))
    }
    fun addHost(host: String) {
        data.replace("host", Objects.requireNonNullElse(host, defaultHOST))
    }
    fun addPort(port: Int) {
        data.replace("port", Objects.requireNonNullElse(port, defaultPORT))
    }
    fun addDatabaseName(name: String) {
        data.replace("dbname", Objects.requireNonNullElse(name, defaultDBNAME))
    }
    fun getUsername(): String? {
        return data["username"] as String?
    }
    fun getPassword(): String? {
        return data["password"] as String?
    }
    fun getHost(): String? {
        return data["host"] as String?
    }
    fun getPort(): Int {
        return data["port"] as Int
    }
    fun getDatabaseName(): String? {
        return data["dbname"] as String?
    }
}