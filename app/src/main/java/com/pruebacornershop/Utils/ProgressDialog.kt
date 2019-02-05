package com.pruebacornershop.Utils

import android.app.ProgressDialog
import android.content.Context
import com.pruebacornershop.R

private lateinit var progressDialog: ProgressDialog

fun showProgressDialog(context: Context): ProgressDialog {
    progressDialog = ProgressDialog(context)
    progressDialog.setCancelable(false)
    progressDialog.setMessage(context.getString(R.string.dialog_loading))
    progressDialog.show()
    return progressDialog
}

fun hideProgressDialog() {
    if (::progressDialog.isInitialized && progressDialog.isShowing) progressDialog.dismiss()
}