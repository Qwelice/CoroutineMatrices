package database

import java.sql.Connection
import java.sql.DriverManager

import java.sql.SQLException

class MySQL() : Database() {
    private var source: DataSource? = null

    /**
     * Геттер url подключения к серверу (а также к бд)
     * @param exists существование бд на сервере: true - бд на сервере существует,
     * false - бд на сервере не существует
     */
    private fun getUrl(exists: Boolean): String? {
        return if (exists) {
            // если бд на сервере существует, возвращаем url подключения к бд на данном сервере
            "jdbc:mysql://" + source!!.getHost() + ':' + source!!.getPort() + '/' +
                    source!!.getDatabaseName() + "?serverTimezone=UTC"
        } else {
            // если бд не существует, возвращаем url подключения к серверу
            "jdbc:mysql://" + source!!.getHost() + ':' + source!!.getPort() + "/?serverTimezone=UTC"
        }
    }

    /** Конструктор по умолчанию  */
    init {
        // инициализируем стандартные конфиги
        source = DataSource()
    }

    /**
     * Конструктор с известным названием бд
     * @param dbname название бд
     */
    constructor(dbname: String) : this() {
        // добавляем имя бд в конфиги, тем самым изменяя их
        source!!.addDatabaseName(dbname)
    }

    /**
     * Конструктор с известными именем и паролем пользователя
     * @param username имя пользователя
     * @param password пароль пользователя
     */
    constructor(username: String, password: String) : this() {
        // добавляем имя и пароль пользователя в конфиги, тем самым изменяя их
        source!!.addUsername(username)
        source!!.addPassword(password)
    }

    /**
     * Конструктор с известными адресом сервера и порта
     * @param host адрес сервера
     * @param port порт
     */
    constructor(host: String, port: Int) : this() {
        // добавляем адрес сервера и порт в конфиги, тем самым изменяя их
        source!!.addHost(host)
        source!!.addPort(port)
    }

    /**
     * Конструктор с известными именем и паролем пользователя,
     * а также названием бд
     * @param username имя пользователя
     * @param password пароль пользователя
     * @param dbname название бд
     */
    constructor(username: String, password: String, dbname: String) : this() {
        // добавление параметров в конфиги, тем самым изменяя их
        source!!.addUsername(username)
        source!!.addPassword(password)
        source!!.addDatabaseName(dbname)
    }

    /**
     * Конструктор с известными адресом сервера и портом,
     * а также навзанием бд
     * @param host адрес сервера
     * @param port порт
     * @param dbname название бд
     */
    constructor(host: String, port: Int, dbname: String) : this() {
        // добавление параметров в конфиги, тем самым изменяя их
        source!!.addHost(host)
        source!!.addPort(port)
        source!!.addDatabaseName(dbname)
    }

    /**
     * Конструктор с известными адресом сервера и портом,
     * а также именем и паролем пользователя
     * @param host адрес сервера
     * @param port порт
     * @param username имя пользователя
     * @param password пароль пользователя
     */
    constructor(host: String, port: Int, username: String, password: String) : this() {
        // добавление параметров в конфиги, тем самым изменяя их
        source!!.addHost(host)
        source!!.addPort(port)
        source!!.addUsername(username)
        source!!.addPassword(password)
    }

    /**
     * Конструктор с известными адресом сервера и портом,
     * а также именем и паролем пользователя и названием бд
     * @param host адрес сервера
     * @param port порт
     * @param username имя пользователя
     * @param password пароль пользователя
     * @param dbname название бд
     */
    constructor(host: String, port: Int, username: String, password: String, dbname: String) : this() {
        source!!.addHost(host)
        source!!.addPort(port)
        source!!.addUsername(username)
        source!!.addPassword(password)
        source!!.addDatabaseName(dbname)
    }

    /**
     * Переопределенный метод класса [Database] для получения подключения к серверу (а также к бд) */
    @Throws(SQLException::class)
    override fun getConnection(): Connection {
        // изначально просто подключаемся к серверу
        val testing = DriverManager.getConnection(getUrl(false), source!!.getUsername(), source!!.getPassword())
        // отдельным запросом создаем на сервере необходимое бд, если его не существует
        testing.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + source!!.getDatabaseName())
        // возвращаем соединение с бд на данном сервере
        return DriverManager.getConnection(getUrl(true), source!!.getUsername(), source!!.getPassword())
    }
}