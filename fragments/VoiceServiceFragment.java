package com.example.bryan.odginformar.fragments;


import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bryan.odginformar.R;
import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class VoiceServiceFragment extends BaseFragment implements ISpeechRecognitionServerEvents {

    public static final String TAG = VoiceServiceFragment.class.getSimpleName();
    private TextView resultView;
    private ProgressBar spinner;
    int m_waitSeconds = 0;
    DataRecognitionClient dataClient = null;
    MicrophoneRecognitionClient micClient = null;
    FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;
    onVoiceResultsReturnedListener mListener;

    public interface onVoiceResultsReturnedListener {
        public void onVoiceResultsReturned(String response);
    }

    public void passResults(String results){
        mListener.onVoiceResultsReturned(results);
    }

    public CharSequence getTitle(){
        return TAG;
    }

    public enum FinalResponseStatus {NotReceived, OK, Timeout }

    /*
   * Gets the primary subscription key
    */
    public String getPrimaryKey(){
        return this.getString(R.string.primaryKey);
    }

    /**
     * For demo purposes, will just set this boolean function that checks if we want to use
     * the microphone to true
     */

    private boolean getUseMicrophone(){
        return true;
    }

    public VoiceServiceFragment() {
        // Required empty public constructor
        super("VoiceServiceFragment");
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        Activity a = null;
        if(context instanceof Activity){
            a=(Activity)context;
        }

        try{
            mListener = (onVoiceResultsReturnedListener)context;
        } catch (ClassCastException e){
            throw new ClassCastException(a.toString() + " must implement onVoiceResultsReturnedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getString(R.string.primaryKey).startsWith("Please")){
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage("Need a valid subscription key")
                    .setCancelable(false)
                    .show();
        }
        View view = inflater.inflate(R.layout.fragment_voice_service, container, false);
        resultView = (TextView) view.findViewById(R.id.ms_voice_service_response_text);
        spinner = (ProgressBar) view.findViewById(R.id.ms_voice_service_spinner);
        spinner.setIndeterminate(true);
        spinner.setMax(10);
        return view;
    }

    private void playSound(Context context, int soundID){
        final MediaPlayer mp = MediaPlayer.create(context, soundID);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.stop();
                mp.release();
            }
        });
        mp.start();
    }

    @Override
    public void onShown(){
        if(this.getUseMicrophone()){
            if(this.micClient == null){
                this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(getActivity(),
                        SpeechRecognitionMode.LongDictation,
                        "en-us",
                        this,
                        this.getPrimaryKey());
            }
            //playSound(getContext(), R.raw.gem_ping);
            this.micClient.startMicAndRecognition();
            resultView.setText(R.string.begin_speaking);
        }
    }

    @Override
    public void onHide(){
        this.micClient.endMicAndRecognition();
    }

    public void onFinalResponseReceived(final RecognitionResult response){
        boolean isFinalDictationMessage = (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if(null != this.micClient && this.getUseMicrophone() && isFinalDictationMessage){
            this.micClient.endMicAndRecognition();
        }
        if(isFinalDictationMessage){
            this.isReceivedResponse = FinalResponseStatus.OK;
            spinner.setVisibility(View.INVISIBLE);
        }

        if(!isFinalDictationMessage){
            Log.d(TAG, "********** Final n-Best Results ********");
            String resultString = "";
            if(response.Results.length > 0){
                Log.d(TAG, "Result String: " + response.Results[0].DisplayText);
                resultString = response.Results[0].DisplayText;
            } else {
                resultString = "";
            }

            String export = parseVoiceResults(resultString);
            if(export.equals("getPhoneNumber")){
                this.micClient.startMicAndRecognition();
            } else if(export.equals("quit")){
                playSound(getContext(), R.raw.button_click);
                getActivity().finish();
            } else if (export.equals("none")) {
                this.micClient.startMicAndRecognition();
            } else {
                this.micClient.endMicAndRecognition();
                //playSound(getContext(), R.raw.button_click);
                playSound(getContext(), R.raw.notification_sound);
                passResults(export);
            }
        }
    }

    /**
     * Called when the response is received and when its Intent is parsed
     */
    public void onIntentReceived(final String payload){
        //Log.d(TAG, payload);
    }


    public void onPartialResponseReceived(final String response){
        //Log.d(TAG, response);
        resultView.setText(response);
    }

    public void onError(final int errorCode, final String response){
        Log.d(TAG, "Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        Log.d(TAG, "Error text: " + response);
    }

    /**
     * Called when the microphone status has changed
     * @param recording The current recording state
     */

    public void onAudioEvent(boolean recording){
        if(recording){
            spinner.setVisibility(View.VISIBLE);
        }

        if(!recording){
            this.micClient.endMicAndRecognition();
            spinner.setVisibility(View.INVISIBLE);
        }
    }

    public String parseVoiceResults(final String result){
        this.micClient.endMicAndRecognition();
        String parsedResult = result.toLowerCase();
        Log.d(TAG, "Parsed voice response: " + parsedResult);

        Pattern pattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
        Matcher matcher = pattern.matcher(result);

        if(matcher.find()){
            Log.d(TAG, "Number: " + String.valueOf(matcher.group(0)));
            return matcher.group(0);
        }

        if(parsedResult.contains("content")){
            parsedResult = "content";
        } else if(parsedResult.contains("troubleshooting")){
            parsedResult = "troubleshooting";
        } else if(parsedResult.contains("dial") || parsedResult.contains("call") || matcher.find()){
            if(matcher.find()){
                Log.d(TAG, "PHONE NUMBER DETECTED: " + matcher.group(0));
                passResults(matcher.group(0));
            } else {
                resultView.setText(R.string.call_phone_number);
                return "getPhoneNumber";
            }
        } else if(parsedResult.contains("back")){
          parsedResult = "back";
        } else if(parsedResult.contains("quit") || parsedResult.contains("exit")){
            parsedResult = "quit";
        } else if (parsedResult.contains("cancel")) {
            parsedResult = "cancel";
        } else if (parsedResult.contains("scan")){
            parsedResult = "scanner";
        } else if(parsedResult.contains("equipment")){
            parsedResult = "equipment";
        } else {
            resultView.setText(R.string.failure_to_understand);
            parsedResult = "none";
        }
        Log.d(TAG, "exporting: " + parsedResult);
        return parsedResult;
    }
}
