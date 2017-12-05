package com.example.bryan.odginformar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.augumenta.agapi.AugumentaManager;
import com.example.bryan.odginformar.fragments.CardViewFragment;
import com.example.bryan.odginformar.fragments.MainMenuNonSurfaceFragment;
import com.example.bryan.odginformar.fragments.VoiceServiceFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.vuzix.sdk.barcode.ScanResult;
//import com.vuzix.sdk.barcode.ScannerIntent;

/** comment
 *
 */

/**
 * Created by Bryan on 9/6/2017.
 */

public class MainActivity extends FragmentActivity implements VoiceServiceFragment.onVoiceResultsReturnedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    //Permission request code for camera permission
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    //Permission request code for contacts (used in dialer)
    private static final int PERMISSION_REQUEST_CONTACTS = 0;
    //Permissions for voice control
    private static final int REQUEST_MICROPHONE = 0;
    private static final int PERMISSION_REQUEST_PHONE = 0;
    //Code for Barcode Scanner (Vuzix)
    private static final int REQUEST_CODE_SCAN = 0;

    //AugumentaManager instance
    private AugumentaManager mAugumentaManager;
    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;

    boolean isAR = false;

    //Our listener for voice arguments to be acted upon
    @Override
    public void onVoiceResultsReturned(String results) {
        Log.d(TAG, "Voice data results: " + results);
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getFragments().size() > 0) {
            fm.popBackStack();
        }
        Pattern pattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
        Matcher matcher = pattern.matcher(results);
        if (matcher.find()) {
            final String number = matcher.group(0);
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + number));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_PHONE);
            } else {
                startActivity(intent);
            }
        }
        mCurrentFragment = new CardViewFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAR", isAR);

        switch (results) {
            case "content":
                bundle.putString("menuSelection", "content");
                mCurrentFragment.setArguments(bundle);
                showFragment(mCurrentFragment, true);
                break;
            case "troubleshooting":
                bundle.putString("menuSelection", "troubleshooting");
                mCurrentFragment.setArguments(bundle);
                showFragment(mCurrentFragment, true);
                break;
            case "back":
                popFragment(mCurrentFragment);
                break;
            case "equipment":
                bundle.putString("menuSelection", "equipment");
                mCurrentFragment.setArguments(bundle);
                showFragment(mCurrentFragment, true);
                break;
            case "scanner":
                IntentIntegrator ii = new IntentIntegrator(this);
                ii.setBeepEnabled(false);
                ii.setBarcodeImageEnabled(false);
                ii.setPrompt("Scan a code");
                ii.setTimeout(4000);
                ii.initiateScan();
            default:
                Log.d(TAG, "Results received: " + results);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isAR = true;

        if (isAR) {
            Log.d(TAG, "is AR");
            Log.d(TAG, Build.MODEL.toString() + ", " + Build.DEVICE.toString() + " , " + Build.TYPE.toString());
            mCurrentFragment = new MainMenuNonSurfaceFragment();
            try {
                mAugumentaManager = AugumentaManager.getInstance(this);
            } catch (IllegalStateException e) {
                //Something went wrong when authenticating license
                Toast.makeText(this, "License error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "License error: " + e.getMessage());

                //Close the app before AugumentaManager is used, or else it
                //will throw an NullPointerException when trying to use it
                finish();
                return;
            }

            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

            mFragmentManager = getSupportFragmentManager();
            if (savedInstanceState == null) {
                showFragment(mCurrentFragment, false);
            }
        }
    }

    public void showFragment(Fragment fragment, boolean push_to_stack) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content, fragment, tag);
        Log.d(TAG, "replacing fragment");
        if (push_to_stack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

    public void showFragmentAllowingStateLoss(Fragment fragment, boolean push_to_stack) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content, fragment, tag);
        Log.d(TAG, "replacing fragment");
        if (push_to_stack) {
            ft.addToBackStack(tag);
        }
        ft.commitAllowingStateLoss();
    }

    public void showVoiceOverlay(Fragment fragment, boolean push_to_stack) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(0, R.anim.slide_up, R.anim.slide_down, R.anim.slide_up);
        ft.replace(R.id.voice_command_container, fragment, tag);
        if (push_to_stack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = findViewById(R.id.voice_command_container);
                view.bringToFront();
            }
        });
    }

    public void animateShowFragment(Fragment fragment, boolean push_to_stack, int enter, int exit, int popIn, int popOut) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(enter, exit, popIn, popOut);
        ft.replace(R.id.content, fragment, tag);
        if (push_to_stack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

    @Override
    public void onResume() {
        //Our method for when the app resumes
        super.onResume();
        Log.d(TAG, "MMSurfaceFragment Resume");

        //Check if the camera permission is already available
        if (isAR) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission("camera");
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission("contacts");
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission("phone");
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission("microphone");
            } else {
                Log.d(TAG, "CAMERA PERMISSIONS WERE GRANTED");
                //Camera permissions are available
                //Start our detection once app resumes
                startAugumentaManager();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission("contacts");
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission("phone");
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission("microphone");
            } else {
                Log.d(TAG, "PERMISSIONS WERE GRANTED");
                //Camera permissions are available
                //Start our detection once app resumes
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        //remember to stop the camera when application is closed, or else other applications wont
        //be able to use it
        //if(isAR){
        mAugumentaManager.unregisterAllListeners();
        mAugumentaManager.stop();
        //   mAugumentaManager.getFrameProvider().stop();
//            unregisterReceiver(mVoiceReceiver);
        //}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void popFragment(Fragment fragment) {
        mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void popRootFragment() {
        Log.d(TAG, "pop root fragment");
        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void requestPermission(String type) {
        //method to request permission to use the camera
        Log.d(TAG, "Requesting CAMERA permission");
        switch (type) {
            case "camera":
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                }
                break;
            case "contacts":
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CONTACTS);
                }
                break;
            case "phone":
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_PHONE);
                }
                break;
            case "microphone":
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
                }
        }
    }

    //Method to start app if required permissions are granted
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            Log.d(TAG, "Received CAMERA permission request");

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "CAMERA permissions granted, starting application");
                startAugumentaManager();
            } else {
                Log.d(TAG, "CAMERA permissions not granted, exiting application...");
                Toast.makeText(this, "CAMERA permissions were not granted", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startAugumentaManager() {
        //method to start detection
        if (!mAugumentaManager.start()) {
            //failed to start frame provider, probably failed to start camera
            Toast.makeText(this, "Failed to open camera!", Toast.LENGTH_LONG).show();
            //close the application
            finish();
        }
    }

    public void scanCode(Intent intent) {
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String resultString = result.getContents();
                mCurrentFragment = new CardViewFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isAR", isAR);
                switch (resultString) {
                    case "content":
                        bundle.putString("menuSelection", "content");
                        mCurrentFragment.setArguments(bundle);
                        showFragmentAllowingStateLoss(mCurrentFragment, true);
                        break;
                    case "troubleshoot":
                        bundle.putString("menuSelection", "troubleshooting");
                        mCurrentFragment.setArguments(bundle);
                        showFragmentAllowingStateLoss(mCurrentFragment, true);
                        break;
                    case "equipment":
                        bundle.putString("menuSelection", "equipment");
                        mCurrentFragment.setArguments(bundle);
                        showFragmentAllowingStateLoss(mCurrentFragment, true);
                        break;
                    case "M300":
                        bundle.putString("menuSelection", "equipment");
                        mCurrentFragment.setArguments(bundle);
                        showFragmentAllowingStateLoss(mCurrentFragment, true);
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}

