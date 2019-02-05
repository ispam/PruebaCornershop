package com.pruebacornershop.Activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.pruebacornershop.Adapters.CounterAdapter
import com.pruebacornershop.Data.Local.Entities.Counter
import com.pruebacornershop.Data.Local.Entities.Total
import com.pruebacornershop.Data.Local.ViewModels.TotalViewModel
import com.pruebacornershop.Data.Remote.APIService
import com.pruebacornershop.Data.Remote.repository
import com.pruebacornershop.R
import com.pruebacornershop.Utils.checkFirstRun
import com.pruebacornershop.Utils.hideProgressDialog
import com.pruebacornershop.Utils.noInternetConnection
import com.pruebacornershop.Utils.showProgressDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    @Inject
    lateinit var apiService: APIService

    @Inject
    lateinit var totalVM: TotalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.component.inject(this)
        title = getString(R.string.app_name)

        main_recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        getTotalFromLocal()
        repository({checkCounterListFromServer()}, {main_lottie_animation.visibility = View.VISIBLE}, this)
        openDialogForNewCounter()
    }

    override fun onStart() {
        super.onStart()
        checkFirstRun(this, {}, setUpTapTarget(), {})
    }


    private fun getTotalFromLocal() {

        disposable.add(totalVM.getTotal()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { main_total.text = "Total = $it" }
            .subscribe())
    }

    private fun checkCounterListFromServer() {

        disposable.add(apiService.getCountersList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .switchIfEmpty { Observable.just(emptyList<Counter>()) }
            .map { list ->
                if (list.isEmpty()) {
                    main_recycler.visibility = View.INVISIBLE
                    main_lottie_animation.visibility = View.VISIBLE
                    main_total.visibility = View.INVISIBLE
                } else {
                    main_lottie_animation.visibility = View.INVISIBLE
                    main_total.visibility = View.VISIBLE
                    main_recycler.adapter = CounterAdapter(list.toMutableList(), disposable, apiService, totalVM)
                }
            }
            .subscribe())
    }

    private fun howToDeleteTapTarget(adapter: CounterAdapter) {

        disposable.add(adapter.showTapTarget
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                MaterialTapTargetPrompt.Builder(this)
                    .setTarget(it)
                    .setPrimaryText(getString(R.string.adapter_tap_target))
                    .setBackgroundColour(resources.getColor(R.color.colorPrimaryDark))
                    .setPromptBackground(RectanglePromptBackground())
                    .setPromptFocal(RectanglePromptFocal())
                    .show()
            }
            .subscribe())

    }

    private fun openDialogForNewCounter() {
        main_fab.setOnClickListener { clickView ->
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater = clickView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_counter, null, true)
            val editText = view.findViewById<TextInputEditText>(R.id.dialog_counter_edit)

            val dialog: AlertDialog = builder
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .setPositiveButton(getString(R.string.dialog_accept)) { _, _ -> repository({createCounterWithUIUXComponent(editText, clickView)}, {noInternetConnection(this)}, this@MainActivity) }
                .setView(view)
                .create()

            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
    }

    private fun createCounterWithUIUXComponent(editText: TextInputEditText, clickView: View) {
        // This is a chain of actions composed with RxJava to connect
        // with the API service, it includes some UI/UX component showing dialog when
        // connection is made and when it finishes with the stream.
        disposable.add(apiService.createCounter(Counter(editText.text.toString()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showProgressDialog(clickView.context) }
            .delay(350, TimeUnit.MILLISECONDS)
            .doAfterTerminate { hideProgressDialog() }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                val adapter = CounterAdapter(emptyList<Counter>().toMutableList(), disposable, apiService, totalVM)
                main_recycler.adapter = adapter
                adapter.clearList()
                adapter.appendCounters(it)

                howToDeleteTapTarget(adapter)

                main_lottie_animation.visibility = View.INVISIBLE
                main_total.visibility = View.VISIBLE
                main_recycler.visibility = View.VISIBLE
            }
            .doOnError { e -> Log.e("TAG", e.localizedMessage) }

            .subscribe())
    }

    private fun initializeTotal() {
        disposable.add(apiService.getCountersList()
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { totalVM.insertTotal(Total(it.sumBy { it.count!! }.toLong())) }
            .doOnComplete { Log.v("insertTotal", "total created") }
            .subscribe())
    }

    private fun emptyTotal() {
        disposable.add(apiService.getCountersList()
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { totalVM.insertTotal(Total(0)) }
            .doOnComplete { Log.v("insertTotal", "total created") }
            .subscribe())
    }

    private fun setUpTapTarget(): () -> Unit {
        return {
            repository({initializeTotal()}, {emptyTotal()}, this@MainActivity)
            Handler().postDelayed({
                MaterialTapTargetPrompt.Builder(this)
                    .setTarget(main_fab)
                    .setPrimaryText(getString(R.string.tap_target_no_info))
                    .setBackgroundColour(resources.getColor(R.color.colorPrimaryDark))
                    .setAnimationInterpolator(FastOutSlowInInterpolator())
                    .show()
            }, 500)
        }
    }

    override fun onStop() {
        if (!disposable.isDisposed) disposable.clear()
        super.onStop()
    }
}
