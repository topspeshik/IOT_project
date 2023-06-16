package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.FragmentOpenCvBinding
import okhttp3.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.IOException


class OpenCvFragment : Fragment() {

    var pickedPhoto : Uri? = null
    var pickedBitMap : Bitmap? = null
    private lateinit var request: Request
    private val client = OkHttpClient()

    private var _binding: FragmentOpenCvBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOpenCvBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        OpenCVLoader.initDebug()
        binding.button.setOnClickListener {
            val url = "http://192.168.4.1/getdata"
//            val pixelArray = resizeImage(requireContext(), R.drawable.suriken)
            val pixelArray = resizeImage(requireContext())
            var message  = ""
            if (pixelArray != null) {
                for (row in pixelArray) {
                    for (pixel in row) {
                        message += "$pixel,"
                    }
                }
            }
            message = message.dropLast(1)
            Log.d("checkshitimg", message.substring(0, message.length/2))

            sendPostRequest(url, message)
        }
        binding.imageView.setOnClickListener{
            pickPhoto()
        }
    }

    fun pickPhoto(){
        if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1)
        } else {
            val galleriaIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleriaIntent,2)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galleriaIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleriaIntent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            pickedPhoto = data.data
            if (pickedPhoto != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver,pickedPhoto!!)
                    pickedBitMap = ImageDecoder.decodeBitmap(source)
                    binding.imageView.setImageBitmap(pickedBitMap)
                }
                else {
                    pickedBitMap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,pickedPhoto)
                    binding.imageView.setImageBitmap(pickedBitMap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    fun sendPostRequest(url: String, message: String) {
        val client = OkHttpClient()

        // Создаем тело запроса
        val requestBody = FormBody.Builder()
            .add("message", message)
            .build()

        // Создаем запрос типа POST
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Отправляем асинхронный запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()

                }
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Обработка успешного ответа
                response.body()?.let {
                    val responseData = it.string()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Successfully", Toast.LENGTH_SHORT).show()

                    }
                }
            }
        })
    }

    fun resizeImage(context: Context): Array<Array<String>> ? {

        val bitmap = pickedBitMap!!
//        val pixel123 = bitmap.getPixel(3, 3)
//        val red = Color.red(pixel123)
//        val green = Color.green(pixel123)
//        val blue = Color.blue(pixel123)
//        Log.d("MatString", red.toString())
//        Log.d("MatString", green.toString())
//        Log.d("MatString", blue.toString())
//        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val imageMat = Mat()
        Utils.bitmapToMat(bitmap, imageMat)

        Imgproc.resize(imageMat, imageMat, Size(16.0, 16.0))

        val pixels:Array<Array<String>> = Array(16) { Array(16){""} }

        for (i in 0 until 16) {
            for (j in 0 until 16) {
//                Log.d("MatString", imageMat.get(i, j)[0].toString())
                pixels[i][j]= imageMat.get(i, j)[1].toInt().toString() + " "  // rgb GBR
                pixels[i][j]+= imageMat.get(i, j)[2].toInt().toString()+" "  // r GBR
                pixels[i][j]+= imageMat.get(i, j)[0].toInt().toString()  // BGR GBR


            }
        }

        return pixels
    }
}