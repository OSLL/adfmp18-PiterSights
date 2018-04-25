package ru.spbau.mit.pitersights

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View

class SightDescription : DialogFragment() {
    override fun onCreateDialog(savedInstanceState : Bundle) : Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.description)
                .setOnKeyListener { dialog, keyCode, event ->
                    TODO("if we press the dialog window, it will close")
                }.setNeutralButton(R.string.navigate_to) {
                    dialog, which -> TODO("do navigation to the sight")
                }
        return builder.create()
    }
}