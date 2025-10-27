package db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

// Join table to store secondary drivers assigned to a car (many-to-many-like)
object CarSecondaryDriversTable : Table("car_secondary_drivers") {
    val carId = varchar("car_id", 255).references(CarsTable.id, onDelete = ReferenceOption.CASCADE)
    val driverProfileId = varchar("driver_profile_id", 255).references(DriverProfilesTable.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(carId, driverProfileId)
}
