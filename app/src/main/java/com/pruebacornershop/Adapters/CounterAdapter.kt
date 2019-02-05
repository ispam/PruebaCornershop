package com.pruebacornershop.Adapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pruebacornershop.Data.Local.Entities.Counter
import com.pruebacornershop.Data.Local.Entities.CounterID
import com.pruebacornershop.Data.Remote.APIService
import com.pruebacornershop.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CounterAdapter(val counterList: MutableList<Counter>, val disposable: CompositeDisposable, val apiService: APIService) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.format_counter_title)
        private val total: TextView = view.findViewById(R.id.format_counter_total)
        private val subtract: ImageView = view.findViewById(R.id.format_counter_subtract)
        private val add: ImageView = view.findViewById(R.id.format_counter_add)

        fun bind(counter: Counter) {

            title.text = counter.title
            var count = counter.count?: 0
            total.text = count.toString()
            val id = counter.id?: ""

            subtract.setOnTouchListener(tintColorOnState(decreaseCounter(id), subtract))
            add.setOnTouchListener(tintColorOnState(incrementCounter(id), add))
        }

        private fun incrementCounter(id: String): () -> Unit {
            return {
                disposable.add(apiService.incrementCounter(CounterID(id))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { e -> Log.e("incrementCounter", e.localizedMessage) }
                    .doOnSuccess { clearList(); appendCounters(it) }
                    .subscribe())
            }
        }

        private fun decreaseCounter(id: String): () -> Unit {
            return {
                disposable.add(apiService.decreaseCounter(CounterID(id))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { e -> Log.e("decreaseCounter", e.localizedMessage) }
                    .doOnSuccess { clearList(); appendCounters(it) }
                    .subscribe())
            }
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