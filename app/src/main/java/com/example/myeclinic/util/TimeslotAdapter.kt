package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
/**
 * RecyclerView adapter for displaying a list of available or booked time slots.
 *
 * Each item shows the hour and booking status, with color-coded backgrounds.
 * A click listener is provided for selecting a timeslot.
 *
 * @property onClick Callback invoked when a timeslot item is clicked.
 */
class TimeslotAdapter(
    private val onClick: (Timeslot) -> Unit
) : ListAdapter<Timeslot, TimeslotAdapter.TimeslotViewHolder>(TimeslotDiffCallback()) {
    /**
     * Inflates the layout for a single timeslot item.
     *
     * @param parent The parent view group.
     * @param viewType The view type (not used).
     * @return A new [TimeslotViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeslotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeslot_second, parent, false)
        return TimeslotViewHolder(view)
    }
    /**
     * Binds a [Timeslot] to the corresponding view holder.
     *
     * @param holder The holder to bind.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: TimeslotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    /**
     * ViewHolder for displaying a single timeslot with hour and availability status.
     *
     * Colors:
     * - Green: Available
     * - Red: Booked
     */
    inner class TimeslotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        /**
         * Binds the [Timeslot] data to the UI elements and sets up click handling.
         *
         * @param timeslot The timeslot to display.
         */
        fun bind(timeslot: Timeslot) {
            timeText.text = timeslot.hour
            statusText.text = if (timeslot.booked) "Booked" else "Available"

            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (timeslot.booked) R.color.red else R.color.green
                )
            )

            itemView.setOnClickListener { onClick(timeslot) }

            itemView.contentDescription = "${timeslot.hour} is ${if (timeslot.booked) "booked" else "available"}"
        }
    }
    /**
     * A [DiffUtil.ItemCallback] implementation to efficiently update the timeslot list.
     */
    class TimeslotDiffCallback : DiffUtil.ItemCallback<Timeslot>() {
        override fun areItemsTheSame(oldItem: Timeslot, newItem: Timeslot) =
            oldItem.hour == newItem.hour

        override fun areContentsTheSame(oldItem: Timeslot, newItem: Timeslot) =
            oldItem == newItem
    }
}

