package mhs.unisbank.prayertime

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(ctx: Context) :
    SQLiteOpenHelper(ctx, LocationContract.DB_NAME, null, LocationContract.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(LocationContract.LocationTable.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL(LocationContract.LocationTable.DROP_TABLE)
        onCreate(db)
    }
}
