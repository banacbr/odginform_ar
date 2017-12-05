package com.example.bryan.odginformar.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.augumenta.agapi.AugumentaManager;
import com.augumenta.agapi.CameraFrameProvider;
import com.augumenta.agapi.HandPoseEvent;
import com.augumenta.agapi.HandPoseListener;
import com.augumenta.agapi.Poses;
import com.example.bryan.odginformar.AR_interaction_utils.PoseLayout;
import com.example.bryan.odginformar.MainActivity;
import com.example.bryan.odginformar.R;
import com.google.zxing.integration.android.IntentIntegrator;



/**
 * Created by Bryan on 10/25/2017.
 */

public class MainMenuNonSurfaceFragment extends BaseFragment {
    private static final String TAG = MainMenuNonSurfaceFragment.class.getSimpleName();
    private static final int REQUEST_CODE_SCAN = 0;

    //pose layout handles interaction with poses in layout
    private PoseLayout mPoseLayout;
    private CameraFrameProvider cameraFrameProvider;
    private SurfaceView thisCamera;
    private ImageButton imageButton;

    private MediaPlayer mp;
    private FrameLayout contentMenu, tsMenu, barcodeMenu, equipmentMenu;

    //private VoiceCmdReceiver mVoiceReceiver;
    //private VuzixSpeechClient sc;
    private Fragment fragmentToCompare;


    public MainMenuNonSurfaceFragment(){
        super("AR menu non surface");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.main_menu_non_surface_ar, container, false);

        mPoseLayout = (PoseLayout) rootView.findViewById(R.id.pose_layout);
        contentMenu = (FrameLayout) rootView.findViewById(R.id.menu_Content_choice);
        contentMenu.setOnClickListener(mMenuButtonOnClickListener);
        tsMenu = (FrameLayout) rootView.findViewById(R.id.menu_Troubleshooting_choice);
        tsMenu.setOnClickListener(mMenuButtonOnClickListener);
        barcodeMenu = (FrameLayout) rootView.findViewById(R.id.menu_barcode_choice);
        barcodeMenu.setOnClickListener(mMenuButtonOnClickListener);
        equipmentMenu = (FrameLayout) rootView.findViewById(R.id.menu_equipment_choice);
        equipmentMenu.setOnClickListener(mMenuButtonOnClickListener);

        imageButton = (ImageButton) rootView.findViewById(R.id.voice_command_button);
        //register poselayout poses

        fragmentToCompare = getFragmentManager().findFragmentById(R.id.content);


        return rootView;
    }

    @Override
    public void onShown(){
        super.onShown();

        if(Build.MODEL.equals("M300")){
//            imageButton.setVisibility(GONE);
//            try{
//                sc = new VuzixSpeechClient(getActivity());
//                setUpVoice(sc);
//                mVoiceReceiver = new VoiceCmdReceiver();
//                getActivity().registerReceiver(mVoiceReceiver, new IntentFilter(VuzixSpeechClient.ACTION_VOICE_COMMAND));
//            } catch (RemoteException e){
//
//            }
        } else {
            imageButton.setBackground(getResources().getDrawable(R.drawable.round_button));
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_black_24dp));
            imageButton.bringToFront();
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseFragment fragment = new VoiceServiceFragment();
                    ((MainActivity)getActivity()).showVoiceOverlay(fragment, true);
                }
            });
        }

        AugumentaManager detman = AugumentaManager.getInstance(this.getActivity());

        //Set our camera view
        thisCamera = (SurfaceView) getActivity().findViewById(R.id.hand_camera_layout);

        if(cameraFrameProvider == null)
        {
            Log.d(TAG, "Frame Provider created from Augumenta manager");
            cameraFrameProvider = (CameraFrameProvider) detman.getFrameProvider();
        }
        cameraFrameProvider.setFastMode(false);
        cameraFrameProvider.setFramesPerSecond(15);
        cameraFrameProvider.setCameraPreview(thisCamera);

        //Set to false to enable hover selecting
        mPoseLayout.registerPoses(detman, true);

        detman.registerListener(new HandPoseListener() {
            @Override
            public void onDetected(HandPoseEvent handPoseEvent, boolean newDetected) {
                Log.d(TAG, "detected");
                if(newDetected && handPoseEvent.separation > .60){
                    BaseFragment fragment = new VoiceServiceFragment();
                    ((MainActivity)getActivity()).showVoiceOverlay(fragment, true);
                }
            }

            @Override
            public void onLost(HandPoseEvent handPoseEvent) {

            }

            @Override
            public void onMotion(HandPoseEvent handPoseEvent) {

            }
        }, Poses.P002); //Search pose listener

    }

    @Override
    public void onHide(){
        super.onHide();

        //unregister poselayout poses
        AugumentaManager detman = AugumentaManager.getInstance(this.getActivity());
        mPoseLayout.unregisterPoses(detman, true);
        detman.unregisterAllListeners();

        if(cameraFrameProvider.isRunning()) {
            cameraFrameProvider.stopPreview();
        }
//        if(Build.MODEL.contains("M300") && (mVoiceReceiver != null)){
//            Log.d(TAG, "Destroying");
//            getActivity().unregisterReceiver(mVoiceReceiver);
//        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public CharSequence getTitle(){
        return TAG;
    }

    @Override
    public void goBack(){
        goHome();
    }

    private View.OnClickListener mMenuButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClicked: " + view.getId());
            BaseFragment fragment = new CardViewFragment();
            Bundle bundle = new Bundle();
            switch(view.getId()){
                case R.id.menu_Content_choice: // selected content demo menu choice
                    Log.d(TAG, "CONTENT CHOSEN");
                    bundle.putString("menuSelection", "content");
                    bundle.putBoolean("isAR", true);
                    fragment.setArguments(bundle);
                    showFragment(fragment, true);
                    playSoundAndTransition(getContext(), R.raw.button_click);
                    break;
                case R.id.menu_Troubleshooting_choice: //Troubleshooting demo selected
                    bundle.putString("menuSelection", "troubleshooting");
                    bundle.putBoolean("isAR", true);
                    fragment.setArguments(bundle);
                    showFragment(fragment, true);
                    playSoundAndTransition(getContext(), R.raw.button_click);
                    break;
                case R.id.menu_barcode_choice:
                    if(Build.MODEL.contains("M300")){
                        //Intent intent = new Intent(ScannerIntent.ACTION);
                        //((MainActivity)getActivity()).scanCode(intent);
                    } else {
                        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
                        intentIntegrator.setPrompt("Scan a code");
                        intentIntegrator.setBarcodeImageEnabled(false);
                        intentIntegrator.setBeepEnabled(false);
                        intentIntegrator.initiateScan();
                    }
                    break;
                case R.id.menu_equipment_choice: // Equipment demo selected
                    Log.d(TAG, "equipment selected");
                    bundle.putString("menuSelection", "equipment");
                    bundle.putBoolean("isAR", true);
                    fragment.setArguments(bundle);
                    showFragment(fragment, true);
                default:
                    break;
            }
        }
    };
    private void playSoundAndTransition(Context context, int soundID){
        mp = MediaPlayer.create(context, soundID);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
            }
        });
    }

