package mhs.unisbank.prayertime

import kotlin.math.*

object PrayerCalculator {

    private fun degToRad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun radToDeg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    private fun safeAcos(x: Double): Double {
        val clamped = x.coerceIn(-1.0, 1.0)
        return acos(clamped)
    }

    private fun moreLess360(angle: Double): Double {
        val mod = angle % 360.0
        return if (mod < 0) mod + 360.0 else mod
    }

    fun calcPrayerTimes(
        year: Int,
        month: Int,
        day: Int,
        longitude: Double,
        latitude: Double,
        timeZone: Int,
        fajrTwilight: Double,
        ishaTwilight: Double
    ): DoubleArray {

        val D = (367 * year) -
                ((year + (month + 9) / 12) * 7 / 4) +
                ((275 * month / 9) + day - 730531.5)

        val L = moreLess360(280.461 + 0.9856474 * D)
        val M = moreLess360(357.528 + 0.9856003 * D)
        val Lambda = moreLess360(L + 1.915 * sin(degToRad(M)) + 0.02 * sin(degToRad(2 * M)))
        val Obliquity = 23.439 - 0.0000004 * D

        var Alpha = radToDeg(atan(cos(degToRad(Obliquity)) * tan(degToRad(Lambda))))
        Alpha = moreLess360(Alpha)
        Alpha += 90 * ((floor(Lambda / 90.0) - floor(Alpha / 90.0)))

        val ST = 100.46 + 0.985647352 * D
        val Dec = radToDeg(asin(sin(degToRad(Obliquity)) * sin(degToRad(Lambda))))
        val Durinal_Arc = radToDeg(acos(
            (sin(degToRad(-0.8333)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                    (cos(degToRad(Dec)) * cos(degToRad(latitude)))
        ))

        val Noon = moreLess360(Alpha - ST)
        val UT_Noon = Noon - longitude
        val zuhrTime = UT_Noon / 15.0 + timeZone

        val angle = radToDeg(atan(1 / tan(abs(degToRad(latitude - Dec)))))
        val Asr_Arc = radToDeg(safeAcos(
            (sin(degToRad(90 - angle)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                    (cos(degToRad(Dec)) * cos(degToRad(latitude)))
        ))
        val asrTime = zuhrTime + (Asr_Arc / 15.0)

        val sunRiseTime = zuhrTime - (Durinal_Arc / 15.0)
        val maghribTime = zuhrTime + (Durinal_Arc / 15.0)

        val Esha_Arc = radToDeg(acos(
            (sin(degToRad(ishaTwilight)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                    (cos(degToRad(Dec)) * cos(degToRad(latitude)))
        ))
        val ishaTime = zuhrTime + (Esha_Arc / 15.0)

        val Fajr_Arc = radToDeg(acos(
            (sin(degToRad(fajrTwilight)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                    (cos(degToRad(Dec)) * cos(degToRad(latitude)))
        ))
        val fajrTime = zuhrTime - (Fajr_Arc / 15.0)

        return doubleArrayOf(fajrTime, sunRiseTime, zuhrTime, asrTime, maghribTime, ishaTime)
    }
}
