package com.ziyi.pgcalculator;

import java.util.IdentityHashMap;

/**
 * Created by Ziyi on 2016/5/27.
 */
public class GetReult {
    public int GetReult(int first, char operator, int second, char scale) {
        int result;

        switch (operator) {
            case '+':
                result = first + second;
                break;
            case '-':
                result = first - second;
                break;
            case 'x':
                result = first * second;
                break;
            case '/':
                result = first / second;
                break;
            default:
                result = 0;
        }

        return result;
    }
}
