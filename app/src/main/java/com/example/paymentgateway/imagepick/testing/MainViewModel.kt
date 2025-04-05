package com.example.paymentgateway.imagepick.testing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

import java.util.UUID


class MainViewModel : ViewModel() {
    private val _rowsData = MutableLiveData<List<Row>>()
    val rowsData: LiveData<List<Row>> = _rowsData


    val firstRowsData: LiveData<List<Row>> = _rowsData.map { rows ->
        if (rows.isEmpty()) {
            emptyList()
        } else {
            val midPoint = (rows.size / 2).coerceAtLeast(1)
            rows.subList(0, midPoint).toList()
        }
    }

    val secondRowsData: LiveData<List<Row>> = _rowsData.map { rows ->
        if (rows.isEmpty()) {
            emptyList()
        } else {
            val midPoint = (rows.size / 2).coerceAtLeast(1)
            rows.subList(midPoint, rows.size).toList()
        }
    }

    init {
        _rowsData.value = List(10) { Row(id = UUID.randomUUID().toString(), title = "Row #${it + 1}", image = "") }
    }

    fun moveRow(movingRowId: String, shiftingRowId: String) {
        val currentList = _rowsData.value?.toMutableList() ?: return
        val movingRowIndex = currentList.indexOfFirst { it.id == movingRowId }
        val shiftingRowIndex = currentList.indexOfFirst { it.id == shiftingRowId }

        if (movingRowIndex != -1 && shiftingRowIndex != -1) {
            val movingRow = currentList[movingRowIndex]
            currentList.removeAt(movingRowIndex)
            currentList.add(shiftingRowIndex, movingRow)
            _rowsData.value = currentList
        }

    }


    fun updateRows(newRows: List<Row>) {
        val currentRows = rowsData.value ?: mutableListOf()
        _rowsData.value = currentRows
    }


    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }


    data class Row(
        val id: String,
        val title: String,
        val image: String
    )
}