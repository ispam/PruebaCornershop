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
import com.pruebacornershop.Data.Remote.APIService
import com.pruebacornershop.R
import com.pruebacornershop.Utils.checkFirstRun
import com.pruebacornershop.Utils.hideProgressDialog
import com.pruebacornershop.Utils.showProgressDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    @Inject
    lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.component.inject(this)
        title = "Prueba Cornershop"

        main_recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        checkCounterListFromServer()
        openDialogForNewCounter()
    }

    override fun onStart() {
        super.onStart()
        checkFirstRun(this, setUpTapTarget(), {}, {})
    }

    private fun checkCounterListFromServer() {

        disposable.add(apiService.getCountersList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .switchIfEmpty { Observable.just(emptyList<Counter>()) }
            .map {
                if (it.isEmpty()) {
                    main_recycler.visibility = View.INVISIBLE
                    main_lottie_animation.visibility = View.VISIBLE
                } else {
                    main_lottie_animation.visibility = View.GONE
                    main_recycler.adapter = CounterAdapter(it.toMutableList(), disposable, apiService)
                }
            }
            .subscribe())

    }

    private fun openDialogForNewCounter() {
        main_fab.setOnClickListener { clickView ->
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater = clickView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_counter, null, true)
            val editText = view.findViewById<TextInputEditText>(R.id.dialog_counter_edit)

            val dialog: AlertDialog = builder.setNegativeButton("Cancelar", null)
                .setPositiveButton("Agregar") { dialog1, which ->

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
                            val adapter = CounterAdapter(emptyList<Counter>().toMutableList(), disposable, apiService)
                            main_recycler.adapter = adapter
                            adapter.clearList()
                            adapter.appendCounters(it)

                            main_lottie_animation.visibility = View.GONE
                            main_recycler.visibility = View.VISIBLE
                        }
                        .doOnError { e ->
                            Log.e("TAG", e.localizedMessage)
                            Toast.makeText(clickView.context, "Tenemos problemas de conexion con el servidor, por favor intentalo mas tarde", Toast.LENGTH_LONG).show()
                        }
                        .subscribe())

                }.setView(view).create()

            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
    }

    private fun setUpTapTarget(): () -> Unit {
        return {
            Handler().postDelayed({
                MaterialTapTargetPrompt.Builder(this)
                    .setTarget(main_fab)
//            .setAutoDismiss(false)
//            .setAutoFinish(false)
                    .setPrimaryText("Aun no tenemos informacion, para ingresar un nuevo contador presiona aqui! →")
                    .setBackgroundColour(resources.getColor(R.color.colorPrimaryDark))
                    .setAnimationInterpolator(FastOutSlowInInterpolator())
//            .setPromptFocal(RectanglePromptFocal())
                    .setPromptStateChangeListener { prompt, state ->
                        when (state) {
                            MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                                // User has pressed the prompt target
                            }
                            MaterialTapTargetPrompt.STATE_DISMISSED -> {
                                // Prompt has been removed from view after the prompt has either been pressed somewhere other than the prompt target or the system back button has been pressed
                            }
                            MaterialTapTargetPrompt.STATE_FINISHED -> {
                                // Prompt has been removed from view after the prompt has been pressed in the focal area
                            }
                        }
                    }
                    .show()
            }, 450)
        }
    }

    override fun onStop() {
        if (!disposable.isDisposed) disposable.clear()
        super.onStop()
    }
}
