package com.amr3d.preview.pro

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class STLRenderer : GLSurfaceView.Renderer {

    // --- Shaders (اختصرت النصوص الطويلة هنا، تقدر تحتفظ بنفس الكود الأصلي للـ shaders) ---
    private val vertexShaderCode = """ ... """
    private val fragmentShaderCode = """ ... """
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

    // --- نظام المواد ---
    enum class Material(val id: Int, val nameAr: String, val defaultColor: FloatArray) {
        PLASTIC(0, "بلاستيك", floatArrayOf(0.08f, 0.42f, 0.78f)),
        METAL  (1, "معدن",    floatArrayOf(0.78f, 0.78f, 0.82f)),
        WOOD   (2, "خشب",     floatArrayOf(0.55f, 0.32f, 0.12f)),
        MARBLE (3, "رخام",    floatArrayOf(0.90f, 0.88f, 0.85f)),
        COPPER (4, "نحاس",    floatArrayOf(0.80f, 0.45f, 0.15f)),
        CARBON (5, "كربون",   floatArrayOf(0.12f, 0.12f, 0.14f)),
        GOLD   (6, "ذهب",     floatArrayOf(0.95f, 0.72f, 0.04f)),
        RUBBER (7, "مطاط",    floatArrayOf(0.10f, 0.10f, 0.10f))
    }

    @Volatile var currentMaterial = Material.PLASTIC

    fun setMaterial(material: Material) {
        currentMaterial = material
        setModelColor(material.defaultColor[0], material.defaultColor[1], material.defaultColor[2])
    }

    fun setModelColor(r: Float, g: Float, b: Float) {
        modelColor = floatArrayOf(r, g, b, 1.0f)
    }

    fun getCurrentModelMatrix(): FloatArray = modelMatrix.copyOf()
    fun getCurrentViewMatrix(): FloatArray = viewMatrix.copyOf()
    fun getCurrentProjectionMatrix(): FloatArray = projectionMatrix.copyOf()
    fun getSurfaceWidth(): Int = surfaceWidth
    fun getSurfaceHeight(): Int = surfaceHeight

    private var currentModel: STLModel? = null
    fun getModel(): STLModel? = currentModel

    fun setModel(model: STLModel) {
        currentModel = model
        pendingModel = model
        modelCenter = floatArrayOf(
            (model.minBounds[0] + model.maxBounds[0]) / 2f,
            (model.minBounds[1] + model.maxBounds[1]) / 2f,
            (model.minBounds[2] + model.maxBounds[2]) / 2f
        )
        val dx = model.maxBounds[0] - model.minBounds[0]
        val dy = model.maxBounds[1] - model.minBounds[1]
        val dz = model.maxBounds[2] - model.minBounds[2]
        modelRadius = (maxOf(dx, dy, dz) / 2f).let { if (it <= 0f) 1f else it }
        rotationX = -25f; rotationY = 35f; scaleFactor = 1f; panX = 0f; panY = 0f
        measurementPoints.clear()
        updateProjection()
    }

    fun addMeasurementPoint(point: FloatArray) {
        measurementPoints.add(point)
        if (measurementPoints.size > 2) measurementPoints.removeAt(0)
    }

    fun clearMeasurementPoints() { measurementPoints.clear() }
    fun getMeasurementPoints(): List<FloatArray> = measurementPoints.toList()

    // --- إدارة الموارد ---
    fun release() {
        GLES20.glDeleteBuffers(3, vboIds, 0)
        if (meshProgram != 0) GLES20.glDeleteProgram(meshProgram)
        if (lineProgram != 0) GLES20.glDeleteProgram(lineProgram)
        vertexBuffer = null
        normalBuffer = null
        wireframeBuffer = null
        vboReady = false
    }

    // باقي الدوال: onSurfaceCreated, onSurfaceChanged, onDrawFrame, drawMesh, drawWireframe, drawSolidMesh, drawMeasurementOverlay, captureFrame, createProgram, loadShader
    // تقدر تحتفظ بنفس الكود الأصلي لها لأنها كانت صحيحة، فقط أضفنا الدوال الناقصة أعلاه.
}
