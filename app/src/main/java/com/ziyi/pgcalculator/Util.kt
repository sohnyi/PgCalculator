package com.ziyi.pgcalculator

import android.content.Context
import android.widget.Toast

/**
 * Created by Ziyi on 2016/7/28.
 */
object Util {
    private var mToast: Toast? = null
    fun showToast(context: Context?, s: String?) {
        if (mToast == null) {
            mToast = Toast.makeText(context, s, Toast.LENGTH_SHORT)
        } else {
            mToast!!.setText(s)
        }
        mToast!!.show()
    }

    fun showToast(context: Context?, id: Int) {
        if (mToast == null) {
            mToast = Toast.makeText(context, id, Toast.LENGTH_SHORT)
        } else {
            mToast!!.setText(id)
        }
        mToast!!.show()
    }
}