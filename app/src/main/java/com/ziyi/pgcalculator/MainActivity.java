package com.ziyi.pgcalculator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.lang.String;

public class MainActivity extends AppCompatActivity {

    private StringBuffer firstOperand = new StringBuffer();
    private StringBuffer secondOperand = new StringBuffer();
/*
    private char operatorType;
*/
    private String operator = "";
    private char state;

    private String mString_output = "";

    private int mScale = 10;

    private TextView mTextView_scale;
    private EditText mEditText_input;
    private TextView mTextView_output;

    private int[] mButtonOperands_id = {R.id.button_0, R.id.button_1, R.id.button_2,R.id.button_3,
            R.id.button_4, R.id.button_5, R.id.button_6,R.id.button_7,
            R.id.button_8, R.id.button_9, R.id.button_a,R.id.button_b,
            R.id.button_c, R.id.button_d, R.id.button_e,R.id.button_f,};
    
    private  int[] mButtonUnaryOperators_id = {R.id.button_lsh, R.id.button_rsh, R.id.button_not, R.id.button_equ};

    private int[] mButtonBinaryOperators_id = {R.id.button_add, R.id.button_sub, R.id.button_mul,
            R.id.button_div, R.id.button_or, R.id.button_xor, R.id.button_and, R.id.button_mod};

    private Button mButton_clean;
    private Button mButton_copy;
    private Button mButton_back;
    private Button mButton_equ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText_input = (EditText) findViewById(R.id.editText_input);
        mTextView_output = (TextView) findViewById(R.id.textView_output);

        mButton_clean = (Button)findViewById(R.id.button_clean);

        changeToDEC();

        Button mButton_toHEX = (Button) findViewById(R.id.button_toHEX);
        mButton_toHEX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToHEX();
            }
        });

        Button mButton_toDEC = (Button) findViewById(R.id.button_toDEC);
        mButton_toDEC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDEC();
            }
        });

        Button mButton_toOCT = (Button) findViewById(R.id.button_toOCT);
        mButton_toOCT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToOCT();
            }
        });

        Button mButton_toBIN = (Button) findViewById(R.id.button_toBIN);
        mButton_toBIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToBIN();
            }
        });

//        归零
        mButton_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText_input.setText("");
                mEditText_input.setTextSize(40);
                firstOperand = new StringBuffer();
                secondOperand = new StringBuffer();
                mTextView_output.setText("");
                mString_output = "";
                state = 'i';
                operator = "";
            }
        });

//        结果复制到剪贴板
        mButton_copy = (Button) findViewById(R.id.button_copy);
        mButton_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTextView_output.getText().toString().equals("")) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData;
                    clipData = ClipData.newPlainText("text", mTextView_output.getText().toString());
                    cm.setPrimaryClip(clipData);
                    Util.showToast(MainActivity.this, R.string.copy);
                } else {
                    Util.showToast(MainActivity.this, "请先得到一个计算结果");
                }
            }
        });

//        "="按钮方法
        mButton_equ = (Button) findViewById(R.id.button_equ);
        mButton_equ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("pgct", "equ click");
                if (!mTextView_output.getText().toString().equals("")) {
                    mEditText_input.setText(mTextView_output.getText());
                    mTextView_output.setText("");
                    Log.e("pgct", "output to input");
                }
            }
        });

//        退格
        mButton_back = (Button) findViewById(R.id.button_back);
        mButton_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("pgct(bk)", "button_back click");
                if (secondOperand.length() > 0) {
                    secondOperand.deleteCharAt(secondOperand.length() - 1);
                    mEditText_input.setText(firstOperand + operator + secondOperand);
                    if (secondOperand.length() > 0) {
                        mTextView_output.setText(getOutput());
                    } else if (secondOperand.length() == 0) {
                        mTextView_output.setText("");
                    }
                } else if (operator.length() > 0) {
                    operator = "";
                    mTextView_output.setText("");
                    mEditText_input.setText(firstOperand);
                } else if (firstOperand.length() > 0) {
                    firstOperand.deleteCharAt(firstOperand.length() - 1);
                    mEditText_input.setText(firstOperand);
                    Log.e("pgct(bk)", "firstOperand:" + firstOperand);
                }
            }
        });

