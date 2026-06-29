package com.amr3d.preview.pro

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class STLRenderer : GLSurfaceView.Renderer {

    // --- Shaders ---
    private val vertexShaderCode = """ ... """ // نفس الكود الأصلي
    private val fragmentShaderCode = """ ... """ // نفس الكود الأصلي
    private val lineVertexShaderCode = """ ... """
    private val lineFragmentShaderCode = """ ... """

    private var meshProgram = 0
    private var lineProgram = 0

    private var vertexBuffer: FloatBuffer? = null
    private var normalBuffer: FloatBuffer? = null
    private var wireframeBuffer: FloatBuffer? = null
    private var wireframeVertexCount = 0
    private var vertexCountToDraw = 0

    private val vboIds = IntArray(3)
    private var vboReady = false
    private var pendingModel: STLModel? = null

    @Volatile var wireframeMode = false

    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    @Volatile var rotationX = -25f
    @Volatile var rotationY = 35f
    @Volatile var scaleFactor = 1f
    @Volatile var panX = 0f
    @Volatile var panY = 0f

    @Volatile var lightAngle = 45f
        set(value) { field = ((value % 360f) + 360f) % 360f }

    private var modelCenter = floatArrayOf(0f, 0f, 0f)
    private var modelRadius = 1f

    private val measurementPoints = CopyOnWriteArrayList<FloatArray>()

    private var surfaceWidth = 0
    private var surfaceHeight = 0

    var modelColor = floatArrayOf(0.45f, 0.75f, 0.95f, 1.0f)

    enum class Material(val id: Int, val
