package com.example.paymentgateway.imagepick.testing

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class MainSimpleOnItemTouchListener(private val listener: OnInterceptTouchEventListener) : RecyclerView.OnItemTouchListener {
    interface OnInterceptTouchEventListener {
        fun onInterceptTouchEvent(touchedPosition: Int)
    }
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            val childView = rv.findChildViewUnder(e.x, e.y)
            if (childView != null) {
                val touchedPosition = rv.getChildAdapterPosition(childView)
                listener.onInterceptTouchEvent(touchedPosition)
            }
        }
        return false
    }
    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}
