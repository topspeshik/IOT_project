package com.example.myapplication

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import codes.side.andcolorpicker.converter.toColorInt
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.group.registerPickers
import codes.side.andcolorpicker.hsl.HSLColorPickerSeekBar
import codes.side.andcolorpicker.model.IntegerHSLColor
import codes.side.andcolorpicker.view.picker.ColorSeekBar
import com.example.myapplication.databinding.FragmentDrawBinding
import com.example.myapplication.databinding.FragmentOpenCvBinding
import okhttp3.*
import java.io.IOException


class DrawFragment : Fragment() {

    private lateinit var matrix: Array<Array<View>>
    private val matrixSize = 16
    private lateinit var matrixState: Array<BooleanArray>
    private var colorMain = Color.BLACK

    private var _binding: FragmentDrawBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDrawBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setColorBar()
        setTableLayout()
        setButtonClickListener()

    }

    private fun setButtonClickListener(){
        binding.button.setOnClickListener{

            val url = "http://192.168.4.1/getdata"
            var message  = ""

            for (row in matrix) {
                for (pixel in row) {
                    val color = (pixel.background as ColorDrawable).color
                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    message += "$green $blue $red,"
                }
            }

            message = message.dropLast(1)
            Log.d("checkshitimg2", message)

            sendPostRequest(url, message)
        }

        binding.buttonClear.setOnClickListener {
            for (i in 0 until matrixSize) {
                for (j in 0 until matrixSize) {
                    matrix[i][j].setBackgroundColor(Color.WHITE)
                }

            }
        }
    }

    private fun setTableLayout(){
        val tableLayout = binding.tableLayout
        matrix = Array(matrixSize) { Array<View>(matrixSize) { View(requireContext()) } }
        matrixState = Array(matrixSize) { BooleanArray(matrixSize) }

        for (i in 0 until matrixSize) {
            val tableRow = TableRow(requireContext())
            tableRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT,
                1.0f
            )

            for (j in 0 until matrixSize) {
                matrix[i][j] = View(requireContext())
                matrix[i][j].layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1.0f
                )
                matrix[i][j].setBackgroundColor(Color.WHITE)

                matrix[i][j].setOnTouchListener { _, event ->
                    handleTouch(event, i, j)
                    true
                }

                tableRow.addView(matrix[i][j])
            }

            tableLayout.addView(tableRow)
        }
    }

    private fun handleTouch(event: MotionEvent, row: Int, col: Int) {
        val action = event.action
        val color = if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            colorMain
        } else {
            if (matrixState[row][col]) {
                colorMain
            } else {
                Color.WHITE
            }
        }

        matrix[row][col].setBackgroundColor(color)
        matrixState[row][col] = color == colorMain
    }

    private fun setColorBar(){
        binding.hueSeekBar.mode = HSLColorPickerSeekBar.Mode.MODE_HUE
        binding.hueSeekBar.coloringMode = HSLColorPickerSeekBar.ColoringMode.PURE_COLOR


        val group = PickerGroup<IntegerHSLColor>().also {
            it.registerPickers(
                binding.hueSeekBar
            )
        }
        group.addListener(
            object : ColorSeekBar.OnColorPickListener<ColorSeekBar<IntegerHSLColor>, IntegerHSLColor> {
                override fun onColorChanged(
                    picker: ColorSeekBar<IntegerHSLColor>,
                    color: IntegerHSLColor,
                    value: Int
                ) {
                    colorMain = color.toColorInt()
                    Log.d(
                        "checkcolor",
                        "${color.toColorInt()} picked"
                    )
                }

                override fun onColorPicked(
                    picker: ColorSeekBar<IntegerHSLColor>,
                    color: IntegerHSLColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                    //
                }

                override fun onColorPicking(
                    picker: ColorSeekBar<IntegerHSLColor>,
                    color: IntegerHSLColor,
                    value: Int,
                    fromUser: Boolean
                ) {
                    //
                }
            }
        )
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
                // Обработка ошибки
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
}