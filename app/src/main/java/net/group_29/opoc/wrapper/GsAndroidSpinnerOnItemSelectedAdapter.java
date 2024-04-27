package net.group_29.opoc.wrapper;

import android.view.View;
import android.widget.AdapterView;

public class GsAndroidSpinnerOnItemSelectedAdapter implements AdapterView.OnItemSelectedListener {

    private final GsCallback.a1<Integer> _callback;

    public GsAndroidSpinnerOnItemSelectedAdapter(final GsCallback.a1<Integer> callback) {
        _callback = callback;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        _callback.callback(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        _callback.callback(-1);
    }
}
