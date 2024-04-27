package net.group_29.master.format.todotxt;

import android.text.InputFilter;
import android.text.Spanned;

import net.group_29.master.frontend.textview.TextViewUtils;

public class TodoTxtAutoTextFormatter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            if (start < source.length() && dstart <= dest.length() && TextViewUtils.isNewLine(source, start, end)) {
                return autoIndent(source);
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
        }
        return source;
    }

    /*private CharSequence autoIndent(CharSequence source) {
        return source + TodoTxtTask.DATEF_YYYY_MM_DD.format(new Date()) + " ";
    }*/
    private CharSequence autoIndent(CharSequence source) {
        // 使用一个小圆点符号代替日期
        String bulletPoint = "\u2022 "; // 这是Unicode编码下的黑色小圆点符号
        return source + bulletPoint;
    }
}

