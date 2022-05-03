package com.konovus.myfiles.ui.dialogScreen

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.konovus.myfiles.TAG

class SafetyDialogFragment : DialogFragment() {


    companion object {
         val deletionTrigger = MutableLiveData(false)
    }

    fun newInstance(nr: Int): SafetyDialogFragment {
        val instance = SafetyDialogFragment()
        val args = Bundle()
        args.putInt("nr", nr)
        instance.arguments = args
        return instance
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val nr = arguments?.getInt("nr") ?: 0
        val suffix =  if (nr == 1) "item" else "items"

        return AlertDialog.Builder(requireContext())
            .setMessage("Are you sure you want to delete $nr $suffix")
            .setPositiveButton("Ok") { _, _ ->
                deletionTrigger.postValue(true)
                Log.i(TAG, "onCreateDialog: positive BTN")
            }
            .setNegativeButton("Cancel") {_, _ ->
                Log.i(TAG, "onCreateDialog: negative BTN")
            }
            .create()

    }

    override fun onStart() {
        super.onStart()
        if (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_NO) {
            (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY)
            (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY)
        }

    }
}