//    private void setUpVoice(VuzixSpeechClient client){
//        try {
//            client.insertPhrase("troubleshooting");
//            client.insertPhrase("content");
//            client.insertPhrase("equipment");
//            client.insertPhrase("scan");
//            client.insertPhrase("exit");
//            client.deletePhrase("voice off");
//            client.deletePhrase("voice on");
//            client.insertPhrase("select");
//            client.insertPhrase("yes");
//            client.insertPhrase("no");
//            client.insertKeycodePhrase("yes", KEYCODE_DPAD_UP);
//            client.insertKeycodePhrase("no", KEYCODE_DPAD_DOWN);
//            client.insertKeycodePhrase("next card", KEYCODE_DPAD_RIGHT);
//            client.insertKeycodePhrase("previous card", KEYCODE_DPAD_LEFT);
//        } catch(Exception e){
//
//        }
//    }

//    public class VoiceCmdReceiver extends BroadcastReceiver {
//        private String cleanPhrase(String phrase){
//            if(phrase.contains("troubleshoot")){
//                return "troubleshooting";
//            } else if (phrase.contains("content")){
//                return "content";
//            } else if(phrase.contains("scan")){
//                return "scanner";
//            } else if(phrase.contains("voice")){
//                return "vt";
//            } else if(phrase.contains("exit")){
//                return "quit";
//            } else if(phrase.contains("equipment")){
//                return "equipment";
//            } else if(phrase.contains("back")){
//                return "go_back";
//            }
//            else {
//                return "cancel";
//            }
//        }

//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(VuzixSpeechClient.ACTION_VOICE_COMMAND)) {
//                String phrase = intent.getStringExtra("phrase");
//                Log.d(TAG, "PHRASE: " + phrase);
//                BaseFragment fragment = new CardViewFragment();
//                Bundle bundle = new Bundle();
//                String name = getFragmentManager().findFragmentById(R.id.content).getClass().getSimpleName();
//                if ((phrase != null) && name.equals("MainMenuNonSurfaceFragment")) {
//                    String myPhrase = this.cleanPhrase(phrase);
//                    switch(myPhrase){
//                        case "equipment":
//                            bundle.putString("menuSelection", "equipment");
//                            bundle.putBoolean("isAR", true);
//                            fragment.setArguments(bundle);
//                            playSoundAndTransition(getContext(), R.raw.button_click);
//                            showFragment(fragment, true);
//                            break;
//                        case "scanner":
//                            //Intent scannerIntent = new Intent(ScannerIntent.ACTION);
//                            //((MainActivity)getActivity()).scanCode(scannerIntent);
//                            break;
//                        case "quit":
//                            getActivity().finish();
//                            break;
//                        case "troubleshooting":
//                            bundle = new Bundle();
//                            bundle.putString("menuSelection", "troubleshooting");
//                            bundle.putBoolean("isAR", true);
//                            fragment.setArguments(bundle);
//                            playSoundAndTransition(context, R.raw.button_click);
//                            showFragment(fragment, true);
//                            break;
//                        case "content":
//                            bundle.putString("menuSelection", "content");
//                            bundle.putBoolean("isAR", true);
//                            fragment.setArguments(bundle);
//                            playSoundAndTransition(getContext(), R.raw.button_click);
//                            showFragment(fragment, true);
//                            break;
//                        case "go_back":
//                            popRoot();
//                    }
//                }
//            }
//        }
//    }

}
