package db.tables 

import org.jetbrains.exposed.sql.Table

object WorkshopsTable : Table("workshops") {
    val id = varchar("id", 255) 
    val name = varchar("name", 255)
    val specialty = varchar("specialty", 255)
    val phone = varchar("phone", 50)
    val location = varchar("location", 512)
    val hourlyRate = double("hourly_rate")

    override val primaryKey = PrimaryKey(id)
}