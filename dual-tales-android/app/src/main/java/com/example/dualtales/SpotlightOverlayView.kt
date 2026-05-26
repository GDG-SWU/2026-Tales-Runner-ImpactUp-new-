package com.example.dualtales

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SpotlightOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint().apply {
        color = 0x99000000.toInt()
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    var spotlightRect: RectF = RectF()
    var cornerRadius: Float = 0f

    init {
        // CLEAR 모드가 정상 동작하려면 하드웨어 레이어 필요
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        // saveLayer로 격리된 레이어 생성 (CLEAR 모드 적용을 위해 필수)
        val sc = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // 전체 어두운 배경
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // spotlight 영역이 설정된 경우에만 구멍 뚫기
        if (!spotlightRect.isEmpty) {
            canvas.drawRoundRect(spotlightRect, cornerRadius, cornerRadius, clearPaint)
        }

        canvas.restoreToCount(sc)
    }
}
