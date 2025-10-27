package db.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object DriverProfilesTable : Table("driver_profiles") {
    val id = varchar("id", 255)
    val userId = varchar("user_id", 255).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val photoUrl = varchar("photo_url", 512).nullable()
    val phone = varchar("phone", 50).nullable()
    val email = varchar("email", 255).nullable()
    val licenseType = varchar("license_type", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}