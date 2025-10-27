package db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object DriverProfileLicensesTable : Table("driver_profile_licenses") {
    val driverProfileId = varchar("driver_profile_id", 255).references(DriverProfilesTable.id, onDelete = ReferenceOption.CASCADE)
    val licenseType = varchar("license_type", 50)
    override val primaryKey = PrimaryKey(driverProfileId, licenseType)
}
