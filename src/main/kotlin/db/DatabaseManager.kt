package db

import db.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {

    fun init() {
        val dbHost = System.getenv("DB_HOST")?.takeIf { it.isNotBlank() } ?: "localhost"
        val dbPort = System.getenv("DB_PORT")?.toIntOrNull() ?: 5432
        val dbName = System.getenv("DB_NAME")?.takeIf { it.isNotBlank() } ?: "carlog"
        val dbUser = System.getenv("DB_USER")?.takeIf { it.isNotBlank() } ?: "postgres"
        val dbPassword = System.getenv("DB_PASSWORD")?.takeIf { it.isNotBlank() } ?: "postgres"
        val jdbcUrl = System.getenv("JDBC_URL")?.takeIf { it.isNotBlank() }
            ?: "jdbc:postgresql://$dbHost:$dbPort/$dbName"
        val driver = System.getenv("JDBC_DRIVER")?.takeIf { it.isNotBlank() } ?: "org.postgresql.Driver"

        try {
            Database.connect(jdbcUrl, driver = driver, user = dbUser, password = dbPassword)

            transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    UsersTable,
                    CarsTable
                    , WorkshopsTable
                    , MaintenancesTable
                    , InvoicesTable
                    , ExpenseItemsTable
                    , RemindersTable
                    , DriverProfilesTable
                    , CarSecondaryDriversTable
                    , DriverProfileLicensesTable
                )
            }
        } catch (e: Exception) {
            System.err.println("DatabaseManager: ERROR DENTRO DE init(): ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}