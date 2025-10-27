package db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object InvoicesTable : Table("invoices") {
    val id = varchar("id", 255) 
    val maintenanceId = varchar("maintenance_id", 255).references(MaintenancesTable.id, onDelete = ReferenceOption.CASCADE) 
    val date = varchar("invoice_date", 20) 
    val total = double("total")
    val status = varchar("status", 50) 

    override val primaryKey = PrimaryKey(id)
}