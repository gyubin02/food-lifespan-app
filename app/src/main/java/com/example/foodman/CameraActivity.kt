package com.example.foodman

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.util.Base64

import androidx.camera.core.ImageProxy
//import androidx.camera.core.ImageProxy.toBitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage


class CameraActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var scanMode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        scanMode = intent.getStringExtra("mode") ?: "barcode"

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        findViewById<ImageView>(R.id.btn_capture).setOnClickListener {
            if(scanMode=="barcode")
                takePhoto()
            else
                takePhoto2()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@이미지분석 함수@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    private fun takePhoto2() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    processFoodImage(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "촬영 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    // 1. ImageProxy -> Bitmap(JPEG) 변환
    @OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return when (imageProxy.format) {
            ImageFormat.JPEG -> {
                // JPEG은 plane이 1개뿐이므로 바로 bytes로 변환
                val buffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            ImageFormat.YUV_420_888 -> {
                val image = imageProxy.image ?: return null
                if (image.planes.size < 3) return null // 방어코드
                val yBuffer = image.planes[0].buffer
                val uBuffer = image.planes[1].buffer
                val vBuffer = image.planes[2].buffer

                val ySize = yBuffer.remaining()
                val uSize = uBuffer.remaining()
                val vSize = vBuffer.remaining()

                val nv21 = ByteArray(ySize + uSize + vSize)
                yBuffer.get(nv21, 0, ySize)
                vBuffer.get(nv21, ySize, vSize)
                uBuffer.get(nv21, ySize + vSize, uSize)

                val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
                val out = ByteArrayOutputStream()
                yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
                val imageBytes = out.toByteArray()
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }
            else -> null
        }
    }


    // 2. Bitmap -> Base64 변환
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val bytes = stream.toByteArray()
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }
    private fun resizeBitmap(src: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val aspectRatio = src.width.toFloat() / src.height
        var width = maxWidth
        var height = (width / aspectRatio).toInt()
        if (height > maxHeight) {
            height = maxHeight
            width = (height * aspectRatio).toInt()
        }
        return Bitmap.createScaledBitmap(src, width, height, true)
    }


    // 3. 전체 분석 프로세스 (ImageProxy → Map 결과)
    @OptIn(ExperimentalGetImage::class)
    private fun processFoodImage(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxyToBitmap(imageProxy)
            if (bitmap == null) {
                Toast.makeText(this, "이미지 변환 실패", Toast.LENGTH_SHORT).show()
                imageProxy.close()
                return
            }
            //이미지 파일 축소!!!!!!!!!!!!!!!!!!!!!!!!!!!
            val resizedBitmap = resizeBitmap(bitmap, 800, 800) // 원하는 크기로
            lifecycleScope.launch {
                try {
                    val base64Image = bitmapToBase64(resizedBitmap)
                    val result = ImageAnalysisRepository.analyzeImageAndGetExpiryDateMap(base64Image)
                    Log.d("FoodAnalysis", "서버 응답 결과: $result")
                    Toast.makeText(this@CameraActivity, "분석 완료", Toast.LENGTH_SHORT).show()
                    // 필요하다면 result(Map) 활용 코드 추가
                } catch (e: Exception) {
                    Log.e("FoodAnalysis", "서버 통신 에러: ${e.message}", e)
                    Toast.makeText(this@CameraActivity, "분석 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    imageProxy.close()
                }
            }
        } catch (e: Exception) {
            Log.e("FoodAnalysis", "이미지 처리 에러: ${e.message}", e)
            Toast.makeText(this@CameraActivity, "처리 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            imageProxy.close()
        }
    }


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    if (scanMode == "barcode") {
                        processBarcodeImage(image)
                    }
                    else {
                        Toast.makeText(this@CameraActivity, "해당 모드의 처리는 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "촬영 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processBarcodeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return imageProxy.close()
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode: Barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        fetchFoodNameFromApi(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "바코드 인식 실패", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun fetchFoodNameFromApi(barcode: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://openapi.foodsafetykorea.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(FoodBarcodeApi::class.java)
        val call = service.getFoodInfo(BuildConfig.FOOD_API_KEY, "I2570", 1, 5, barcode)

        call.enqueue(object : Callback<FoodJsonResponse> {
            override fun onResponse(call: Call<FoodJsonResponse>, response: Response<FoodJsonResponse>) {
                val productName = response.body()?.C005?.row?.firstOrNull()?.PRDLST_NM
                if (productName != null) {
                    val ingredient = Ingredient(
                        name = productName,
                        category = "기타",
                        storage = "냉장",
                        purchaseDate = getTodayString(),
                        expirationDate = getDefaultExpiration()
                    )
                    IngredientRepository.addIngredient(ingredient) { success ->
                        if (success) Toast.makeText(this@CameraActivity, "저장 완료: $productName", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this@CameraActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CameraActivity, "식품명을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FoodJsonResponse>, t: Throwable) {
                Toast.makeText(this@CameraActivity, "API 호출 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getDefaultExpiration(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 5)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(windowManager.defaultDisplay.rotation)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "카메라를 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
