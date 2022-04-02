package com.github.tuuzed.dragbacklayout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.customview.widget.ViewDragHelper
import kotlin.math.absoluteValue
import kotlin.math.max

/**
 * 下拉拖拽关闭
 * <br/>
 * Activity需要设置透明主题
 * <br/>
 *
 * 透明主题例子：
 * ```xml
 * <style name="ThemeTranslucent">
 *  <item name="android:windowIsTranslucent">true</item>
 *  <item name="android:windowBackground">@android:color/transparent</item>
 * </style>
 * ```
 *
 * 用法：
 *
 * ```kotlin
 * class YourActivity : AppCompatActivity() {
 *    ...
 *    override fun onPostCreate(savedInstanceState: Bundle?) {
 *       super.onPostCreate(savedInstanceState)
 *       DragBackLayout(this).attachToActivity(this, object : DragBackLayout.Callback() {
 *           override fun onDrag(state: Int) {
 *               // do something
 *           }
 *           override fun onBack() {
 *               // do something
 *           }
 *       })
 *    }
 *    ...
 * }
 * ```
 */
class DragBackLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val DRAG_BEGIN = 1
        const val DRAG_END = 2
        const val DRAG_END_AND_BACK = 3
    }

    private var callback: Callback? = null
    private val helper = ViewDragHelper.create(this, ViewDragCallback())
    private var originWidth: Int = width
    private var originHeight: Int = height

    init {
        post {
            originWidth = width
            originHeight = height
        }
    }

    fun attachToActivity(activity: Activity, callback: Callback) {
        this.callback = callback
        val decorView = activity.window.decorView as ViewGroup
        val childView = decorView.getChildAt(0)
        decorView.removeView(childView)
        addView(childView)
        decorView.addView(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setBackgroundColor(Color.BLACK)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return helper.shouldInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        helper.processTouchEvent(event)
        return true
    }

    private inner class ViewDragCallback : ViewDragHelper.Callback() {

        private var beginDrag = false
        private var ratio = 1f

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (beginDrag) left else 0
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if (!beginDrag && top < 0) {
                return 0
            }
            if (!beginDrag && top > originHeight / 10) {
                callback?.onDrag(DRAG_BEGIN)
                beginDrag = true
            }
            ratio = (originHeight - top.absoluteValue) / originHeight.toFloat()
            val dim = ratio * 255
            setBackgroundColor(Color.argb(dim.toInt(), 0, 0, 0))
            child.animate().scaleX(ratio).scaleY(ratio).setDuration(0).start()
            return max(top, 0)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if (ratio <= 0.75f) {
                setBackgroundColor(Color.TRANSPARENT)
                releasedChild.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(100).start()
                callback?.onDrag(DRAG_END_AND_BACK)
                postDelayed({ callback?.onBack() }, 100)
            } else {
                releasedChild.animate().scaleX(1f).scaleY(1f).setDuration(0).start()
                requestLayout()
                callback?.onDrag(DRAG_END)
            }
            beginDrag = false
            ratio = 1f
        }
    }

    @IntDef(value = [DRAG_BEGIN, DRAG_END, DRAG_END_AND_BACK])
    @Retention(AnnotationRetention.SOURCE)
    annotation class DragState

    abstract class Callback {
        open fun onDrag(@DragState state: Int) {}
        open fun onBack() {}
    }

}