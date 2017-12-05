package com.example.bryan.odginformar.fragments;


import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.augumenta.agapi.AugumentaManager;
import com.augumenta.agapi.CameraFrameProvider;
import com.augumenta.agapi.HandPoseEvent;
import com.augumenta.agapi.HandPoseListener;
import com.augumenta.agapi.Poses;
import com.example.bryan.odginformar.MainActivity;
import com.example.bryan.odginformar.R;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends BaseFragment implements TextureView.SurfaceTextureListener {
    public static final String TAG = VideoFragment.class.getSimpleName();

    public CharSequence getTitle(){
        return TAG;
    }

    public VideoFragment() {
        super("Video Player Fragment");
        // Required empty public constructor
    }

    private MediaPlayer mediaPlayer;
    private TextureView mPreview;
    private Uri uri;
    private CameraFrameProvider provider;
    private SurfaceView mCameraPreview;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.video_fragment_layout, container, false);

        //Get the video name from the bundle and open the resource based on the file name
        String videoName = getArguments().getString("video_resource_name");
        int videoResource = getResources().getIdentifier(videoName, "raw", getContext().getPackageName());

        TextView textBanner = (TextView) v.findViewById(R.id.video_player_instructions);
        if(getArguments().getBoolean("isAR")){
            textBanner.setText("Thumbs up to exit");
        } else {
            textBanner.setText("Hit back button to exit");
        }

        uri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + videoResource);
        mPreview = (TextureView) v.findViewById(R.id.video_texture_player);
        mPreview.setSurfaceTextureListener(this);

        return v;
    }

    @Override
    public void onShown(){
        AugumentaManager detman = AugumentaManager.getInstance(getContext());
        detman.registerListener(quitPoseListener, Poses.THUMB_UP);
        mCameraPreview = (SurfaceView) getActivity().findViewById(R.id.truckster_camera_preview);
        if(provider == null){
            provider = (CameraFrameProvider) detman.getFrameProvider();
        }
        provider.setFastMode(false);
        provider.setFramesPerSecond(20);
        provider.setCameraPreview(mCameraPreview);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture){
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture){
        surfaceTexture.release();
        return true;
    }
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height){

    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
        Surface s = new Surface(surface);
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getContext(), uri);
            mediaPlayer.setSurface(s);
            mediaPlayer.prepare();
            mediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();
        } catch (IOException e){
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

        }
    };
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pop();
                }
            });
        }
    };
    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {

        }
    };
    MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {

        }
    };


    HandPoseListener quitPoseListener = new HandPoseListener() {
        @Override
        public void onDetected(HandPoseEvent handPoseEvent, boolean newdetection) {
            if(newdetection){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pop();
                    }
                });
            }
        }

        @Override
        public void onLost(HandPoseEvent handPoseEvent) {

        }

        @Override
        public void onMotion(HandPoseEvent handPoseEvent) {

        }
    };

    @Override
    public void onHide(){
        AugumentaManager.getInstance(getContext()).unregisterListener(quitPoseListener);
//        AugumentaManager.getInstance(getContext()).stop();
        if(provider.isRunning()){
            provider.stopPreview();
        }
        //mediaPlayer.stop();
        mediaPlayer.release();
        //provider.stop();
    }

}
