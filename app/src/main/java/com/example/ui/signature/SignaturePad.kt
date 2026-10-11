package com.example.ui.signature

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

data class StrokePath(
    val points: List<Offset>
)

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    strokes: List<StrokePath>,
    onStrokeAdded: (StrokePath) -> Unit
) {
    val currentPoints = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFF6B7280), RoundedCornerShape(8.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                onStrokeAdded(StrokePath(currentPoints.toList()))
                                currentPoints.clear()
                            }
                        },
                        onDragCancel = {
                            currentPoints.clear()
                        }
                    )
                }
        ) {
            // Draw finalized strokes
            for (stroke in strokes) {
                if (stroke.points.size > 1) {
                    val path = Path()
                    path.moveTo(stroke.points[0].x, stroke.points[0].y)
                    for (i in 1 until stroke.points.size) {
                        path.lineTo(stroke.points[i].x, stroke.points[i].y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF111827),
                        style = Stroke(
                            width = 3.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (stroke.points.size == 1) {
                    drawCircle(
                        color = Color(0xFF111827),
                        radius = 2.5f,
                        center = stroke.points[0]
                    )
                }
            }

            // Draw active stroke
            if (currentPoints.size > 1) {
                val path = Path()
                path.moveTo(currentPoints[0].x, currentPoints[0].y)
                for (i in 1 until currentPoints.size) {
                    path.lineTo(currentPoints[i].x, currentPoints[i].y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF111827),
                    style = Stroke(
                        width = 3.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            } else if (currentPoints.size == 1) {
                drawCircle(
                    color = Color(0xFF111827),
                    radius = 2.5f,
                    center = currentPoints[0]
                )
            }
        }
    }
}

object SignatureSerializer {
    // Serializes strokes to "x,y;x,y|x,y;x,y"
    fun serialize(strokes: List<StrokePath>): String {
        return strokes.joinToString("|") { stroke ->
            stroke.points.joinToString(";") { "${it.x},${it.y}" }
        }
    }

    fun deserialize(data: String): List<StrokePath> {
        if (data.isBlank()) return emptyList()
        return try {
            data.split("|").mapNotNull { strokeStr ->
                if (strokeStr.isBlank()) null
                else {
                    val points = strokeStr.split(";").mapNotNull { ptStr ->
                        val parts = ptStr.split(",")
                        if (parts.size == 2) {
                            val x = parts[0].toFloatOrNull()
                            val y = parts[1].toFloatOrNull()
                            if (x != null && y != null) Offset(x, y) else null
                        } else null
                    }
                    if (points.isNotEmpty()) StrokePath(points) else null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun createBitmap(strokes: List<StrokePath>, width: Int = 300, height: Int = 130): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 3.5f
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (stroke in strokes) {
            if (stroke.points.size > 1) {
                val androidPath = android.graphics.Path()
                androidPath.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    androidPath.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
                canvas.drawPath(androidPath, paint)
            } else if (stroke.points.size == 1) {
                canvas.drawCircle(stroke.points[0].x, stroke.points[0].y, 2f, paint)
            }
        }
        return bitmap
    }
}
