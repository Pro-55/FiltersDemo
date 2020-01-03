package com.example.filtersdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import com.example.filtersdemo.model.ImageFilterItem
import com.example.filtersdemo.utils.createFilters
import com.example.filtersdemo.utils.extensions.getDisplayMetrics
import com.example.filtersdemo.utils.extensions.getPaintedBitmap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val SDT = 125
        private const val SVT = 75

        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }

    private val bitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.test_image) }
    private val imageFilters by lazy { arrayListOf<ImageFilterItem>() }
    private val matrix by lazy { getDisplayMetrics() }
    private val w by lazy { matrix.widthPixels }
    private val h by lazy { matrix.heightPixels }
    private var gDetector: GestureDetectorCompat? = null
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gDetector = GestureDetectorCompat(this, this)

        setupFilters()

    }

    override fun onTouchEvent(mE: MotionEvent): Boolean {
        gDetector?.onTouchEvent(mE)
        return super.onTouchEvent(mE)
    }

    override fun onShowPress(mE: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent?) = false

    override fun onDown(p0: MotionEvent?) = false

    override fun onFling(mE1: MotionEvent, mE2: MotionEvent, vx: Float, vy: Float): Boolean {
        val dx = mE2.x - mE1.x
        val dy = mE2.y - mE1.y
        if (abs(dx) > abs(dy) && abs(dx) > SDT && abs(dx) > SVT) {
            if (dx > 0 && index < imageFilters.size && index != imageFilters.size) ++index
            else if (index > 0) --index

            if (imageFilters.isNotEmpty() && index >= 0 && index < imageFilters.size) {
                val bmp = getPaintedBitmap(bitmap, imageFilters[index].processedBitmap, w, h)
                img_target.setImageBitmap(bmp)
            }
        }
        return true
    }

    override fun onScroll(mE1: MotionEvent, mE2: MotionEvent, dx: Float, dy: Float) = false

    override fun onLongPress(p0: MotionEvent) {}

    private fun setupFilters() {
        lifecycleScope.launchWhenStarted {
            val filters = createFilters()
            filters.forEach {
                var item: ImageFilterItem? = null
                withContext(Dispatchers.IO) {
                    // Create mutable bitmap for processing
                    val smallBitmap = Bitmap.createScaledBitmap(bitmap, w, h, false)
                    val mutableBitmap = smallBitmap.copy(bitmap.config, true)
                    val outputBitmap = it.filter.processFilter(mutableBitmap)
                    item = ImageFilterItem(it.name, outputBitmap, it.filter)
                }
                item?.let { imageFilters += it }
            }
            if (imageFilters.size > 0) {
                val bmp = getPaintedBitmap(bitmap, imageFilters[index].processedBitmap, w, h)
                img_target.setImageBitmap(bmp)
            }
        }
    }

}
