package com.pruebacornershop.Adapters

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pruebacornershop.Data.Local.Entities.Counter
import com.pruebacornershop.Data.Local.Entities.CounterID
import com.pruebacornershop.Data.Local.ViewModels.TotalViewModel
import com.pruebacornershop.Data.Remote.APIService
import com.pruebacornershop.Data.Remote.repository
import com.pruebacornershop.R
import com.pruebacornershop.Utils.isOnline
import com.pruebacornershop.Utils.noInternetConnection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class CounterAdapter(
    val counterList: MutableList<Counter>,
    val disposable: CompositeDisposable,
    val apiService: APIService,
    val totalVM: TotalViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val publishSubject: PublishSubject<CardView> = PublishSubject.create()
    var showTapTarget: Observable<CardView> = publishSubject
    private var checkOnce = true

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
        private val card: CardView = view.findViewById(R.id.format_counter_card)

        fun bind(counter: Counter) {

            title.text = counter.title
            total.text = "${counter.count ?: 0}"

            subtract.setOnTouchListener(tintColorOnState(decreaseCounter(counter.id ?: "") , subtract))

            add.setOnTouchListener(tintColorOnState(incrementCounter(counter.id ?: "") , subtract))

            card.setOnLongClickListener { longView ->
                deleteDialog(longView, counter)
                return@setOnLongClickListener true
            }

            if (this@CounterAdapter.itemCount == 1 && checkOnce) {
                publishSubject.onNext(card)
                checkOnce = false
            }

        }

        private fun deleteDialog(view: View, counter: Counter) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
            val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_elimination, null, true)
            val title = view.findViewById<TextView>(R.id.dialog_elimination_title)
            val message = view.findViewById<TextView>(R.id.dialog_elimination_message)

            title.text = view.context.getString(R.string.dialog_elimination_title)
            message.text = "Se eliminará el contador \"${counter.title}\""

            val dialog: AlertDialog = builder
                .setNegativeButton(view.context.getString(R.string.dialog_cancel), null)
                .setPositiveButton(view.context.getString(R.string.dialog_delete)) { _, _ ->
                    repository(
                        { deleteCounter(counter.id ?: "") },
                        { noInternetConnection(view.context) },
                        view.context
                    )
                }
                .setView(view)
                .create()

            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        private fun deleteCounter(id: String) {
            disposable.add(apiService.deleteCounter(CounterID(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { e -> Log.e("deleteCounter", e.localizedMessage) }
                .doOnSuccess { clearList(); appendCounters(it) }
                .flatMapCompletable {
                    val total = it.sumBy { it.count!! }.toLong()
                    totalVM.updateTotal(total, 1).subscribeOn(Schedulers.newThread())
                }
                .subscribe())
        }

        private fun incrementCounter(id: String): () -> Unit {
            return {
                disposable.add(apiService.incrementCounter(CounterID(id))
                    .subscribeOn(Schedulers.io())
                    .doOnError { e -> Log.e("incrementCounter", e.localizedMessage) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess { clearList(); appendCounters(it) }
                    .flatMapCompletable {
                        val total = it.sumBy { it.count!! }.toLong()
                        totalVM.updateTotal(total, 1)
                            .subscribeOn(Schedulers.newThread())
                    }
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
                    .flatMapCompletable {
                        val total = it.sumBy { it.count!! }.toLong()
                        totalVM.updateTotal(total, 1)
                            .subscribeOn(Schedulers.newThread())
                    }
                    .subscribe())
            }
        }

        private fun tintColorOnState(function: () -> Unit, view: ImageView): View.OnTouchListener {

            return View.OnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        repository({view.clearColorFilter()}, {noInternetConnection(view.context)}, view.context)
                        true
                    }
                    MotionEvent.ACTION_DOWN -> {
                        repository({
                            view.setColorFilter(Color.parseColor("#ce3737"), PorterDuff.Mode.SRC_IN)
                            function.invoke()
                        }, {noInternetConnection(view.context)}, view.context)
                        true
                    }
                    else -> {
                        repository({view.clearColorFilter()}, {noInternetConnection(view.context)}, view.context)
                        false
                    }
                }
            }
        }
    }
}