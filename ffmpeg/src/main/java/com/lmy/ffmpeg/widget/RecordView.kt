package com.lmy.ffmpeg.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import com.lmy.ffmpeg.CameraWrapper
import com.lmy.ffmpeg.codec.VideoEncoder

/**
 * Created by limin on 2018/2/6.
 */
class RecordView : ScalableTextureView2, Camera.PreviewCallback, TextureView.SurfaceTextureListener,
        View.OnTouchListener {

    companion object {
        private val TAG = "RecordView"
    }

    private var mOutPath: String? = null
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mCameraWrapper: CameraWrapper? = null
    private var mVideoEncoder: VideoEncoder? = null
    private var mAvailable = false
    private var count = 0
    private var start = false

    constructor(context: Context) : super(context) {
        apply(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        apply(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        apply(attrs, 0)
    }

    private fun apply(attrs: AttributeSet?, defStyleAttr: Int) {
        surfaceTextureListener = this
        setOnTouchListener(this)
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p1?.action) {
            MotionEvent.ACTION_UP -> {
                mCameraWrapper?.focus(0.5f, 0.5f, Camera.AutoFocusCallback { p0, p1 -> })
            }
        }
        return true
    }

    override fun onPreviewFrame(data: ByteArray?, p1: Camera?) {
        if (!start) return
        Log.v(TAG, "onPreviewFrame: $count")
        ++count
        mVideoEncoder?.encode2(data)
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, width: Int, height: Int) {
        Log.v(TAG, "onSurfaceTextureSizeChanged: ${width}x$height")
        mCameraWrapper?.stopPreview()
        preview(p0)
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        mCameraWrapper?.stopCamera()
        mVideoEncoder?.flush2()
        return true
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, width: Int, height: Int) {
        Log.v(TAG, "onSurfaceTextureAvailable: ${width}x$height")
        mAvailable = true
        initCamera()
    }

    private fun initCamera() {
        if (!mAvailable || TextUtils.isEmpty(mOutPath) || mVideoWidth < 1 || mVideoHeight < 1) return
        mCameraWrapper = CameraWrapper.build(mVideoHeight, mVideoWidth)
        mVideoEncoder = VideoEncoder.build(mOutPath,
                mCameraWrapper!!.getCameraSize().width, mCameraWrapper!!.getCameraSize().height,
                mVideoWidth, mVideoHeight)
        mCameraWrapper?.setPreviewCallback(this)
        preview(surfaceTexture)
        updateViewSize(ImageView.ScaleType.CENTER,
                mCameraWrapper!!.getCameraSize().height / mCameraWrapper!!.getCameraSize().width.toFloat(),
                mVideoWidth, mVideoHeight)
    }

    private fun preview(texture: SurfaceTexture?) {
        if (null != texture)
            mCameraWrapper?.startPreview(texture)
    }

    fun setVideoInfo(outPath: String, width: Int, height: Int) {
        mOutPath = outPath
        mVideoWidth = width
        mVideoHeight = height
        initCamera()
    }

    fun start() {
        start = true
    }

    fun pause() {
        start = false
    }
}