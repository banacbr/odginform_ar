package com.example.bryan.odginformar.fragments;


import android.support.v4.app.Fragment;

import com.example.bryan.odginformar.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {
    public final String tag;

    public BaseFragment(String tag) {
        // Required empty public constructor
        this.tag = tag;
    }

    public abstract CharSequence getTitle();

    @Override
    public void onResume(){
        super.onResume();

        //Trigger onShown if this fragment is shown
        if(getUserVisibleHint()){
            this.onShown();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        //Trigger onHide if this fragment is hidden
        if(getUserVisibleHint()){
            this.onHide();
        }
    }

    public void onShown(){
        //Log.d(tag, "onShown");
    }

    public void onHide(){
        //Log.d(tag, "onHide");
    }

    public void pop(){((MainActivity) getActivity()).popFragment(this);
    }

    public void popRoot(){
        ((MainActivity) getActivity()).popRootFragment();
    }

    public void showFragment(BaseFragment fragment){
        showFragment(fragment, true);
    }

    public void showFragment(BaseFragment fragment, boolean push_to_stack){
        ((MainActivity) getActivity()).showFragment(fragment, push_to_stack);
    }

    public void animateShowFragment(BaseFragment fragment, boolean push_to_stack, int enter, int exit, int popIn, int popOut){
        ((MainActivity) getActivity()).animateShowFragment(fragment, push_to_stack, enter, exit, popIn, popOut);
    }

    public void goBack(){
        ((MainActivity) getActivity()).popFragment(this);
    }

    public void goHome(){
        ((MainActivity) getActivity()).popRootFragment();
    }

}
