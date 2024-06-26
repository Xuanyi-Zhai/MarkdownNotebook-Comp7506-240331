package net.group_29.master.activity;


import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import net.group_29.master.R;
import net.group_29.opoc.frontend.base.GsFragmentBase;
import net.group_29.opoc.model.GsSharedPreferencesPropertyBackend;
import net.group_29.opoc.util.GsContextUtils;

public class MoreFragment extends GsFragmentBase<GsSharedPreferencesPropertyBackend, GsContextUtils> {
    public static final String FRAGMENT_TAG = "MoreFragment";

    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    public MoreFragment() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.more__fragment;
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MoreInfoFragment moreInfoFragment;
        if (_savedInstanceState == null) {
            FragmentTransaction t = getChildFragmentManager().beginTransaction();
            moreInfoFragment = MoreInfoFragment.newInstance();
            t.replace(R.id.more__fragment__placeholder_fragment, moreInfoFragment, MoreInfoFragment.TAG).commit();
        } else {
            moreInfoFragment = (MoreInfoFragment) getChildFragmentManager().findFragmentByTag(MoreInfoFragment.TAG);
        }
    }
}