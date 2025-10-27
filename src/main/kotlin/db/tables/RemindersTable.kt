
package db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object RemindersTable : Table("reminders") {
    val id = varchar("id", 255) 
    val carId = varchar("car_id", 255).references(CarsTable.id, onDelete = ReferenceOption.CASCADE) 
    val title = varchar("title", 255)
    val subtitle = varchar("subtitle", 512)

    override val primaryKey = PrimaryKey(id)
}
