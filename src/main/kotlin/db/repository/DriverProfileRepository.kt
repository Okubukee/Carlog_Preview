package db.repository

import db.tables.DriverProfilesTable
import db.tables.DriverProfileLicensesTable
import models.DriverProfile
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object DriverProfileRepository {

    private fun toDriverProfile(row: ResultRow): DriverProfile = DriverProfile(
        id = row[DriverProfilesTable.id],
        userId = row[DriverProfilesTable.userId],
        name = row[DriverProfilesTable.name],
        photoUrl = row[DriverProfilesTable.photoUrl],
        phone = row[DriverProfilesTable.phone],
        email = row[DriverProfilesTable.email],
        licenseTypes = loadLicenses(row[DriverProfilesTable.id])
    )

    private fun loadLicenses(driverProfileId: String): List<String> = transaction {
        DriverProfileLicensesTable.select { DriverProfileLicensesTable.driverProfileId eq driverProfileId }
            .map { it[DriverProfileLicensesTable.licenseType] }
    }

    fun createProfile(userId: String, name: String, photoUrl: String? = null, phone: String? = null, email: String? = null, licenseTypes: List<String> = emptyList()): DriverProfile {
        val id = UUID.randomUUID().toString()
        transaction {
            DriverProfilesTable.insert {
                it[DriverProfilesTable.id] = id
                it[DriverProfilesTable.userId] = userId
                it[DriverProfilesTable.name] = name
                it[DriverProfilesTable.photoUrl] = photoUrl
                it[DriverProfilesTable.phone] = phone
                it[DriverProfilesTable.email] = email
            }
            // insert license rows
            licenseTypes.forEach { lt ->
                DriverProfileLicensesTable.insertIgnore {
                    it[DriverProfileLicensesTable.driverProfileId] = id
                    it[DriverProfileLicensesTable.licenseType] = lt
                }
            }
        }
        return DriverProfile(id = id, userId = userId, name = name, photoUrl = photoUrl, phone = phone, email = email, licenseTypes = licenseTypes)
    }

    fun getProfilesByUser(userId: String): List<DriverProfile> {
        return transaction {
            DriverProfilesTable.select { DriverProfilesTable.userId eq userId }
                .map { toDriverProfile(it) }
        }
    }

    fun updateProfile(profile: DriverProfile) {
        transaction {
            DriverProfilesTable.update({ DriverProfilesTable.id eq profile.id }) {
                it[name] = profile.name
                it[photoUrl] = profile.photoUrl
                it[phone] = profile.phone
                it[email] = profile.email
            }
            // update licenses
            DriverProfileLicensesTable.deleteWhere { DriverProfileLicensesTable.driverProfileId eq profile.id }
            profile.licenseTypes.forEach { lt ->
                DriverProfileLicensesTable.insertIgnore {
                    it[DriverProfileLicensesTable.driverProfileId] = profile.id
                    it[DriverProfileLicensesTable.licenseType] = lt
                }
            }
        }
    }

    fun deleteProfile(profileId: String) {
        transaction {
            DriverProfilesTable.deleteWhere { DriverProfilesTable.id eq profileId }
        }
    }
}