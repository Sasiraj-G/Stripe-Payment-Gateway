package com.example.paymentgateway.imagepick.testing


import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModelTouchCallback
import com.airbnb.epoxy.EpoxyViewHolder
import com.example.paymentgateway.imagepick.ImagePickerModel
import com.example.paymentgateway.imagepick.UploadImagePickerModel
import com.example.paymentgateway.imagepick.UploadImagePickerModel_



class MainEpoxyTouchCallback(
    controller: EpoxyController,
    private val listener: OnRowMoveListener
) : EpoxyModelTouchCallback<UploadImagePickerModel>(controller, UploadImagePickerModel::class.java) {
    interface OnRowMoveListener {
        fun onMoved(movingRowId: String, shiftingRowId: String)
    }

    private var draggedItemId: String? = null
    private var pendingMove: Pair<String, String>? = null

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: EpoxyViewHolder,
        fromPos: Int,
        target: EpoxyViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        val movingRowId = (viewHolder.model as? UploadImagePickerModel_)?.rowId()
        val shiftingRowId = (target.model as? UploadImagePickerModel_)?.rowId()

        if (movingRowId != null && shiftingRowId != null) {
            pendingMove = Pair(movingRowId, shiftingRowId)
        }
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
    }

    override fun onDragStarted(model: UploadImagePickerModel?, itemView: View?, adapterPosition: Int) {
        draggedItemId = (model as? UploadImagePickerModel_)?.rowId()
        pendingMove = null // Clear any previous pending move
        super.onDragStarted(model, itemView, adapterPosition)
    }

    override fun onDragReleased(model: UploadImagePickerModel, itemView: View) {
        draggedItemId = null
        pendingMove?.let { (movingId, shiftingId) ->
            listener.onMoved(movingId, shiftingId)
            pendingMove = null
        }
        super.onDragReleased(model, itemView)
    }


    override fun getMovementFlagsForModel(model: UploadImagePickerModel?, adapterPosition: Int) =
        makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)

    override fun isLongPressDragEnabled() = true
}


