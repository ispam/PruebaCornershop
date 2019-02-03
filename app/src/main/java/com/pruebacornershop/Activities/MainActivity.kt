package com.pruebacornershop.Activities

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.pruebacornershop.Adapters.CounterAdapter
import com.pruebacornershop.Data.Local.Entities.Counter
import com.pruebacornershop.R
import com.pruebacornershop.Utils.checkFirstRun
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Prueba Cornershop"

        setUpRecycler()
        openDialogForNewCounter()
    }

    override fun onStart() {
        super.onStart()
        checkFirstRun(this, setUpTapTarget(), {}, {})
    }

    private fun openDialogForNewCounter() {
        main_fab.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater = it.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_counter, null, true)
            val editText = view.findViewById<TextInputEditText>(R.id.dialog_counter_edit)

            val dialog: AlertDialog = builder.setNegativeButton("Cancelar", null)
                    .setPositiveButton("Agregar") { dialog1, which ->
                        Toast.makeText(this, "${editText.text}", Toast.LENGTH_LONG).show()
                    }.setView(view).create()

            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
    }

    private fun setUpRecycler() {
        main_recycler.visibility = View.GONE // TODO REMOVE THIS
        main_recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        main_recycler.adapter = CounterAdapter(randomPlaceholders())
    }

    private fun randomPlaceholders(): List<Counter> {

        val randomList = mutableListOf<Counter>()
        for (num in 0..19) {
            randomList.add(Counter("Test ${num + 1}"))
        }
        return randomList
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
