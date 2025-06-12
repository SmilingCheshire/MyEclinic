package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.model.Booking
import java.util.Date

class BookingsAdapter(
    private val onCancelClick: (Booking, String) -> Unit
) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    private val bookings = mutableListOf<Pair<Booking, String>>() // Pair<Booking, bookingId>

    fun submitList(newBookings: List<Pair<Booking, String>>) {
        bookings.clear()
        bookings.addAll(newBookings)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val (booking, bookingId) = bookings[position]
        holder.bind(booking, bookingId)
    }

    override fun getItemCount(): Int = bookings.size


    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val doctorName: TextView = itemView.findViewById(R.id.doctorNameText)
        private val date: TextView = itemView.findViewById(R.id.dateText)
        private val time: TextView = itemView.findViewById(R.id.timeText)
        private val status: TextView = itemView.findViewById(R.id.statusText)
        private val cancelBtn: Button = itemView.findViewById(R.id.cancelBookingBtn)

        fun bind(booking: Booking, bookingId: String) {
            doctorName.text = booking.doctorName
            date.text = booking.date
            time.text = booking.time
            status.text = booking.status

            // Parse scheduled date + time
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
            val appointmentDateTime = try {
                formatter.parse("${booking.date} ${booking.time}")
            } catch (e: Exception) {
                null
            }

            val now = Date()
            val isActive = booking.status == "booked" && appointmentDateTime?.after(now) == true

            cancelBtn.isVisible = isActive

            // Background color
            val bgRes = if (isActive) {
                R.drawable.active_booking_bg
            } else {
                R.drawable.inactive_booking_bg
            }
            itemView.setBackgroundResource(bgRes)

            // Text color
            val textColor = if (isActive) {
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }

            doctorName.setTextColor(textColor)
            date.setTextColor(textColor)
            time.setTextColor(textColor)
            status.setTextColor(textColor)

            cancelBtn.setOnClickListener {
                onCancelClick(booking, bookingId)
            }
        }
    }

        class DiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean =
            oldItem.bookedAt == newItem.bookedAt

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean =
            oldItem == newItem
    }
}
