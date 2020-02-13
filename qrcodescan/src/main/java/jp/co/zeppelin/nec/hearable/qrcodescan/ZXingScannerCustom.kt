package jp.co.zeppelin.nec.hearable.qrcodescan

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.core.ViewFinderView
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ZXingScannerCustom(context: Context, attrs: AttributeSet?) :
    ZXingScannerView(context, attrs) {

    constructor(context: Context) : this(context, null)

    override fun createViewFinderView(context: Context?): IViewFinder {
        return CustomViewFinder(
            context!!
        )
    }

    /**
     * Override ZXingScanner viewfinder class with no-op draw method
     *
     * Motivation
     * To use custom viewfinder, simplest way is to define the layout via xml; this override
     * simply prevents the default viewfinder from being displayed at all
     */
    class CustomViewFinder(context: Context, attrs: AttributeSet?) :
        ViewFinderView(context, attrs) {
        constructor(context: Context) : this(context, null)

        override fun onDraw(canvas: Canvas?) {
        }
    }
}
