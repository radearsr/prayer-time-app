package mhs.unisbank.prayertime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.TimeZone

class PrayerAdapter(private val items: List<PrayerTime>) :
    RecyclerView.Adapter<PrayerAdapter.PrayerViewHolder>() {

        private var isDarkMode = false

    fun setDarkMode(enabled: Boolean) {
        isDarkMode = enabled
        notifyDataSetChanged()
    }

    inner class PrayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconEmoji: TextView = itemView.findViewById(R.id.iconEmoji)
        val prayerName: TextView = itemView.findViewById(R.id.prayerName)
        val prayerTime: TextView = itemView.findViewById(R.id.prayerTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.prayer_card, parent, false)
        return PrayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        val item = items[position]
        holder.iconEmoji.text = item.emoji
        holder.prayerName.text = item.name
        holder.prayerTime.text = item.time

        val currentMinutes = getCurrentMinutes()
        val thisTime = parseTimeToMinutes(item.time)
        val nextTime = if (position + 1 < items.size)
            parseTimeToMinutes(items[position + 1].time)
        else 1440 // jam 24:00

        val isCurrentPrayer = currentMinutes in thisTime until nextTime

        val cardLayout = holder.itemView.findViewById<ConstraintLayout>(R.id.prayer_card)
        if (isCurrentPrayer) {
            cardLayout.setBackgroundResource(R.drawable.bg_prayer_next)
            holder.prayerName.setTextColor(holder.itemView.context.resources.getColor(R.color.next_prayer_border_light))
        } else {
            if (isDarkMode) {
                cardLayout.setBackgroundResource(R.drawable.bg_prayer_card_dark)
                holder.prayerName.setTextColor(holder.itemView.context.resources.getColor(R.color.surface_light))
            } else {
                cardLayout.setBackgroundResource(R.drawable.bg_prayer_card)
                holder.prayerName.setTextColor(holder.itemView.context.resources.getColor(R.color.surface_dark))
            }
        }
    }


    override fun getItemCount(): Int = items.size

    private fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour * 60 + minute
    }

    private fun getCurrentMinutes(): Int {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return hour * 60 + minute
    }
}
