package com.pruebacornershop.Adapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pruebacornershop.Data.Local.Entities.Counter
import com.pruebacornershop.R

class CounterAdapter(val counterList: MutableList<Counter>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.format_counter, parent, false))
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return counterList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val counter = counterList[position]
        (holder as ViewHolder).bind(counter)
    }

    fun clearList() {
        counterList.clear()
        notifyDataSetChanged()
    }

    fun appendCounters(countersToAppend: List<Counter>) {
        counterList.addAll(countersToAppend)
        notifyDataSetChanged()
    }

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.format_counter_title)
        private val total: TextView = view.findViewById(R.id.format_counter_total)
        private val subtract: ImageView = view.findViewById(R.id.format_counter_subtract)
        private val add: ImageView = view.findViewById(R.id.format_counter_add)

        fun bind(counter: Counter) {

            title.text = counter.title
            var count = counter.count!!
            total.text = count.toString()

            subtract.setOnTouchListener(tintColorOnState({total.text = count--.toString()}, subtract))
            add.setOnTouchListener(tintColorOnState({total.text = count++.toString()}, add))
        }

        private fun tintColorOnState(function: () -> Unit, view: ImageView): View.OnTouchListener {

            return View.OnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        view.clearColorFilter()
                        true
                    }
                    MotionEvent.ACTION_DOWN -> {
                        view.setColorFilter(Color.parseColor("#ce3737"), PorterDuff.Mode.SRC_IN)
                        function.invoke()
                        true
                    }
                    else -> {
                        view.clearColorFilter()
                        false
                    }
                }
            }
        }
    }
}