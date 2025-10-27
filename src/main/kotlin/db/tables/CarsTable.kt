package db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object CarsTable : Table("cars") {
    val id = varchar("id", 255)
    val userId = varchar("user_id", 255).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val driverProfileId = varchar("driver_profile_id", 255).references(DriverProfilesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val brand = varchar("brand", 255)
    val model = varchar("model", 255)
    val year = integer("year")
    val plate = varchar("plate", 50)
    val km = integer("km")
    val imageUrl = varchar("image_url", 512).nullable()
    val nextServiceDate = varchar("next_service_date", 20).nullable()
    val color = varchar("color", 50)
    val transmission = varchar("transmission", 50)
    val fuelType = varchar("fuel_type", 50)
    val purchaseDate = varchar("purchase_date", 20).nullable()
    val lastTiresCheckDate = varchar("last_tires_check_date", 20).nullable()
    val lastOilCheckDate = varchar("last_oil_check_date", 20).nullable()
    val lastBrakesCheckDate = varchar("last_brakes_check_date", 20).nullable()
    val lastLightsCheckDate = varchar("last_lights_check_date", 20).nullable()
    val lastWipersCheckDate = varchar("last_wipers_check_date", 20).nullable()

    override val primaryKey = PrimaryKey(id)
}