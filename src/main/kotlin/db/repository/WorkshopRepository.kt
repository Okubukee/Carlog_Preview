package db.repository

import db.tables.WorkshopsTable
import models.Workshop
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object WorkshopRepository {
    private fun toWorkshop(row: ResultRow): Workshop = Workshop(
        id = row[WorkshopsTable.id],
        name = row[WorkshopsTable.name],
        specialty = row[WorkshopsTable.specialty],
        phone = row[WorkshopsTable.phone],
        location = row[WorkshopsTable.location],
        hourlyRate = row[WorkshopsTable.hourlyRate]
    )

    fun getAllWorkshops(): List<Workshop> {
        return transaction {
            WorkshopsTable.selectAll().map { toWorkshop(it) }
        }
    }
}