package com.example.data.model

data class LabSettings(
    val preparedByName: String = "",
    val preparedBySignaturePoints: String = "", // JSON or serialized stroke points
    val paidByName: String = "",
    val paidBySignaturePoints: String = "",
    val isMarkedPaid: Boolean = true
)