//        初始状态，监听数字按键
        state = 'i';
        setButtonNumbersListener();
    }


//   计算器具体功能实现


    public String getFirstOperand() {
        return firstOperand.toString();
    }

    public String getOperator() {
        return operator;
    }

    public String getSecondOperand() {
        return secondOperand.toString();
    }

    /*得到计算结果*/
    public String getOutput() {
        long first, second, result;

        Log.e("pgct(op)", "mScale:" + mScale);
        Log.e("pgct(op)", "firstOperand:" + firstOperand);
        first = Long.parseLong(firstOperand.toString(), mScale);
        second = Long.parseLong(secondOperand.toString(), mScale);

        switch (getOperator()) {
            case "+":
                result = first + second;
                break;
            case "-":
                result = first - second;
                break;
            case "x":
                result = first * second;
                break;
            case "÷":
                if (second == 0) {
                    mString_output = "0不能作为除数!!!";
                    return mString_output;
                }
                else {
                    result = first / second;
                    break;
                }
            case "Mod":
                if (second == 0) {
                    mString_output = "0不能作为除数!!!";
                    return mString_output;
                }
                else
                    result = first % second;
                break;
            case "Or":
                result = first | second;
                break;
            case "Xor":
                result = first ^ second;
                break;
            case "And":
                result = first & second;
                break;
            default:
                result = 0;
        }

        switch (mScale) {
            case 16:
                mString_output = Long.toHexString(result);
                break;
            case 10:
                mString_output = String.valueOf(result);
                break;
            case 8:
                mString_output = Long.toOctalString(result);
                break;
            case 2:
                mString_output = Long.toBinaryString(result);
                break;
            default:
                mString_output = "mScale wrong!";
        }

        Log.e("pgct", String.valueOf(first) + operator + String.valueOf(second));
        Log.e("pgct", "output:" + mString_output);
        return mString_output;
    }

    /*设置数字按钮监听*/
    public void setButtonNumbersListener() {
        Log.e("pgct", "NumbersListener");
        for (int i = 0; i < mButtonOperands_id.length; i++) {
            Button button = (Button) findViewById(mButtonOperands_id[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (firstOperand.length() > 16 || secondOperand.length() > 16)
                        Util.showToast(MainActivity.this, R.string.unableInput);
                    else {
                        if (mEditText_input.length() >= 26)
                            mEditText_input.setTextSize(10);
                        else if (mEditText_input.length() > 18)
                            mEditText_input.setTextSize(20);
                        else if (mEditText_input.length() > 10)
                            mEditText_input.setTextSize(30);
                        Button button = (Button) findViewById(v.getId());
                        if (state == 'i') {
                            firstOperand.append(button.getText().toString());
                            mEditText_input.setText(firstOperand);
                            if (!firstOperand.toString().equals("")) {
                                setButtonOperatorsListener();
                                Log.e("pgct", "firstOperand(i):" + firstOperand);
                            }
                        }
                        else if (state == 's') {
                            Log.e("pgct", "firstOperand(o)" + firstOperand);
                            mEditText_input.setText(firstOperand.toString() + operator);
                            secondOperand.append(button.getText().toString());
                            Log.e("epg", secondOperand.toString());
                            mEditText_input.append(secondOperand.toString());
                            mTextView_output.setText(getOutput());
                        }
                    }
                }
            });
        }
    }

    /*设置操作符监听*/
    public void setButtonOperatorsListener() {
        Log.e("pgct", "OperatorsListener");
        for (int i = 0; i < mButtonUnaryOperators_id.length; i++) {
            Button button1 = (Button) findViewById(mButtonUnaryOperators_id[i]);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEditText_input.length() <= 40) {
                        switch (v.getId()) {
                            case R.id.button_lsh:
                                operator = "Lsh";
                                mEditText_input.setText("Lsh(" + mEditText_input.getText() + ")");
                                mString_output = Long.toBinaryString(Long.parseLong(firstOperand.toString(), 2) << 1);
                                break;
                            case R.id.button_rsh:
                                operator = "Rsh";
                                if (!mString_output.equals("0")) {
                                    mEditText_input.setText("Rsh(" + mEditText_input.getText() + ")");
                                    mString_output = Long.toBinaryString(Long.parseLong(firstOperand.toString(), 2) >> 1);
                                }
                                break;
                            case R.id.button_not:
                                operator = "Not";
                                mEditText_input.setText("Not(" + mEditText_input.getText() + ")");
                                mString_output = Long.toBinaryString(~Long.parseLong(firstOperand.toString(), 2));
                                break;
                        }
                        if (mEditText_input.length() > 16) {
                            mEditText_input.setTextSize(30);
                            if (mEditText_input.length() > 24) {
                                mEditText_input.setTextSize(22);
                                if (mEditText_input.length() > 32)
                                    mEditText_input.setTextSize(14);
                            }
                        }
                        mTextView_output.setText(mString_output);
                        firstOperand = new StringBuffer(mString_output);
                    }
                    else
                        Util.showToast(MainActivity.this, R.string.unableInput);
                }

            });
        }

        for (int i = 0; i < mButtonBinaryOperators_id.length; i++) {

            Button button2 = (Button) findViewById(mButtonBinaryOperators_id[i]);

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*operatorType = 'B';*/
                    if (!mTextView_output.getText().toString().equals("")) {
                        firstOperand = new StringBuffer(mTextView_output.getText().toString());
                    }
                    switch (v.getId()) {
                        case R.id.button_add:
                            operator = "+";
                            break;
                        case R.id.button_sub:
                            operator = "-";
                            break;
                        case R.id.button_mul:
                            operator = "x";
                            break;
                        case R.id.button_div:
                            operator = "÷";
                            break;
                        case R.id.button_mod:
                            operator = "Mod";
                            break;
                        case R.id.button_or:
                            operator = "Or";
                            break;
                        case R.id.button_xor:
                            operator = "Xor";
                            break;
                        case R.id.button_and:
                            operator = "And";
                            break;
                    }
                    mEditText_input.append(operator);

                    state = 's';
                    secondOperand = new StringBuffer();
                    Log.e("pgct", "state:" + state);
                }
            });
        }
    }



    /*进制转换*/

    public void changeToHEX() {
    /*   所有数值按键可用，包括'A','B'……   */
        for (int i = 0; i < mButtonOperands_id.length; i++) {
            Button button = (Button) findViewById(mButtonOperands_id[i]);
            button.setEnabled(true);
        }

        /*单元操作符不可用*/
        for (int i = 0; i < mButtonUnaryOperators_id.length - 1; i++) {
            Button button = (Button) findViewById(mButtonUnaryOperators_id[i]);
            button.setEnabled(false);
        }

        /*转换为十六进制*/
        if (firstOperand.length() > 0) {
            firstOperand = new StringBuffer(Long.toHexString(
                    Long.valueOf(firstOperand.toString(), mScale)));
            mEditText_input.setText(firstOperand.toString());
            if (!operator.equals("")) {
                mEditText_input.append(String.valueOf(operator));
                if (secondOperand.length() > 0) {
                    secondOperand = new StringBuffer(Long.toHexString(
                            Long.valueOf(secondOperand.toString(), mScale)));
                    mEditText_input.append(secondOperand);
                }
            }
        }
        if (!mString_output.equals("")) {
            mString_output = Long.toHexString(Long.valueOf(mString_output, mScale));
            mTextView_output.setText(mString_output);
        }

        mScale = 16;
        mTextView_scale = (TextView) (findViewById(R.id.textView_scale));
        mTextView_scale.setText("H");

    }

    public void changeToDEC() {
        /*A,B,C...按键不可用*/
        for (int i = 0; i < mButtonOperands_id.length; i++) {
            if (i < 10) {
                Button button = (Button) findViewById(mButtonOperands_id[i]);
                button.setEnabled(true);
            }
            else if (i >= 10) {
                Button button = (Button) findViewById(mButtonOperands_id[i]);
                button.setEnabled(false);
            }
        }

        /*转换为十进制*/
        for (int i = 0; i < mButtonUnaryOperators_id.length - 1; i++) {
            Button button = (Button) findViewById(mButtonUnaryOperators_id[i]);
            button.setEnabled(false);
        }

        if (firstOperand.length() > 0) {
            firstOperand = new StringBuffer(Long.valueOf(
                    firstOperand.toString(), mScale).toString());
            mEditText_input.setText(firstOperand.toString());
            if (!operator.equals("")) {
                mEditText_input.append(String.valueOf(operator));
                if (secondOperand.length() > 0) {
                    secondOperand = new StringBuffer(Long.valueOf(
                            secondOperand.toString(), mScale).toString());
                    mEditText_input.append(secondOperand);
                }
            }
        }

        if (!mString_output.equals("")) {
            mString_output = String.valueOf(Long.valueOf(mString_output, mScale));
            mTextView_output.setText(mString_output);
        }
        mScale = 10;
        mTextView_scale = (TextView) (findViewById(R.id.textView_scale));
        mTextView_scale.setText("D");
    }

    public void changeToOCT() {

        for (int i = 0; i < mButtonOperands_id.length; i++) {
            if (i < 8) {
                Button button = (Button) findViewById(mButtonOperands_id[i]);
                button.setEnabled(true);
            }
            else if (i >= 8) {
                Button button = (Button) findViewById(mButtonOperands_id[i]);
                button.setEnabled(false);
            }
        }

         /*单元操作符不可用*/
        for (int i = 0; i < mButtonUnaryOperators_id.length - 1; i++) {
            Button button = (Button) findViewById(mButtonUnaryOperators_id[i]);
            button.setEnabled(false);
        }

        /*转换为八进制*/
        if (firstOperand.length() > 0) {
            firstOperand = new StringBuffer(Long.toOctalString(
                    Long.valueOf(firstOperand.toString(), mScale)));
            mEditText_input.setText(firstOperand.toString());
            if (!operator.equals("")) {
                mEditText_input.append(String.valueOf(operator));
                if (secondOperand.length() > 0) {
                    secondOperand = new StringBuffer(Long.toOctalString(
                            Long.valueOf(secondOperand.toString(), mScale)));
                    mEditText_input.append(secondOperand);

                }
            }
        }

        if (!mString_output.equals("")) {
            mString_output = Long.toOctalString(Long.valueOf(mString_output, mScale));
            mTextView_output.setText(mString_output);
        }
        mScale = 8;
        mTextView_scale = (TextView) (findViewById(R.id.textView_scale));
        mTextView_scale.setText("O");
    }
    
    public void changeToBIN(){
        for (int i = 2; i < mButtonOperands_id.length; i++) {
            Button button = (Button) findViewById(mButtonOperands_id[i]);
            button.setEnabled(false);
        }

        /*单元操作符可用*/
        for (int i = 0; i < mButtonUnaryOperators_id.length ; i++) {
            Button button = (Button) findViewById(mButtonUnaryOperators_id[i]);
            button.setEnabled(true);
        }

        /*转换为二进制*/
        Log.e("pgct(B)", "firstOperand:" + firstOperand);
        Log.e("pgct", "mScale:" + mScale);
        if (firstOperand.length() > 0) {
            firstOperand = new StringBuffer(Long.toBinaryString(Long.valueOf(
                    firstOperand.toString(), mScale)));
            mEditText_input.setText(firstOperand.toString());
            if (!operator.equals("")) {
                mEditText_input.append(String.valueOf(operator));
                if (secondOperand.length() > 0) {
                    secondOperand = new StringBuffer(Long.toBinaryString(
                            Long.valueOf(secondOperand.toString(), mScale)));
                    mEditText_input.append(secondOperand);
                }
            }
        }

        if (!mString_output.equals("")) {
            mString_output = Long.toBinaryString(Long.valueOf(mString_output, mScale));
            mTextView_output.setText(mString_output);
        }

        mScale = 2;
        mTextView_scale = (TextView) (findViewById(R.id.textView_scale));
        mTextView_scale.setText("B");
    }

}
