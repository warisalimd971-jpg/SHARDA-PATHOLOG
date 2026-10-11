package com.example

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PatientRecord
import com.example.ui.signature.SignatureSerializer
import com.example.ui.signature.StrokePath
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sharda Lab", appName)
    }

    @Test
    fun `patient calculation test`() {
        val patient = PatientRecord(
            date = "2026-10-11",
            name = "Test Patient",
            billAmount = 1000.0,
            lessAmount = 200.0,
            paidAmount = 700.0
        )
        assertEquals(100.0, patient.dueAmount, 0.01)
        assertEquals(20.0, patient.lessPercentage, 0.01)
        assertEquals(70.0, patient.paidPercentage, 0.01)
    }

    @Test
    fun `signature serializer test`() {
        val stroke = StrokePath(listOf(Offset(10f, 20f), Offset(30f, 40f)))
        val serialized = SignatureSerializer.serialize(listOf(stroke))
        val deserialized = SignatureSerializer.deserialize(serialized)
        assertEquals(1, deserialized.size)
        assertEquals(2, deserialized[0].points.size)
        assertEquals(10f, deserialized[0].points[0].x, 0.01f)
        assertEquals(20f, deserialized[0].points[0].y, 0.01f)
    }
}
