package nz.co.sha.zxing

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import com.journeyapps.barcodescanner.ViewfinderView

class CustomViewFinderView(context: Context?, attrs: AttributeSet?) : ViewfinderView(context, attrs) {
    init {
        afterMeasured {
            framingRect.bottom
        }
    }
}


inline fun View.afterMeasured(crossinline block: () -> Unit) {
    if (measuredWidth > 0 && measuredHeight > 0) {
        block()
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    block()
                }
            }
        })
    }
}