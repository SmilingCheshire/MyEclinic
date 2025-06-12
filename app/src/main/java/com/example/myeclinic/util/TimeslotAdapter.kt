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

class TimeslotAdapter(
    private val onClick: (Timeslot) -> Unit
) : ListAdapter<Timeslot, TimeslotAdapter.TimeslotViewHolder>(TimeslotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeslotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeslot_second, parent, false)
        return TimeslotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeslotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TimeslotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)

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

    class TimeslotDiffCallback : DiffUtil.ItemCallback<Timeslot>() {
        override fun areItemsTheSame(oldItem: Timeslot, newItem: Timeslot) =
            oldItem.hour == newItem.hour

        override fun areContentsTheSame(oldItem: Timeslot, newItem: Timeslot) =
            oldItem == newItem
    }
}

