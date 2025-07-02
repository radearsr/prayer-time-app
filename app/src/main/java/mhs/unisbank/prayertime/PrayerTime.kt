package mhs.unisbank.prayertime

data class PrayerTime(
    val emoji: String,
    val name: String,
    val time: String,
    val isNext: Boolean = false
)
