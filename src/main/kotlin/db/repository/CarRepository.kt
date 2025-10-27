package db.repository

import models.Car
import db.tables.CarsTable
import db.tables.CarSecondaryDriversTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CarRepository {

    private fun toCar(row: ResultRow): Car = Car(
        id = row[CarsTable.id],
        brand = row[CarsTable.brand],
        model = row[CarsTable.model],
        year = row[CarsTable.year],
        plate = row[CarsTable.plate],
        km = row[CarsTable.km],
        imageUrl = row[CarsTable.imageUrl],
        nextServiceDate = row[CarsTable.nextServiceDate],
        color = row[CarsTable.color],
        transmission = row[CarsTable.transmission],
        fuelType = row[CarsTable.fuelType],
        purchaseDate = row[CarsTable.purchaseDate],
        driverProfileId = row[CarsTable.driverProfileId],
        lastWipersCheckDate = row[CarsTable.lastWipersCheckDate]
    )

    private fun loadSecondaryDrivers(carId: String): List<String> = transaction {
        CarSecondaryDriversTable.select { CarSecondaryDriversTable.carId eq carId }
            .map { it[CarSecondaryDriversTable.driverProfileId] }
    }

    fun addCar(car: Car, userId: String) {
        transaction {
            CarsTable.insert {
                it[id] = car.id
                it[CarsTable.userId] = userId
                it[CarsTable.driverProfileId] = car.driverProfileId
                it[brand] = car.brand
                it[model] = car.model
                it[year] = car.year
                it[plate] = car.plate
                it[km] = car.km
                it[imageUrl] = car.imageUrl
                it[nextServiceDate] = car.nextServiceDate
                it[color] = car.color
                it[transmission] = car.transmission
                it[fuelType] = car.fuelType
                it[purchaseDate] = car.purchaseDate
                it[lastTiresCheckDate] = car.lastTiresCheckDate
                it[lastOilCheckDate] = car.lastOilCheckDate
                it[lastBrakesCheckDate] = car.lastBrakesCheckDate
                it[lastLightsCheckDate] = car.lastLightsCheckDate
                it[lastWipersCheckDate] = car.lastWipersCheckDate
            }

            // Insert secondary drivers
            car.secondaryDriverIds.forEach { drvId ->
                CarSecondaryDriversTable.insertIgnore {
                    it[CarSecondaryDriversTable.carId] = car.id
                    it[CarSecondaryDriversTable.driverProfileId] = drvId
                }
            }
        }
    }

    fun updateCar(car: Car) {
        transaction {
            CarsTable.update({ CarsTable.id eq car.id }) {
                it[brand] = car.brand
                it[model] = car.model
                it[year] = car.year
                it[plate] = car.plate
                it[km] = car.km
                it[imageUrl] = car.imageUrl
                it[nextServiceDate] = car.nextServiceDate
                it[color] = car.color
                it[transmission] = car.transmission
                it[fuelType] = car.fuelType
                it[purchaseDate] = car.purchaseDate
                it[CarsTable.driverProfileId] = car.driverProfileId
                it[lastTiresCheckDate] = car.lastTiresCheckDate
                it[lastOilCheckDate] = car.lastOilCheckDate
                it[lastBrakesCheckDate] = car.lastBrakesCheckDate
                it[lastLightsCheckDate] = car.lastLightsCheckDate
                it[lastWipersCheckDate] = car.lastWipersCheckDate
            }

            // Update secondary drivers
            CarSecondaryDriversTable.deleteWhere { CarSecondaryDriversTable.carId eq car.id }
            car.secondaryDriverIds.forEach { drvId ->
                CarSecondaryDriversTable.insertIgnore {
                    it[CarSecondaryDriversTable.carId] = car.id
                    it[CarSecondaryDriversTable.driverProfileId] = drvId
                }
            }
        }
    }

    fun deleteCar(carId: String) {
        transaction {
            CarsTable.deleteWhere { CarsTable.id eq carId }
        }
    }

    fun getAllCars(userId: String): List<Car> {
        return transaction {
            CarsTable.select { CarsTable.userId eq userId }
                .map { row ->
                    val base = toCar(row)
                    val secondaries = loadSecondaryDrivers(base.id)
                    base.copy(secondaryDriverIds = secondaries)
                }
        }
    }

    fun getAllCarsByProfile(driverProfileId: String): List<Car> {
        return transaction {
            CarsTable.select { CarsTable.driverProfileId eq driverProfileId }
                .map { toCar(it) }
        }
    }

    fun getCarById(carId: String): Car? {
        return transaction {
            CarsTable.select { CarsTable.id eq carId }
                .mapNotNull { row ->
                    val base = toCar(row)
                    val secondaries = loadSecondaryDrivers(base.id)
                    base.copy(secondaryDriverIds = secondaries)
                }
                .singleOrNull()
        }
    }
}