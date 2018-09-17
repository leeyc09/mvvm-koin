package xlab.world.xlab.utils.view.imageView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import xlab.world.xlab.R

class CircleOverlayView: View {

    private lateinit var transparentPaint: Paint
    private lateinit var semiBlackPaint: Paint
    private var path: Path = Path()

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        transparentPaint = Paint()
        transparentPaint.color = Color.TRANSPARENT
        transparentPaint.strokeWidth = 10.0f

        semiBlackPaint = Paint()
        semiBlackPaint.color = Color.TRANSPARENT
        semiBlackPaint.strokeWidth = 10.0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()

        path.addCircle(canvas.width * 0.5f, canvas.height * 0.5f, canvas.width * 0.5f, Path.Direction.CW)
        path.fillType = Path.FillType.INVERSE_EVEN_ODD

        canvas.drawCircle(canvas.width * 0.5f, canvas.height * 0.5f, canvas.width * 0.5f, transparentPaint)

        canvas.drawPath(path, semiBlackPaint)
        canvas.clipPath(path)
        canvas.drawColor(ResourcesCompat.getColor(context.resources, R.color.colorWhite80, null))
    }
}