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
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var scanMode: String
    private lateinit var fridgeId: String
    private var isProcessingBarcode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        scanMode = intent.getStringExtra("mode") ?: "barcode"
        fridgeId = intent.getStringExtra("fridgeId") ?: run {
            Toast.makeText(this, "냉장고 ID 누락", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        findViewById<ImageView>(R.id.btn_capture).setOnClickListener {
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    if (scanMode == "barcode") {
                        processBarcodeImage(image)
                    } else {
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
                    if (!rawValue.isNullOrEmpty() && !isProcessingBarcode) {
                        isProcessingBarcode = true
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
        val call = ApiClient.foodBarcodeService.getFoodInfo(BuildConfig.FOOD_API_KEY, barcode)

        call.enqueue(object : Callback<FoodJsonResponse> {
            override fun onResponse(call: Call<FoodJsonResponse>, response: Response<FoodJsonResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@CameraActivity, "API 응답 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    return
                }

                val items = response.body()?.result?.row.orEmpty()
                Log.d("API_DEBUG", "응답 바코드 항목: ${items.joinToString { "${it.BRCD_NO}: ${it.PRDT_NM}" }}")

                val matchedItem = items.firstOrNull { it.BRCD_NO == barcode }

                if (matchedItem != null) {
                    val productName = matchedItem.PRDT_NM ?: matchedItem.PRDLST_NM ?: "이름없음"
                    val ingredient = Ingredient(
                        name = productName,
                        category = matchedItem.HRNK_PRDLST_NM ?: "기타",
                        storage = "냉장",
                        purchaseDate = getTodayString(),
                        expirationDate = getDefaultExpiration()
                    )
                    IngredientRepository.addIngredient(fridgeId, ingredient) { success ->
                        val msg = if (success) {
                            // 알림 예약 등록
                            WorkManagerScheduler.scheduleExpirationAlerts(
                                context = this@CameraActivity,
                                foodName = ingredient.name,
                                expirationDate = ingredient.expirationDate
                            )
                            "저장 완료: $productName"
                        } else {
                            "저장 실패"
                        }
                        Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CameraActivity, "일치하는 바코드 상품 없음", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FoodJsonResponse>, t: Throwable) {
                Toast.makeText(this@CameraActivity, "API 호출 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun getTodayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun getDefaultExpiration(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 5)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
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