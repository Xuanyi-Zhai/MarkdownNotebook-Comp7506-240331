package net.group_29.opoc.wrapper;

import android.text.Editable;
import android.text.TextWatcher;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class GsTextWatcherAdapter implements TextWatcher {
    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    }

    @Override
    public void afterTextChanged(final Editable s) {
    }

    public static TextWatcher before(final GsCallback.a4<CharSequence, Integer, Integer, Integer> impl) {
        return new GsTextWatcherAdapter() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                impl.callback(s, start, count, after);
            }
        };
    }

    public static TextWatcher on(final GsCallback.a4<CharSequence, Integer, Integer, Integer> impl) {
        return new GsTextWatcherAdapter() {
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                impl.callback(s, start, before, count);
            }
        };
    }

    public static TextWatcher after(final GsCallback.a1<Editable> impl) {
        return new GsTextWatcherAdapter() {
            public void afterTextChanged(final Editable s) {
                impl.callback(s);
            }
        };
    }
}
