package mhs.unisbank.prayertime

import android.content.ContentValues
import android.content.Context

object LocationDao {

    fun insert(
        context: Context,
        lat: Double,
        lon: Double,
        address: String?
    ) {
        val values = ContentValues().apply {
            put(LocationContract.LocationTable.COL_LAT, lat)
            put(LocationContract.LocationTable.COL_LON, lon)
            put(LocationContract.LocationTable.COL_ADDRESS, address)
            put(LocationContract.LocationTable.COL_SAVED_AT, System.currentTimeMillis())
        }

        AppDatabase(context).writableDatabase.use { db ->
            db.insert(LocationContract.LocationTable.NAME, null, values)
        }
    }

    fun getAll(context: Context): List<LocationModel> =
        AppDatabase(context).readableDatabase.use { db ->
            db.query(
                LocationContract.LocationTable.NAME,
                null, null, null, null, null,
                "${LocationContract.LocationTable.COL_SAVED_AT} DESC"
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(
                            LocationModel(
                                id = c.getLong(c.getColumnIndexOrThrow(LocationContract.LocationTable.COL_ID)),
                                lat = c.getDouble(c.getColumnIndexOrThrow(LocationContract.LocationTable.COL_LAT)),
                                lon = c.getDouble(c.getColumnIndexOrThrow(LocationContract.LocationTable.COL_LON)),
                                address = c.getString(c.getColumnIndexOrThrow(LocationContract.LocationTable.COL_ADDRESS)),
                                savedAt = c.getLong(c.getColumnIndexOrThrow(LocationContract.LocationTable.COL_SAVED_AT))
                            )
                        )
                    }
                }
            }
        }
}

/** Data holder */
data class LocationModel(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val address: String?,
    val savedAt: Long
)
