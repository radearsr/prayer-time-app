package mhs.unisbank.prayertime

import android.app.Activity
import android.content.Intent
import android.icu.util.IslamicCalendar
import android.icu.util.Calendar as IslamicCal
import android.icu.util.TimeZone as IslamicTimeZone
import android.icu.util.ULocale
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    private lateinit var ivSetting: ImageView
    private lateinit var ivDate: ImageView
    private lateinit var llNextPrayer: LinearLayout
    private lateinit var tvPrayNextTxt: TextView
    private lateinit var llQuotes: LinearLayout
    private lateinit var prayTimeAdapter: PrayerAdapter


    // INIT TIME
    private lateinit var timeHandler: Handler
    private lateinit var timeRunnable: Runnable

    // DEFAULT LOCATION (SEMARANG)
    var longitude: Double = 106.84513
    var latitude: Double = -6.21462

    private lateinit var quoteHandler: Handler
    private lateinit var quoteRunnable: Runnable
    private var currentQuoteIndex = 0

    private var isDarkMode = false // flag toggle tema

    private lateinit var todayPrayerTimes: List<PrayerTime>

    private lateinit var locationResultLauncher: ActivityResultLauncher<Intent>

    private var locationList: List<LocationModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (isTvDevice()) {
            setContentView(R.layout.activity_main_tv)
        } else {
            setContentView(R.layout.activity_main)
        }
        setupView()
        loadAllLocations()
        setupCurrentDate()
        startLiveClock()
        setupPrayTimeRecyclerView()
        setupActionActivity()
        startQuoteSwitcher()
    }

    private fun setupActionActivity() {
        locationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                latitude = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                longitude = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                val address = data?.getStringExtra("address") ?: "Lokasi belum tersedia"
                tvLocation.text = address
                setupPrayTimeRecyclerView()
                LocationDao.insert(this, latitude, longitude, address)
            }
        }
    }

    private fun loadAllLocations() {
        Thread {
            val result = LocationDao.getAll(this)
            runOnUiThread {
                locationList = result
                if (result.isNotEmpty()) {
                    val latest = result.first()
                    latitude = latest.lat
                    longitude = latest.lon
                    tvLocation.text = latest.address ?: "Lokasi belum tersedia"
                }
            }
        }.start()
    }

    private fun setupCurrentDate() {
        val sdfDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        sdfDate.timeZone = TimeZone.getTimeZone("Asia/Jakarta") // supaya tanggal sesuai WIB
        val todayDate = sdfDate.format(Date())
        tvCurrentDate.text = todayDate
        tvHijriahDate.text = getHijriDate()
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
        ivSetting = findViewById(R.id.iv_setting)
        ivDate = findViewById(R.id.iv_date)
        llNextPrayer = findViewById(R.id.ll_next_prayer)
        tvPrayNextTxt = findViewById(R.id.tv_pray_next_txt)
        llQuotes = findViewById(R.id.ll_quotes)

        ivSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            locationResultLauncher.launch(intent)
        }

        themeButton.setOnClickListener {
            if (isDarkMode) {
                setupViewDarkMode()
            } else {
                setupViewLightMode()
            }
            isDarkMode = !isDarkMode
        }
    }

    private fun setupViewDarkMode() {
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.primary_light))
        mainLayout.setBackgroundResource(R.drawable.gradient_background_light)
        tvLocation.setTextColor(ContextCompat.getColor(this, R.color.background_dark))
        tvHijriahDate.setTextColor(ContextCompat.getColor(this, R.color.background_dark))
        tvCurrentTime.setTextColor(ContextCompat.getColor(this, R.color.primary_light))
        tvCurrentDate.setTextColor(ContextCompat.getColor(this, R.color.background_dark))
        ivLocation.setImageResource(R.drawable.ic_location)
        ivDate.setImageResource(R.drawable.ic_calendar)
        llNextPrayer.setBackgroundResource(R.drawable.bg_prayer_card)
        tvPrayNextTxt.setTextColor(ContextCompat.getColor(this, R.color.background_dark))
        tvNextPrayerCountdown.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        llQuotes.setBackgroundResource(R.drawable.bg_qs_quotes)
        tvQsQuotes.setTextColor(ContextCompat.getColor(this, R.color.background_dark))
        tvQsSource.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        prayTimeAdapter.setDarkMode(false)
        themeButton.setBackgroundResource(R.drawable.bg_mode_default)
        tvNextPrayerName.setTextColor(ContextCompat.getColor(this, R.color.primary_light))
        themeButton.setTextColor(ContextCompat.getColor(this, R.color.background_dark))
        themeButton.text = "🌙 Mode Malam"
    }

    private fun setupViewLightMode() {
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.primary_dark))
        mainLayout.setBackgroundResource(R.drawable.gradient_background_dark)
        tvLocation.setTextColor(ContextCompat.getColor(this, R.color.surface_light))
        tvHijriahDate.setTextColor(ContextCompat.getColor(this, R.color.surface_light))
        tvCurrentTime.setTextColor(ContextCompat.getColor(this, R.color.primary_dark))
        tvCurrentDate.setTextColor(ContextCompat.getColor(this, R.color.surface_light))
        ivLocation.setImageResource(R.drawable.ic_location_dark)
        ivDate.setImageResource(R.drawable.ic_calendar_dark)
        llNextPrayer.setBackgroundResource(R.drawable.bg_prayer_card_dark)
        tvPrayNextTxt.setTextColor(ContextCompat.getColor(this, R.color.surface_light))
        tvNextPrayerCountdown.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark))
        llQuotes.setBackgroundResource(R.drawable.bg_qs_quotes_dark)
        tvQsQuotes.setTextColor(ContextCompat.getColor(this, R.color.surface_light))
        tvQsSource.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_dark))
        prayTimeAdapter.setDarkMode(true)
        themeButton.setBackgroundResource(R.drawable.bg_mode_dark)
        tvNextPrayerName.setTextColor(ContextCompat.getColor(this, R.color.primary_dark))
        themeButton.setTextColor(ContextCompat.getColor(this, R.color.surface_light))
        themeButton.text = "☀️ Mode Terang"
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

    private fun startQuoteSwitcher() {
        val quotes = listOf(
            Pair("Dan dirikanlah shalat, tunaikanlah zakat dan ruku'lah beserta orang-orang yang ruku'", "- QS. Al-Baqarah: 43"),
            Pair("Sesungguhnya shalat itu mencegah dari perbuatan keji dan mungkar.", "- QS. Al-Ankabut: 45"),
            Pair("Peliharalah semua shalat dan shalat wustha. Berdirilah karena Allah (dalam shalatmu) dengan khusyuk.", "- QS. Al-Baqarah: 238"),
            Pair("Hai orang-orang yang beriman, jadikanlah sabar dan shalat sebagai penolongmu.", "- QS. Al-Baqarah: 153"),
            Pair("Sungguh, beruntunglah orang-orang yang beriman, (yaitu) yang khusyuk dalam shalatnya.", "- QS. Al-Mu’minun: 1-2")
        )
        quoteHandler = Handler(Looper.getMainLooper())
        quoteRunnable = object : Runnable {
            override fun run() {
                val (text, source) = quotes[currentQuoteIndex]
                tvQsQuotes.text = text
                tvQsSource.text = source

                currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
                quoteHandler.postDelayed(this, 5000) // ganti setiap 5 detik
            }
        }
        quoteHandler.post(quoteRunnable)
    }


    override fun onDestroy() {
        super.onDestroy()
        timeHandler.removeCallbacks(timeRunnable)
    }

    private fun setupPrayTimeRecyclerView() {
        val prayerTimes = getPrayerTimesForToday()
        prayTimeAdapter = PrayerAdapter(prayerTimes)
        prayTimeAdapter.setTvDevice(isTvDevice())
        val spacing = if (isTvDevice()) 18 else 75
        Log.d("MainActivity", "Spacing: $spacing")
        rvPrayer.addItemDecoration(CenterSpacingDecoration(spacing))
        rvPrayer.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPrayer.adapter = prayTimeAdapter
    }


    private fun getPrayerTimesForToday(): List<PrayerTime> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

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


    private fun isTvDevice(): Boolean {
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as android.app.UiModeManager
        return uiModeManager.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
    }

    private fun getHijriDate(): String {
        val months = listOf("Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal",
            "Jumadil Akhir", "Rajab", "Sya'ban", "Ramadhan", "Syawal", "Dzulkaidah", "Dzulhijjah")
        val tz = IslamicTimeZone.getTimeZone("Asia/Jakarta")
        val uLocale = ULocale("id@calendar=islamic-umalqura")
        val islamicCalendar = IslamicCalendar(tz, uLocale)
        val day = islamicCalendar.get(IslamicCal.DAY_OF_MONTH)
        val monthIndex = islamicCalendar.get(IslamicCal.MONTH)
        val month = months[monthIndex.coerceIn(0, months.size - 1)]
        val year = islamicCalendar.get(IslamicCal.YEAR)
        return "$day $month $year H"
    }


    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour * 60 + minute
    }
}