package db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object ExpenseItemsTable : Table("expense_items") {
    val id = varchar("id", 255) 
    val carId = varchar("car_id", 255).references(CarsTable.id, onDelete = ReferenceOption.CASCADE) 
    val description = varchar("description", 1024)
    val date = varchar("expense_date", 20) 
    val amount = double("amount")
    val iconName = varchar("icon_name", 100) 

    override val primaryKey = PrimaryKey(id)
}