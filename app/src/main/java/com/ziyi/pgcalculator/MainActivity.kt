package com.ziyi.pgcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import android.view.View
import android.widget.Button
import com.ziyi.pgcalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val mBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var firstOperand = StringBuffer()
    private var secondOperand = StringBuffer()

    /*
    private char operatorType;
*/
    private var operator = ""
    private var state = 0.toChar()
    private var mOutputStr = ""
    private var mScale = 10
    private val mButtonOperandIds = intArrayOf(
        R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
        R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7,
        R.id.button_8, R.id.button_9, R.id.button_a, R.id.button_b,
        R.id.button_c, R.id.button_d, R.id.button_e, R.id.button_f
    )
    private val mButtonUnaryOperatorIds =
        intArrayOf(R.id.button_lsh, R.id.button_rsh, R.id.button_not, R.id.button_equ)
    private val mButtonBinaryOperatorIds = intArrayOf(
        R.id.button_add, R.id.button_sub, R.id.button_mul,
        R.id.button_div, R.id.button_or, R.id.button_xor, R.id.button_and, R.id.button_mod
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.editTextInput.keyListener = null


        changeToDEC()
        mBinding.buttonToHEX.setOnClickListener { changeToHEX() }
        mBinding.buttonToDEC.setOnClickListener { changeToDEC() }
        mBinding.buttonToOCT.setOnClickListener { changeToOCT() }
        mBinding.buttonToBIN.setOnClickListener { changeToBIN() }

//        归零
        mBinding.buttonClean.setOnClickListener {
            mBinding.editTextInput.setText("")
            mBinding.editTextInput.textSize = 40f
            firstOperand = StringBuffer()
            secondOperand = StringBuffer()
            mBinding.textViewOutput.text = ""
            mOutputStr = ""
            state = 'i'
            operator = ""
        }

//        结果复制到剪贴板
        mBinding.buttonCopy.setOnClickListener {
            if (mBinding.textViewOutput.text.toString() != "") {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData: ClipData = ClipData.newPlainText("text", mBinding.textViewOutput.text.toString())
                cm.setPrimaryClip(clipData)
                Util.showToast(this@MainActivity, R.string.copy)
            } else {
                Util.showToast(this@MainActivity, "请先得到一个计算结果")
            }
        }

//        "="按钮方法
        mBinding.buttonEqu.setOnClickListener {
            Log.e("pgct", "equ click")
            if (mBinding.textViewOutput.text.toString() != "") {
                mBinding.editTextInput.setText(mBinding.textViewOutput.text)
                mBinding.textViewOutput.text = ""
                Log.e("pgct", "output to input")
            }
        }

//        退格
        mBinding.buttonBack.setOnClickListener {
            Log.e("pgct(bk)", "button_back click")
            if (secondOperand.isNotEmpty()) {
                secondOperand.deleteCharAt(secondOperand.length - 1)
                mBinding.editTextInput.setText(firstOperand.toString() + operator + secondOperand)
                if (secondOperand.isNotEmpty()) {
                    mBinding.textViewOutput.text = output
                } else if (secondOperand.isEmpty()) {
                    mBinding.textViewOutput.text = ""
                }
            } else if (operator.isNotEmpty()) {
                operator = ""
                mBinding.textViewOutput.text = ""
                mBinding.editTextInput.setText(firstOperand)
            } else if (firstOperand.isNotEmpty()) {
                firstOperand.deleteCharAt(firstOperand.length - 1)
                mBinding.editTextInput.setText(firstOperand)
                Log.e("pgct(bk)", "firstOperand:$firstOperand")
            }
        }

//        初始状态，监听数字按键
        state = 'i'
        setButtonNumbersListener()
    }

    //   计算器具体功能实现
    fun getFirstOperand(): String {
        return firstOperand.toString()
    }

    fun getSecondOperand(): String {
        return secondOperand.toString()
    }

    /*得到计算结果*/
    private val output: String
        get() {
            var result = 0L
            Log.e("pgct(op)", "mScale:$mScale")
            Log.e("pgct(op)", "firstOperand:$firstOperand")
            val first: Long = firstOperand.toString().toLong(mScale)
            val second: Long = secondOperand.toString().toLong(mScale)
            when (operator) {
                "+" -> result = first + second
                "-" -> result = first - second
                "x" -> result = first * second
                "÷" -> if (second == 0L) {
                    mOutputStr = "0不能作为除数!!!"
                    mOutputStr
                } else {
                    result = first / second
                }
                "Mod" -> if (second == 0L) {
                    mOutputStr = "0不能作为除数!!!"
                    return mOutputStr
                } else result = first % second
                "Or" -> result = first or second
                "Xor" -> result = first xor second
                "And" -> result = first and second
                else -> result = 0
            }
            mOutputStr = when (mScale) {
                16 -> java.lang.Long.toHexString(result).uppercase()
                10 -> result.toString()
                8 -> java.lang.Long.toOctalString(result)
                2 -> java.lang.Long.toBinaryString(result)
                else -> "mScale wrong!"
            }
            Log.e("pgct", first.toString() + operator + second.toString())
            Log.e("pgct", "output:$mOutputStr")
            return mOutputStr
        }

    /*设置数字按钮监听*/
    private fun setButtonNumbersListener() {
        Log.e("pgct", "NumbersListener")
        for (i in mButtonOperandIds.indices) {
            val button = findViewById<View>(mButtonOperandIds[i]) as Button
            button.setOnClickListener { v ->
                if (firstOperand.length > 16 || secondOperand.length > 16) Util.showToast(
                    this@MainActivity,
                    R.string.unableInput
                ) else {
                    if (mBinding.editTextInput.length() >= 26) mBinding.editTextInput.textSize =
                        10f else if (mBinding.editTextInput.length() > 18) mBinding.editTextInput.textSize =
                        20f else if (mBinding.editTextInput.length() > 10) mBinding.editTextInput.textSize =
                        30f
                    val button = findViewById<Button>(v.id)
                    if (state == 'i') {
                        firstOperand.append(button.text.toString())
                        mBinding.editTextInput.setText(firstOperand)
                        if (firstOperand.toString() != "") {
                            setButtonOperatorsListener()
                            Log.e("pgct", "firstOperand(i):$firstOperand")
                        }
                    } else if (state == 's') {
                        Log.e("pgct", "firstOperand(o)$firstOperand")
                        mBinding.editTextInput.setText(firstOperand.toString() + operator)
                        secondOperand.append(button.text.toString())
                        Log.e("epg", secondOperand.toString())
                        mBinding.editTextInput.append(secondOperand.toString())
                        mBinding.textViewOutput.text = output
                    }
                }
            }
        }
    }

    /*设置操作符监听*/
    private fun setButtonOperatorsListener() {
        Log.e("pgct", "OperatorsListener")
        for (i in mButtonUnaryOperatorIds.indices) {
            val button1 = findViewById<View>(mButtonUnaryOperatorIds[i]) as Button
            button1.setOnClickListener { v ->
                if (mBinding.editTextInput.length() <= 40) {
                    when (v.id) {
                        R.id.button_lsh -> {
                            operator = "Lsh"
                            mBinding.editTextInput.setText("Lsh(" + mBinding.editTextInput.text + ")")
                            mOutputStr = java.lang.Long.toBinaryString(
                                firstOperand.toString().toLong(2) shl 1
                            )
                        }
                        R.id.button_rsh -> {
                            operator = "Rsh"
                            if (mOutputStr != "0") {
                                mBinding.editTextInput.setText("Rsh(" + mBinding.editTextInput.text + ")")
                                mOutputStr = java.lang.Long.toBinaryString(
                                    firstOperand.toString().toLong(2) shr 1
                                )
                            }
                        }
                        R.id.button_not -> {
                            operator = "Not"
                            mBinding.editTextInput.setText("Not(" + mBinding.editTextInput.text + ")")
                            mOutputStr = java.lang.Long.toBinaryString(
                                firstOperand.toString().toLong(2).inv()
                            )
                        }
                    }
                    if (mBinding.editTextInput.length() > 16) {
                        mBinding.editTextInput.textSize = 30f
                        if (mBinding.editTextInput.length() > 24) {
                            mBinding.editTextInput.textSize = 22f
                            if (mBinding.editTextInput.length() > 32) mBinding.editTextInput.textSize = 14f
                        }
                    }
                    mBinding.textViewOutput.text = mOutputStr
                    firstOperand = StringBuffer(mOutputStr)
                } else Util.showToast(this@MainActivity, R.string.unableInput)
            }
        }
        for (i in mButtonBinaryOperatorIds.indices) {
            val button2 = findViewById<View>(mButtonBinaryOperatorIds[i]) as Button
            button2.setOnClickListener { v -> /*operatorType = 'B';*/
                if (mBinding.textViewOutput.text.toString() != "") {
                    firstOperand = StringBuffer(mBinding.textViewOutput.text.toString())
                }
                when (v.id) {
                    R.id.button_add -> operator = "+"
                    R.id.button_sub -> operator = "-"
                    R.id.button_mul -> operator = "x"
                    R.id.button_div -> operator = "÷"
                    R.id.button_mod -> operator = "Mod"
                    R.id.button_or -> operator = "Or"
                    R.id.button_xor -> operator = "Xor"
                    R.id.button_and -> operator = "And"
                }
                mBinding.editTextInput.append(operator)
                state = 's'
                secondOperand = StringBuffer()
                Log.e("pgct", "state:$state")
            }
        }
    }

    /*进制转换*/
    private fun changeToHEX() {
        /*   所有数值按键可用，包括'A','B'……   */
        for (i in mButtonOperandIds.indices) {
            val button = findViewById<View>(mButtonOperandIds[i]) as Button
            button.isEnabled = true
        }

        /*单元操作符不可用*/for (i in 0 until mButtonUnaryOperatorIds.size - 1) {
            val button = findViewById<View>(mButtonUnaryOperatorIds[i]) as Button
            button.isEnabled = false
        }

        /*转换为十六进制*/if (firstOperand.isNotEmpty()) {
            firstOperand = StringBuffer(
                java.lang.Long.toHexString(
                    java.lang.Long.valueOf(firstOperand.toString(), mScale)
                )
            )
            mBinding.editTextInput.setText(firstOperand.toString())
            if (operator != "") {
                mBinding.editTextInput.append(operator)
                if (secondOperand.isNotEmpty()) {
                    secondOperand = StringBuffer(
                        java.lang.Long.toHexString(
                            java.lang.Long.valueOf(secondOperand.toString(), mScale)
                        )
                    )
                    mBinding.editTextInput.append(secondOperand)
                }
            }
        }
        if (mOutputStr != "") {
            mOutputStr =
                java.lang.Long.toHexString(java.lang.Long.valueOf(mOutputStr, mScale))
            mBinding.textViewOutput.text = mOutputStr
        }
        mScale = 16
        mBinding.textViewScale.text = "H"
    }

    private fun changeToDEC() {
        /*A,B,C...按键不可用*/
        for (i in mButtonOperandIds.indices) {
            if (i < 10) {
                val button = findViewById<View>(mButtonOperandIds[i]) as Button
                button.isEnabled = true
            } else {
                val button = findViewById<View>(mButtonOperandIds[i]) as Button
                button.isEnabled = false
            }
        }

        /*转换为十进制*/for (i in 0 until mButtonUnaryOperatorIds.size - 1) {
            val button = findViewById<View>(mButtonUnaryOperatorIds[i]) as Button
            button.isEnabled = false
        }
        if (firstOperand.isNotEmpty()) {
            firstOperand = StringBuffer(
                java.lang.Long.valueOf(
                    firstOperand.toString(), mScale
                ).toString()
            )
            mBinding.editTextInput.setText(firstOperand.toString())
            if (operator != "") {
                mBinding.editTextInput.append(operator)
                if (secondOperand.isNotEmpty()) {
                    secondOperand = StringBuffer(
                        java.lang.Long.valueOf(
                            secondOperand.toString(), mScale
                        ).toString()
                    )
                    mBinding.editTextInput.append(secondOperand)
                }
            }
        }
        if (mOutputStr != "") {
            mOutputStr = java.lang.Long.valueOf(mOutputStr, mScale).toString()
            mBinding.textViewOutput.text = mOutputStr
        }
        mScale = 10
        mBinding.textViewScale.text = "D"

    }

    private fun changeToOCT() {
        for (i in mButtonOperandIds.indices) {
            if (i < 8) {
                val button = findViewById<View>(mButtonOperandIds[i]) as Button
                button.isEnabled = true
            } else {
                val button = findViewById<View>(mButtonOperandIds[i]) as Button
                button.isEnabled = false
            }
        }

        /*单元操作符不可用*/for (i in 0 until mButtonUnaryOperatorIds.size - 1) {
            val button = findViewById<View>(mButtonUnaryOperatorIds[i]) as Button
            button.isEnabled = false
        }

        /*转换为八进制*/if (firstOperand.isNotEmpty()) {
            firstOperand = StringBuffer(
                java.lang.Long.toOctalString(
                    java.lang.Long.valueOf(firstOperand.toString(), mScale)
                )
            )
            mBinding.editTextInput.setText(firstOperand.toString())
            if (operator != "") {
                mBinding.editTextInput.append(operator)
                if (secondOperand.isNotEmpty()) {
                    secondOperand = StringBuffer(
                        java.lang.Long.toOctalString(
                            java.lang.Long.valueOf(secondOperand.toString(), mScale)
                        )
                    )
                    mBinding.editTextInput.append(secondOperand)
                }
            }
        }
        if (mOutputStr != "") {
            mOutputStr =
                java.lang.Long.toOctalString(java.lang.Long.valueOf(mOutputStr, mScale))
            mBinding.textViewOutput.text = mOutputStr
        }
        mScale = 8
        mBinding.textViewScale.text = "O"
    }

    private fun changeToBIN() {
        for (i in 2 until mButtonOperandIds.size) {
            val button = findViewById<View>(mButtonOperandIds[i]) as Button
            button.isEnabled = false
        }

        /*单元操作符可用*/for (i in mButtonUnaryOperatorIds.indices) {
            val button = findViewById<View>(mButtonUnaryOperatorIds[i]) as Button
            button.isEnabled = true
        }

        /*转换为二进制*/Log.e("pgct(B)", "firstOperand:$firstOperand")
        Log.e("pgct", "mScale:$mScale")
        if (firstOperand.isNotEmpty()) {
            firstOperand = StringBuffer(
                java.lang.Long.toBinaryString(
                    java.lang.Long.valueOf(
                        firstOperand.toString(), mScale
                    )
                )
            )
            mBinding.editTextInput.setText(firstOperand.toString())
            if (operator != "") {
                mBinding.editTextInput.append(operator)
                if (secondOperand.isNotEmpty()) {
                    secondOperand = StringBuffer(
                        java.lang.Long.toBinaryString(
                            java.lang.Long.valueOf(secondOperand.toString(), mScale)
                        )
                    )
                    mBinding.editTextInput.append(secondOperand)
                }
            }
        }
        if (mOutputStr != "") {
            mOutputStr =
                java.lang.Long.toBinaryString(java.lang.Long.valueOf(mOutputStr, mScale))
            mBinding.textViewOutput.text = mOutputStr
        }
        mScale = 2
        mBinding.textViewScale.text = "B"
    }
}