package mhs.unisbank.prayertime

object LocationContract {
    const val DB_NAME = "app_data.db"
    const val DB_VERSION = 1

    object LocationTable {
        const val NAME         = "locations"
        const val COL_ID       = "_id"
        const val COL_LAT      = "latitude"
        const val COL_LON      = "longitude"
        const val COL_ADDRESS  = "address"
        const val COL_SAVED_AT = "saved_at"   // timestamp

        /** create & drop */
        const val CREATE_TABLE = """
            CREATE TABLE $NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LAT REAL NOT NULL,
                $COL_LON REAL NOT NULL,
                $COL_ADDRESS TEXT,
                $COL_SAVED_AT INTEGER NOT NULL
            );
        """
        const val DROP_TABLE = "DROP TABLE IF EXISTS $NAME"
    }
}
