package mhs.unisbank.prayertime

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    // INIT COMPONENT
    private lateinit var tvTitle: TextView
    private lateinit var ivLocation: ImageView
    private lateinit var tvLocation: TextView
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvHijriahDate: TextView
    private lateinit var ivLiveTime: ImageView
    private lateinit var tvCurrentTime: TextView
    private lateinit var rvPrayer: RecyclerView
    private lateinit var tvNextPrayerName: TextView
    private lateinit var tvNextPrayerCountdown: TextView
    private lateinit var tvQsQuotes: TextView
    private lateinit var tvQsSource: TextView
    private lateinit var themeButton: Button
    private lateinit var mainLayout: ConstraintLayout

    // INIT TIME
    private lateinit var timeHandler: Handler
    private lateinit var timeRunnable: Runnable

    private var isDarkMode = false // flag toggle tema

    private lateinit var todayPrayerTimes: List<PrayerTime>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupView()
        setupCurrentDate()
        startLiveClock()
        setupPrayTimeRecyclerView()
    }

    private fun setupCurrentDate() {
        val sdfDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        sdfDate.timeZone = TimeZone.getTimeZone("Asia/Jakarta") // supaya tanggal sesuai WIB
        val todayDate = sdfDate.format(Date())
        tvCurrentDate.text = todayDate
    }

    private fun setupView() {
        tvTitle = findViewById(R.id.tv_title)
        ivLocation = findViewById(R.id.iv_location)
        tvLocation = findViewById(R.id.tv_location)
        tvCurrentDate = findViewById(R.id.tv_current_date)
        tvHijriahDate = findViewById(R.id.tv_hijriah_date)
        ivLiveTime = findViewById(R.id.iv_live_time)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        rvPrayer = findViewById(R.id.rv_prayer)
        tvNextPrayerName = findViewById(R.id.tv_next_prayer_name)
        tvNextPrayerCountdown = findViewById(R.id.tv_next_prayer_countdown)
        tvQsQuotes = findViewById(R.id.tv_qs_quotes)
        tvQsSource = findViewById(R.id.tv_qs_source)
        themeButton = findViewById(R.id.themeButton)
        mainLayout = findViewById(R.id.mainLayout)

        themeButton.setOnClickListener {
            if (isDarkMode) {
                // Mode terang
                mainLayout.setBackgroundResource(R.drawable.gradient_background_light)
                themeButton.text = "🌙 Mode Malam"
            } else {
                // Mode gelap
                mainLayout.setBackgroundResource(R.drawable.gradient_background_dark)
                themeButton.text = "☀️ Mode Terang"
            }
            isDarkMode = !isDarkMode
        }
    }

    private fun startLiveClock() {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("id-ID"))
        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta") // WIB

        timeHandler = Handler(Looper.getMainLooper())
        timeRunnable = object : Runnable {
            override fun run() {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
                val currentTime = sdf.format(calendar.time)
                Log.d("MainActivity", "Current Time: $currentTime")
                tvCurrentTime.text = currentTime
                val nextPrayer = getNextPrayer()
                tvNextPrayerName.text = nextPrayer?.name
                tvNextPrayerCountdown.text = getCountdownText(nextPrayer?.time ?: "00:00")
                timeHandler.postDelayed(this, 1000)
            }
        }
        timeHandler.post(timeRunnable)
    }


    override fun onDestroy() {
        super.onDestroy()
        timeHandler.removeCallbacks(timeRunnable)
    }

    private fun setupPrayTimeRecyclerView() {
        val prayerTimes = getPrayerTimesForToday()
        val adapter = PrayerAdapter(prayerTimes)
        rvPrayer.addItemDecoration(CenterSpacingDecoration(18))
        rvPrayer.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPrayer.adapter = adapter
    }


    private fun getPrayerTimesForToday(): List<PrayerTime> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val longitude = 106.84513
        val latitude = -6.21462
        val timeZone = 7 // WIB
        val fajrTwilight = -20.0
        val ishaTwilight = -18.0

        val times = PrayerCalculator.calcPrayerTimes(
            year, month, day,
            longitude, latitude, timeZone,
            fajrTwilight, ishaTwilight
        )

        fun toHHmm(decimalHour: Double): String {
            val hours = decimalHour.toInt()
            val minutes = ((decimalHour - hours) * 60).toInt()
            return String.format("%02d:%02d", hours, minutes)
        }

        todayPrayerTimes = listOf(
            PrayerTime("🌅", "Subuh", toHHmm(times[0])),
            PrayerTime("🌄", "Syuruq", toHHmm(times[1])),
            PrayerTime("🌞", "Dzuhur", toHHmm(times[2])),
            PrayerTime("🌤️", "Ashar", toHHmm(times[3])),
            PrayerTime("🌇", "Maghrib", toHHmm(times[4])),
            PrayerTime("🌙", "Isya", toHHmm(times[5]))
        )

        return todayPrayerTimes
    }

    private fun getNextPrayer(): PrayerTime? {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        return todayPrayerTimes.firstOrNull {
            parseTimeToMinutes(it.time) > nowMinutes
        }
    }

    private fun getCountdownText(prayerTime: String): String {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val targetMinutes = parseTimeToMinutes(prayerTime)
        val diff = targetMinutes - nowMinutes

        val hours = diff / 60
        val minutes = diff % 60

        return if (hours > 0) {
            "dalam $hours jam $minutes menit"
        } else {
            "dalam $minutes menit"
        }
    }

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour * 60 + minute
    }
}