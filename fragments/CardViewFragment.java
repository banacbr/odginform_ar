package com.example.bryan.odginformar.fragments;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.augumenta.agapi.AugumentaManager;
import com.augumenta.agapi.CameraFrameProvider;
import com.augumenta.agapi.HandPath;
import com.augumenta.agapi.HandPathEvent;
import com.augumenta.agapi.HandPathListener;
import com.augumenta.agapi.HandPose;
import com.augumenta.agapi.HandPoseEvent;
import com.augumenta.agapi.HandPoseListener;
import com.augumenta.agapi.HandTransitionEvent;
import com.augumenta.agapi.HandTransitionListener;
import com.augumenta.agapi.Poses;
import com.example.bryan.odginformar.MainActivity;
import com.example.bryan.odginformar.R;
import com.example.bryan.odginformar.utils.ContentCardViewObject;
import com.example.bryan.odginformar.utils.CustomLayoutManager;
import com.example.bryan.odginformar.utils.EquipmentCardObject;
import com.example.bryan.odginformar.utils.GeneralCardAdapter;
import com.example.bryan.odginformar.utils.TroubleshootingCardViewObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.View.GONE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CardViewFragment extends BaseFragment {
    private static final String TAG = CardViewFragment.class.getSimpleName();
    private static final String PACKAGE_NAME = CardViewFragment.class.getPackage().getName();
    private String[] devices;
    private String model;

    private final static int TYPE_CONTENT_CARD=1, TYPE_TROUBLESHOOTING_CARD=4, TYPE_EQUIPMENT_CARD = 5;

    private AugumentaManager detman;

    private RecyclerView mRecyclerView;
    private RecyclerView.OnItemTouchListener disabler = new RecyclerViewDisabler();
    private GeneralCardAdapter mAdapter;
    private CustomLayoutManager mLayoutManager;
    private SurfaceView mCameraPreview;
    private CameraFrameProvider provider;
    // Data sets and different card layout objects, plus an identifier to let us know what type of
    // card object we are using
    private ArrayList myDataset;
    int position;
    private FrameLayout progressBarHolder;
    private ProgressBar progressBar;
    private TextView holdForVideo;
    //A tick variable to keep our selection from executing too many in a row
    private int tick = 0;
    //A timestamp to reduce false swipes
    private long timeStamp;
    //For sound effects
    private MediaPlayer mp;
    // Override voice commands


    @Override
    public CharSequence getTitle(){
        return TAG;
    }
    public CardViewFragment() {
        super("CardView");
        // Required empty public constructor
    }

    @Override
    public void onShown(){
        super.onShown();
        Log.d(TAG, "card shown");
        detman = AugumentaManager.getInstance(getActivity());

        mCameraPreview = (SurfaceView) getActivity().findViewById(R.id.truckster_camera_preview);
        if(provider == null){
            Log.d(TAG, "Frame Provider created from Augumenta manager");
            provider = (CameraFrameProvider) detman.getFrameProvider();
        }
        provider.setFastMode(false);
        provider.setFramesPerSecond(20);
        provider.setCameraPreview(mCameraPreview);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new CustomLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        SnapHelper mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);

        progressBar.setMax(20);

        myDataset = new ArrayList<>();
        String dataType = getArguments().getString("menuSelection");
        try {
            switch(dataType){

                case "content":
                    myDataset.clear();
                    myDataset.addAll(ParseDataFromJSON(readJson("install_server_memory"), "content"));
                    break;
                case "troubleshooting":
                    myDataset.clear();
                    myDataset.addAll(ParseDataFromJSON(readJson("troubleshoot_fios_router"), "troubleshooting"));
                    break;
                case "equipment":
                    myDataset.clear();
                    myDataset.addAll(ParseDataFromJSON(readJson("equip_vuzix_m300"), "equipment"));
                    break;
                default:
                    myDataset.clear();
                    getActivity().finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //mRecyclerView.addOnItemTouchListener(disabler);
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                Log.d(TAG, e.toString());
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        sharedPreferences.edit().putBoolean("firstTimeInApp", false).apply();
//        model = sharedPreferences.getString("model", null);
        model = Build.MODEL;
        //Register new pose layout
        if(!Build.MODEL.equals("M300")){
            HandPath swipe_horizontal = new HandPath(new HandPose(Poses.M001), HandPath.Path.SWIPE_HORIZONTAL);
            HandPath swipe_vertical = new HandPath(new HandPose(Poses.M001), HandPath.Path.SWIPE_VERTICAL);
            detman.registerListener(mSwipeListener, swipe_horizontal);
            detman.registerListener(mVerticalSwipeListener, swipe_vertical);
            //detman.registerListener(voiceGesture, Poses.P002);
        }
        detman.registerListener(TroubleshootingConfirmPoseListener, Poses.P016, HandPose.HandSide.ALL); //Thumb down
        detman.registerListener(TroubleshootingConfirmPoseListener, Poses.P008, HandPose.HandSide.ALL); //Thumb up
        detman.registerListener(exitListener, Poses.P001, Poses.P032); // Open to close hand
        detman.registerListener(SelectingHandHoldPoseListener, Poses.P201, HandPose.HandSide.ALL); //Fist

        mAdapter = new GeneralCardAdapter(getContext());
        if(myDataset.size() > 0){
            //Log.d(TAG, String.valueOf(myDataset.size()));
            mAdapter.setmDataset(myDataset);
        } else {
            Log.d(TAG, "no content to load");
            getActivity().finish();
        }
        mRecyclerView.setAdapter(mAdapter);
        timeStamp = System.currentTimeMillis();
        mRecyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_UP){
                    position = mLayoutManager.findLastVisibleItemPosition();
                    ParseKeyControl(keyCode, position);
                    return true;
                }else {
                    return false;
                }

            }
        });

    }

    @Override
    public void onHide() {
        detman.unregisterAllListeners();
        if (provider.isRunning()) {
            provider.stopPreview();
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.swipe_recycler_view);
        progressBarHolder = (FrameLayout) view.findViewById(R.id.progressBarHolder);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        holdForVideo = (TextView) view.findViewById(R.id.hold_for_video);


        return view;
    }

    //Hand gesture for voice commands
    private HandPoseListener voiceGesture = new HandPoseListener() {
        @Override
        public void onDetected(HandPoseEvent handPoseEvent, boolean b) {
            if(b && handPoseEvent.separation > .60){
                BaseFragment voiceFrag = new VoiceServiceFragment();
                ((MainActivity)getActivity()).showVoiceOverlay(voiceFrag, true);
            }

        }

        @Override
        public void onLost(HandPoseEvent handPoseEvent) {

        }

        @Override
        public void onMotion(HandPoseEvent handPoseEvent) {

        }
    };

    //Our hand gesture to quit
    private HandTransitionListener exitListener = new HandTransitionListener() {
        @Override
        public void onTransition(HandTransitionEvent handTransitionEvent) {
            playSoundAndExit(getContext(), R.raw.button_click);
        }
    };

    //Sound effects methods
    private void playSoundAndExit(Context context, int soundID){
        mp = MediaPlayer.create(context, soundID);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.reset();
                mp.release();
                popRoot();
            }
        });
        mp.start();
    }

    private void playSound(Context context, int soundID){
        mp = MediaPlayer.create(context, soundID);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.stop();
                mp.release();
            }
        });
        mp.start();
    }

    private void ParseKeyControl(int KeyCode, final int position){
            switch (KeyCode) {
                case KEYCODE_ENTER:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeActions(position);
                        }
                    });
                    break;
                case KEYCODE_DPAD_UP:
                    Log.d(TAG, "keycode up");
                    if(mAdapter.getItemViewType(position) == TYPE_TROUBLESHOOTING_CARD && !mRecyclerView.isAnimating()) {
                        final TroubleshootingCardViewObject cvObject = (TroubleshootingCardViewObject) myDataset.get(position);
                        cvObject.setSuccessful(true);
                        cvObject.setCanContinue(true);
                        mAdapter.notifyItemChanged(position);
                        Handler newHandler = new Handler();
                        newHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pop();
                            }
                        }, 1000);
                    }
                    break;
                case KEYCODE_DPAD_RIGHT:
                    if(mAdapter.getItemViewType(position) == TYPE_TROUBLESHOOTING_CARD) {
                        final TroubleshootingCardViewObject cvObject = (TroubleshootingCardViewObject) myDataset.get(position);
                        if (cvObject.isCanContinue()) {
                            mRecyclerView.scrollToPosition(position + 1);
                        }
                    } else {
                        if(position != myDataset.size()){
                            mRecyclerView.scrollToPosition(position + 1);
                        }
                    }
                    break;

                case KEYCODE_DPAD_LEFT:
                    if(position != 0){
                       Log.d(TAG, "going to previous card");
                       mRecyclerView.scrollToPosition(position - 1);
                    }
                    break;
                case KEYCODE_DPAD_DOWN:
                    if(mAdapter.getItemViewType(position) == TYPE_TROUBLESHOOTING_CARD && !mRecyclerView.isAnimating()) {
                        final TroubleshootingCardViewObject cvObject = (TroubleshootingCardViewObject) myDataset.get(position);
                        cvObject.setSuccessful(false);
                        if (!cvObject.isCanContinue()) {
                            cvObject.setCanContinue(true);
                            mAdapter.notifyItemChanged(position);
                        }
                        Handler newHandler = new Handler();
                        newHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.scrollToPosition(position + 1);
                            }
                        }, 1000);
                    }
                    break;
                case KEYCODE_BACK:
                    popRoot();
                    getActivity().getSupportFragmentManager().popBackStack();
                    break;

            }
    }

    //Handposelistener thumbs-up to confirm action on card and enabled forward movement in Troubleshooting
    private HandPoseListener TroubleshootingConfirmPoseListener = new HandPoseListener() {
        final Handler handler = new Handler();
        long time1, time2;
        long diff = 0;
        boolean onDetect = false;

        @Override
        public void onDetected(final HandPoseEvent handPoseEvent, final boolean newdetection) {
            Log.d(TAG, "detected");
            if(newdetection){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        position = mLayoutManager.findLastVisibleItemPosition();
                        if (mAdapter.getItemViewType(position) == TYPE_TROUBLESHOOTING_CARD && !mRecyclerView.isAnimating()) {
                            final TroubleshootingCardViewObject cvObject = (TroubleshootingCardViewObject) myDataset.get(position);
                            switch (handPoseEvent.handpose.pose()) {
                                case Poses.P008: //Thumbs up
                                    cvObject.setSuccessful(true);
                                    if(!cvObject.isCanContinue()){
                                        cvObject.setCanContinue(true);
                                        mAdapter.notifyItemChanged(position);
                                    }
                                    Handler newHandler = new Handler();
                                    newHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            pop();
                                        }
                                    }, 1000);
                                    break;
                                case Poses.P016: //Thumbs down
                                    cvObject.setSuccessful(false);
                                    if (!cvObject.isCanContinue()) {
                                        cvObject.setCanContinue(true);
                                        mAdapter.notifyItemChanged(position);
                                    }
                                    newHandler = new Handler();
                                    newHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRecyclerView.scrollToPosition(position + 1);
                                        }
                                    }, 1000);
                                    break;
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onLost(HandPoseEvent handPoseEvent) {
            Log.d(TAG, "Lost");

        }

        @Override
        public void onMotion(HandPoseEvent handPoseEvent) {
            //nothing needed here
        }
    };

    //HandposeListener for selecting cards with selectable content or actions
    private HandPoseListener SelectingHandHoldPoseListener = new HandPoseListener() {
        boolean detected = false;
        boolean done = false;
        long time1;
        long time2;
        long diff;

        //TODO:  Refactor this to take advantage of the boolean "newdetection"
        @Override
        public void onDetected(final HandPoseEvent handPoseEvent, boolean newdetection) {
            if(newdetection){
                time1 = handPoseEvent.timestamp;
                progressBar.setProgress(0);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarHolder.setVisibility(View.VISIBLE);
                        progressBarHolder.bringToFront();
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                time2 = handPoseEvent.timestamp;
                diff = (time2 - time1)/100;
                final int i = (int) diff;
                progressBar.setProgress(i);
                if(progressBar.getProgress() == progressBar.getMax()){
                    done = true;
                    tick++;
                    if(tick == 1){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int mypos = ((LinearLayoutManager)mLayoutManager).findLastVisibleItemPosition();
                                executeActions(mypos);
                            }
                        });
                    }
                }

            }
        }

        @Override
        public void onLost(HandPoseEvent handPoseEvent) {
            detected = false;
            done = false;
            tick = 0;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(GONE);
                    progressBarHolder.setVisibility(GONE);
                }
            });
            progressBar.setProgress(0);
        }

        @Override
        public void onMotion(HandPoseEvent handPoseEvent) {

        }
    };

    private void executeActions(int position){
        Log.d(TAG, String.valueOf(mAdapter.getItemViewType(position)));
        switch(mAdapter.getItemViewType(position)){

            case TYPE_EQUIPMENT_CARD:
                EquipmentCardObject equipmentCardObject = (EquipmentCardObject) myDataset.get(position);
                Log.d("Equipment Card Object:", equipmentCardObject.getCardTitle());
                if(equipmentCardObject.getCardTitle().equals("Go back")){
                    pop();
                } else {
                if(!equipmentCardObject.getVideoResourceName().equals("0")){
                    BaseFragment newFragment = new VideoFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("video_resource_name", ((EquipmentCardObject) myDataset.get(position)).getVideoResourceName());
                    bundle.putBoolean("isAR", getArguments().getBoolean("isAR"));
                    newFragment.setArguments(bundle);
                    //Need to call the showfragment from truckster activity or else we get a casting exception
                    ((MainActivity) getActivity()).showFragment(newFragment, true);
                }else if((equipmentCardObject.getImageResource() != 0) && (equipmentCardObject.getVideoResourceName().equals("0"))){
                    final ImageView imageView = (ImageView) getView().findViewById(R.id.cv_image_large_view);
                    Drawable resource = getResources().getDrawableForDensity(equipmentCardObject.getImageResource(), DisplayMetrics.DENSITY_XXHIGH);
                    imageView.setImageDrawable(resource);
                    final View cv = (View) getView().findViewById(R.id.equip_fl);
                    Animation fadeOut = new AlphaAnimation(1,0);
                    final Animation fadeIn = new AlphaAnimation(0,1);
                    fadeOut.setDuration(250);
                    fadeIn.setDuration(250);
                    fadeOut.setFillAfter(true);
                    fadeIn.setFillAfter(true);
                    fadeIn.setStartOffset(250);
                    if(imageView.getVisibility() == View.VISIBLE){
                        imageView.setAnimation(fadeOut);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                imageView.setVisibility(GONE);
                                cv.startAnimation(fadeIn);

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        imageView.startAnimation(fadeOut);
                    } else {
                        cv.setAnimation(fadeOut);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setAnimation(fadeIn);
                                imageView.startAnimation(fadeIn);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        cv.startAnimation(fadeOut);
                    }
                    }
                }
                break;
            case TYPE_CONTENT_CARD:
                ContentCardViewObject contentCardViewObject = (ContentCardViewObject) myDataset.get(position);
                Log.d("Content Card Object:", contentCardViewObject.getCardTitle() );
                if(contentCardViewObject.getCardTitle().equals("Go back")){
                    pop();
                } else {
                    if(!contentCardViewObject.getVideoResourceName().equals("0")){
                        //If our video resource name isn't empty, there's obviously a video to be played
                        BaseFragment newFragment = new VideoFragment();
                        Bundle bundle = new Bundle();
                        //Put our video resource name into our bundle to pass to our video fragment
                        bundle.putString("video_resource_name", ((ContentCardViewObject) myDataset.get(position)).getVideoResourceName());
                        bundle.putBoolean("isAR", getArguments().getBoolean("isAR"));
                        newFragment.setArguments(bundle);
                        //Need to call the showfragment from truckster activity or else we get a casting exception
                        ((MainActivity) getActivity()).showFragment(newFragment, true);
                    }else if((contentCardViewObject.getImageResource() != 0)&&(contentCardViewObject.getVideoResourceName().equals("0"))){
                        final ImageView imageView = (ImageView) getView().findViewById(R.id.cv_image_large_view);
                        Drawable resource = getResources().getDrawableForDensity(contentCardViewObject.getImageResource(), DisplayMetrics.DENSITY_XXHIGH);
                        imageView.setImageDrawable(resource);
                        final CardView cv = (CardView) getView().findViewById(R.id.cv);
                        Animation fadeOut = new AlphaAnimation(1,0);
                        final Animation fadeIn = new AlphaAnimation(0,1);
                        fadeOut.setDuration(125);
                        fadeIn.setDuration(125);
                        fadeOut.setFillAfter(true);
                        fadeIn.setFillAfter(true);
                        fadeIn.setStartOffset(125);
                        if(imageView.getVisibility() == View.VISIBLE){
                            imageView.setAnimation(fadeOut);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    imageView.setVisibility(GONE);
                                    cv.startAnimation(fadeIn);

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            imageView.startAnimation(fadeOut);
                        } else {
                            cv.setAnimation(fadeOut);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    imageView.setVisibility(View.VISIBLE);
                                    imageView.setAnimation(fadeIn);
                                    imageView.startAnimation(fadeIn);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            cv.startAnimation(fadeOut);
                        }
                    }
                }
                break;
            case TYPE_TROUBLESHOOTING_CARD:
                TroubleshootingCardViewObject troubleshootingCardViewObject = (TroubleshootingCardViewObject) myDataset.get(position);
                if(troubleshootingCardViewObject.getCardTitle().equals("Go back")){ // This is a goback card, execute go back method
                    pop();
                } else {
                    if(!troubleshootingCardViewObject.getVideoResourceName().equals("0")){
                        //If our video resource name isn't empty, there's obviously a video to be played
                        BaseFragment newFragment = new VideoFragment();
                        Bundle bundle = new Bundle();
                        //Put our video resource name into our bundle to pass to our video fragment
                        bundle.putString("video_resource_name", ((TroubleshootingCardViewObject) myDataset.get(position)).getVideoResourceName());
                        bundle.putBoolean("isAR", getArguments().getBoolean("isAR"));
                        newFragment.setArguments(bundle);
                        //Need to call the showfragment from truckster activity or else we get a casting exception
                        ((MainActivity) getActivity()).showFragment(newFragment, true);
                    } else if((troubleshootingCardViewObject.getImageResource() != 0)&&(troubleshootingCardViewObject.getVideoResourceName().equals("0"))) {
                        final ImageView imageView = (ImageView) getView().findViewById(R.id.cv_image_large_view);
                        Drawable resource = getResources().getDrawableForDensity(troubleshootingCardViewObject.getImageResource(), DisplayMetrics.DENSITY_XXHIGH);
                        imageView.setImageDrawable(resource);
                        final CardView cv = (CardView) getView().findViewById(R.id.cv);
                        Animation fadeOut = new AlphaAnimation(1, 0);
                        final Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeOut.setDuration(125);
                        fadeIn.setDuration(125);
                        fadeOut.setFillAfter(true);
                        fadeIn.setFillAfter(true);
                        fadeIn.setStartOffset(125);
                        if (imageView.getVisibility() == View.VISIBLE) {
                            imageView.setAnimation(fadeOut);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    imageView.setVisibility(GONE);
                                    cv.startAnimation(fadeIn);

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            imageView.startAnimation(fadeOut);
                        } else {
                            cv.setAnimation(fadeOut);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    imageView.setVisibility(View.VISIBLE);
                                    imageView.setAnimation(fadeIn);
                                    imageView.startAnimation(fadeIn);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            cv.startAnimation(fadeOut);
                        }
                    }
                }
                break;
        }
    }
    private Handler handler;


    //Swipe vertical handler to scroll content card
    private HandPathListener mVerticalSwipeListener = new HandPathListener() {
        @Override
        public void onPath(final HandPathEvent handPathEvent) {
            Log.d(TAG, "Swiped");
            position = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
            if(mAdapter.getItemViewType(position) == TYPE_CONTENT_CARD || mAdapter.getItemViewType(position) == TYPE_EQUIPMENT_CARD) {
                View view = mRecyclerView.getLayoutManager().findViewByPosition(position);
                final NestedScrollView sv = (NestedScrollView) view.findViewById(R.id.cv_contenthtml_container);
                if (handPathEvent.motionY > 0.05) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sv.scrollTo(0, sv.getTop());
                        }
                    });
                } else if (handPathEvent.motionY < -0.05) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sv.scrollTo(0, sv.getBottom());
                        }
                    });
                }
            }
        }
    };

    //Swipe handler to swipe between all of the cards, detecting if it is a troubleshooting card with verification needed to continue
    private HandPathListener mSwipeListener = new HandPathListener() {
        @Override
        public void onPath(final HandPathEvent handPathEvent) {
            Log.d(TAG, "horizontal Swipe" + String.valueOf(handPathEvent.path));
            if((timeStamp + 800) < handPathEvent.timestamp){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int itemCount = mRecyclerView.getLayoutManager().getItemCount();
                        position = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        if(mAdapter.getItemViewType(position) == TYPE_TROUBLESHOOTING_CARD){
                            TroubleshootingCardViewObject cvObject = (TroubleshootingCardViewObject) myDataset.get(position);
                            if(cvObject.isCanContinue()) {
                                if (handPathEvent.motionX < -0.35) // swipe left
                                {
                                    if ((position - 1) >= 0) {
                                        mRecyclerView.smoothScrollToPosition(position - 1);
                                    } else {
                                        Toast.makeText(getActivity(), "Top of List", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (handPathEvent.motionX > 0.35) // swipe right
                                {
                                    //int position = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                                    if ((position + 1) < itemCount) {
                                        mRecyclerView.smoothScrollToPosition(position + 1);
                                    } else {
                                        Toast.makeText(getActivity(), "End of List", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {Toast.makeText(getContext(), "Need to verify to continue", Toast.LENGTH_SHORT).show();}
                        }  else {
                            if (handPathEvent.motionX < -0.33) // swipe left
                            {
                                if ((position - 1) >= 0) {
                                    mRecyclerView.smoothScrollToPosition(position - 1);
                                } else {
                                    Toast.makeText(getActivity(), "Top of List", Toast.LENGTH_SHORT).show();
                                }
                            } else if (handPathEvent.motionX > 0.33) // swipe right
                            {
                                if ((position + 1) < itemCount) {
                                    mRecyclerView.smoothScrollToPosition(position + 1);
                                } else {
                                    Toast.makeText(getActivity(), "End of List", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
                timeStamp = handPathEvent.timestamp;
            }
        }
    };

    public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e){
            return false;
        }
        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e){
        }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept){

        }
    }

    private Collection ParseDataFromJSON(String jsonFile, String dataType){
        try {
            JSONObject root = new JSONObject(jsonFile); //Convert raw json string into object
            JSONObject parsedObject = root.getJSONObject("content"); // grab the "content" object
            JSONObject data = parsedObject.getJSONObject("data"); //Grab the data object

            JSONArray jArray; // Initialize our JSON array object
            ArrayList arrayList = new ArrayList<>(); // Initialize object to iterate through json array

            switch(dataType.toLowerCase()){ // Switch based on the type of content in the json file
                case "content":
                    //JSONObject parsedObject = root.getJSONObject("content"); // grab the "content" object
                    //JSONObject data = parsedObject.getJSONObject("data"); //Grab the data object
                    if(data.has("steps")){ // find our nested array inside the data object
                        jArray = data.getJSONArray("steps");
                    } else {
                        jArray = data.getJSONArray("cards");
                    }

                    //Create an object to house our back button card
                    ContentCardViewObject initObject = new ContentCardViewObject();
                    initObject.setCardTitle("Go back");
                    initObject.setImageResource(R.drawable.ic_arrow_back_black_24dp);
                    initObject.setCardContent("Tap or make a fist to go back");
                    arrayList.clear(); // Make sure our iteration array is clear

                    //Iterate through data array
                    for(int i = 0; i < jArray.length(); i++){
                        JSONObject jsonObject = jArray.getJSONObject(i);
                        int imageResourceId, cardIndex;
                        String videoResourceString;
                        String title;
                        if(jsonObject.getString("image").equals("0")){ // If our image resource is empty, set to 0
                            imageResourceId = 0;
                        } else { // Otherwise, pull it from our resource folder (will have to change this when we actually make this a thing)
                            imageResourceId = getResources().getIdentifier(jsonObject.getString("image"), "mipmap", getContext().getPackageName());
                        }
                        if(jsonObject.getString("video").equals("0")){
                            videoResourceString = "0";
                        } else {
                            videoResourceString = jsonObject.getString("video");
                        }

                        if(jsonObject.has("stepTitle")){
                            title = jsonObject.getString("stepTitle");
                        } else {
                            title = jsonObject.getString("cardTitle");
                        }
                        if(jsonObject.has("stepNumber")){
                            cardIndex = jsonObject.getInt("stepNumber");
                        } else {
                            cardIndex = i;
                        }
                        //Initialize an object based on our object data
                        ContentCardViewObject cvObject = new ContentCardViewObject(
                                title, jsonObject.getString("text"),
                                imageResourceId, videoResourceString, cardIndex, jArray.length()
                        );
                        //Push object to data array
                        arrayList.add(cvObject);
                    }
                    //Push our back card to the end of the stack
                    arrayList.add(initObject);
                    return arrayList; //return the array
                case "troubleshooting": //Follows mostly the same as the content case

                    jArray = data.getJSONArray("steps");
                    TroubleshootingCardViewObject backObject = new TroubleshootingCardViewObject("Go back",
                            "Tap or make a fist to go back",
                            R.drawable.ic_arrow_back_black_24dp,
                            false,
                            getActivity().getPackageName(),
                            1, null);
                    arrayList.clear();

                    for(int i = 0; i < jArray.length(); i++){
                        JSONObject jsonObject = jArray.getJSONObject(i);
                        int imageResourceId;
                        if(jsonObject.getString("image").equals("0")){
                            if(!jsonObject.getString("video").equals("0")){
                                //Check if our video name is empty.  If not, set our image resource to the video placeholder image
                                //This is overridden if there actually is an image with the page
                                imageResourceId = R.drawable.video_placeholder;
                            } else {
                                imageResourceId = 0;
                            }
                        } else { //Pull image based on name from our mipmap resource folder
                            imageResourceId = getResources().getIdentifier(jsonObject.getString("image"), "mipmap", getContext().getPackageName());
                        }
                        TroubleshootingCardViewObject troubleshootingCardViewObject = new TroubleshootingCardViewObject(
                                jsonObject.getString("stepTitle"),
                                jsonObject.getString("text"),
                                imageResourceId,
                                Boolean.parseBoolean(jsonObject.getString("needsVerification")),
                                getContext().getPackageName(),
                                jArray.length(), jsonObject.getString("video"));
                        arrayList.add(troubleshootingCardViewObject);
                    }
                    arrayList.add(backObject);
                    return arrayList;
                case "equipment":
                    //JSONObject parsedObject = root.getJSONObject("content"); // grab the "equipment" object
                    //JSONObject data = parsedObject.getJSONObject("data"); //Grab the data object
                    jArray = data.getJSONArray("cards"); //Find the nested array within Data

                    //Create an object to house our back button card
                    EquipmentCardObject lastObject = new EquipmentCardObject();
                    lastObject.setCardTitle("Go back");
                    lastObject.setImageResource(R.drawable.ic_arrow_back_black_24dp);
                    lastObject.setCardContent("Tap or make a fist to go back");
                    arrayList.clear(); // Make sure our iteration array is clear

                    //Iterate through data array
                    for(int i = 0; i < jArray.length(); i++){
                        JSONObject jsonObject = jArray.getJSONObject(i);
                        int imageResourceId;
                        String videoResourceString;
                        if(jsonObject.getString("image").equals("0")){ // If our image resource is empty, set to 0
                            imageResourceId = 0;
                        } else { // Otherwise, pull it from our resource folder (will have to change this when we actually make this a thing)
                            imageResourceId = getResources().getIdentifier(jsonObject.getString("image"), "mipmap", getContext().getPackageName());
                        }
                        if(jsonObject.getString("video").equals("0")){
                            videoResourceString = "0";
                        } else {
                            videoResourceString = jsonObject.getString("video");
                        }

                        //Initialize an object based on our object data
                        EquipmentCardObject cvObject = new EquipmentCardObject(
                                jsonObject.getString("cardTitle"), jsonObject.getString("text"),
                                imageResourceId, videoResourceString, i, jArray.length()
                        );
                        //Push object to data array
                        arrayList.add(cvObject);
                    }
                    //Push our back card to the end of the stack
                    arrayList.add(lastObject);
                    return arrayList; //return the array
                default:
                    return null;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    private String readJson(String filename) throws IOException{

        String json;
        try{
            // Convert filename to the resource that is named by the string
            int file = getResources().getIdentifier(filename, "raw", getContext().getPackageName());
            //get our inputstream of the json file
            InputStream is = getResources().openRawResource(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
            return json;
    }


    //This class dumps the result of the JSON response to the JSONResult variable
    public class JsonTask extends AsyncTask<String, String, String> {
        String myType;
        public JsonTask(String type){
            myType = type;
        }

        protected void onPreExecute(){
            super.onPreExecute();
        }

        protected String doInBackground(String... params){

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try{
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null){
                    buffer.append(line+"\n");
                    //Log.d("Response:", "> " + line); //here we'll get the whole response
                }

                return buffer.toString();
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
                try {
                    if(reader != null){
                        reader.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            ParseDataFromJSON(result, myType);
            mAdapter.notifyDataSetChanged();
//            isHomeMenu = false;
        }
    }

}




