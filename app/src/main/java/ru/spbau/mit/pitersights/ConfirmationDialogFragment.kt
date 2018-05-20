package ru.spbau.mit.pitersights

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.support.annotation.StringRes
import android.widget.Toast
import android.content.DialogInterface
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat

class ConfirmationDialogFragment(): DialogFragment() {
    private val ARG_MESSAGE = "message"
    private val ARG_PERMISSIONS = "permissions"
    private val ARG_REQUEST_CODE = "request_code"
    private val ARG_NOT_GRANTED_MESSAGE = "not_granted_message"

    @SuppressLint("ValidFragment")
    constructor(@StringRes message: Int,
                permissions: Array<String>,
                requestCode: Int,
                @StringRes notGrantedMessage: Int) : this() {
        val args = Bundle()
        args.putInt(ARG_MESSAGE, message)
        args.putStringArray(ARG_PERMISSIONS, permissions)
        args.putInt(ARG_REQUEST_CODE, requestCode)
        args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage)
        arguments = args
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
        return AlertDialog.Builder(activity)
                .setMessage(args!!.getInt(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok,
                        DialogInterface.OnClickListener { dialog, which ->
                            val permissions = args.getStringArray(ARG_PERMISSIONS)
                                    ?: throw IllegalArgumentException()
                            activity!!.let {
                                ActivityCompat.requestPermissions(it,
                                        permissions, args.getInt(ARG_REQUEST_CODE))
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        DialogInterface.OnClickListener { dialog, which ->
                            Toast.makeText(activity,
                                    args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                    Toast.LENGTH_SHORT).show()
                        })
                .create()
    }
}