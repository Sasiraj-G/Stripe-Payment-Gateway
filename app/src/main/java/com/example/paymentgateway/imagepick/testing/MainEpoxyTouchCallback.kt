package com.example.paymentgateway.imagepick.testing


import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModelTouchCallback
import com.airbnb.epoxy.EpoxyViewHolder
import com.example.paymentgateway.imagepick.UploadImagePickerModel
import com.example.paymentgateway.imagepick.UploadImagePickerModel_

class MainEpoxyTouchCallback(
    controller: EpoxyController,
    private val listener: OnRowMoveListener
) : EpoxyModelTouchCallback<UploadImagePickerModel>(controller, UploadImagePickerModel::class.java) {
    interface OnRowMoveListener {
        fun onMoved(movingRowId: String, shiftingRowId: String)
    }
    override fun onMoved(
        recyclerView: RecyclerView?,
        viewHolder: EpoxyViewHolder?,
        fromPos: Int,
        target: EpoxyViewHolder?,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        val movingRowId = viewHolder?.model
            ?.takeIf { it is UploadImagePickerModel_ }
            ?.let { (it as UploadImagePickerModel_).rowId() }
        val shiftingRowId = target?.model
            ?.takeIf { it is UploadImagePickerModel_ }
            ?.let { (it as UploadImagePickerModel_).rowId() }
        if (movingRowId != null && shiftingRowId != null) {
            listener.onMoved(movingRowId, shiftingRowId)
        }
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
    }
    override fun getMovementFlagsForModel(model: UploadImagePickerModel?, adapterPosition: Int) =
        makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)
    override fun isLongPressDragEnabled() = true


}