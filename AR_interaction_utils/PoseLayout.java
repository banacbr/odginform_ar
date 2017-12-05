package com.example.bryan.odginformar.AR_interaction_utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.augumenta.agapi.AugumentaManager;
import com.augumenta.agapi.HandPose;
import com.augumenta.agapi.HandPoseEvent;
import com.augumenta.agapi.HandPoseListener;
import com.augumenta.agapi.HandTransitionEvent;
import com.augumenta.agapi.HandTransitionListener;
import com.augumenta.agapi.Poses;
import com.example.bryan.odginformar.R;


/**
 * Created by Bryan on 10/25/2017.
 *
 * POSELAYOUT provides interaction capabilities for layout child views
 *
 * Child views can be "clicked" with P001 -> P032 transitions.  This may change as
 * I want to have it be timer based instead of "click" based
 *
 * They also have the ability to drag views, but I find this clunky, so I may not implement it
 */

public class PoseLayout extends PoseCursorLayout implements HandPoseListener {
    private static final String TAG = PoseLayout.class.getSimpleName();

    //Should child views be dragged or clicked
    private boolean mDraggable = true;

    //Should listeners be for a click or a hover (default: hover)
    private boolean shouldClick = false;

    // Poses used for hovering and dragging/clicking
    public int POSE_HOVER = Poses.P001;
    public int POSE_TOUCH = Poses.P032;

    public PoseLayout(Context context){
        this(context, null, -1);
    }

    public PoseLayout(Context context, AttributeSet attrs){
        this(context, attrs, -1);
    }

    public PoseLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PoseLayout, 0,0);
        try { //read draggable parameter from xml
            mDraggable = a.getBoolean(R.styleable.PoseLayout_draggable,mDraggable);
        } finally {
            a.recycle();
        }
    }

    // Register poses used for interactions
    public void registerPoses(AugumentaManager detman, boolean poseClick){
//        if(poseClick){
//            shouldClick = poseClick;
            detman.registerListener(touchDownListener, new HandPose(POSE_HOVER, HandPose.HandSide.ALL, 0, 90), new HandPose(POSE_TOUCH, HandPose.HandSide.ALL, 0, 90));
//        }
        detman.registerListener(this, POSE_HOVER, HandPose.HandSide.ALL, 0, 90);
        detman.registerListener(this, POSE_TOUCH, HandPose.HandSide.ALL, 0, 90);
    }

    // Unregister Pose Listeners
    public void unregisterPoses(AugumentaManager detman, boolean poseClick){
        if(poseClick){
            detman.unregisterListener(touchDownListener);
        }
        detman.unregisterListener(this);
    }

    // Previous pose, used for tracking changes
    private int mPreviousPose = 0;
    // previous position variables to detect hovering in place
    private boolean hovered = false;
    private boolean isHovering(int x, int y){
        //Log.d(TAG, "X: " + x + ", Y: " + y);
        return (Math.abs(x) < 20 && Math.abs(y) < 20);
    }


    // Move cursor view depending on the pose event
    private void moveCursor(HandPoseEvent event){
        // get event absolute position on the screen
        int x = (int) (margin.translateX((event.rect.centerX()) * getWidth()));
        int y = (int) (margin.translateY((event.rect.centerY()) * getHeight()));

        int pose = event.handpose.pose();

        //if(shouldClick){
            if(pose == POSE_TOUCH && event.separation > .75){
                if(mPreviousPose == POSE_HOVER){
                    mCursorDeltaX = mCursorX - x;
                    mCursorDeltaY = mCursorY - y;
                }
                x += mCursorDeltaX;
                y += mCursorDeltaY;
            }
//        } else {
//            if(pose == POSE_HOVER){
//                mCursorDeltaX = mCursorX - x;
//                mCursorDeltaY = mCursorY - y;
//
//
//
//                x += mCursorDeltaX;
//                y += mCursorDeltaY;
//
//                //Log.d(TAG, "x: " + Math.abs(x - mCursorDeltaX) + ", y: " + Math.abs(y - mCursorDeltaY));
//
//            }
//
//        }

        mPreviousPose = pose;
        mCursorX = x;
        mCursorY = y;
        //Log.d(TAG, x + ", " + y);
    }

    // Dispatch a click event at the cursor position
    private void dispatchClickEvent(){
        Log.d(TAG, "Dispatch click: " + mCursorX + ", " + mCursorY);
        long now = SystemClock.uptimeMillis();
        dispatchTouchEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, mCursorX, mCursorY, 0));
        dispatchTouchEvent(MotionEvent.obtain(now, now + 1, MotionEvent.ACTION_UP, mCursorX, mCursorY, 0));
    }

    // time when touch was started
    private long touch_event_time = -1;

    // Dispatch touch down event at the cursor position
    private void touchDownEvent(){
        final long now = SystemClock.uptimeMillis();
        //if(shouldClick){
            if(touch_event_time < 0){
                touch_event_time = now;
                dispatchTouchEvent(MotionEvent.obtain(touch_event_time, now, MotionEvent.ACTION_DOWN, mCursorX, mCursorY, 0));
            }  else  {
                dispatchTouchEvent(MotionEvent.obtain(touch_event_time, now, MotionEvent.ACTION_MOVE, mCursorX, mCursorY, 0));
            }
//        } else {
//            if(touch_event_time < 0){
//                Log.d(TAG, "Initial touch down event, " + touch_event_time);
//                touch_event_time = now;
//                dispatchTouchEvent(MotionEvent.obtain(touch_event_time, now, MotionEvent.ACTION_DOWN, mCursorX, mCursorY, 0));
//                Log.d(TAG, "Initial touch tick: " + tick );
//            } else if(tick > 700){
//                Log.d(TAG, "firing touch up event");
//                touchUpEvent();
//            } else  {
//                Log.d(TAG, "touch move event, tick:" + tick);
//                dispatchTouchEvent(MotionEvent.obtain(touch_event_time, now, MotionEvent.ACTION_MOVE, mCursorX, mCursorY, 0));
//                if(isHovering(mCursorDeltaX, mCursorDeltaY)){
//                    tick = SystemClock.uptimeMillis() - touch_event_time;
//                }
//            }
//        }

    }

    // Dispatch touch up event at the cursor position
    private void touchUpEvent(){
        final long now = SystemClock.uptimeMillis();
        dispatchTouchEvent(MotionEvent.obtain(touch_event_time, now, MotionEvent.ACTION_UP, mCursorX, mCursorY, 0));
        touch_event_time = -1;
        tick = 0;
    }

    //Dispatch touch cancel event
    private void touchCancelEvent(){
        final long now = SystemClock.uptimeMillis();
        dispatchTouchEvent(MotionEvent.obtain(touch_event_time, now, MotionEvent.ACTION_CANCEL, mCursorX, mCursorY, 0));
        touch_event_time = 1;
    }

    //Runnable task for canceling touch
    private Runnable mCancelTouchTask = new Runnable() {
        @Override
        public void run() {
            touchCancelEvent();
        }
    };

    // cursor position
    private int mCursorX = 0;
    private int mCursorY = 0;

    // offset in cursor position to compensate shift in position when pose changes
    private int mCursorDeltaX = 0;
    private int mCursorDeltaY = 0;
    private long tick = 0;

    @Override
    public void onDetected(final HandPoseEvent event, final boolean newdetected){
        //Log.d(TAG, "onDetected: " + event);

        // Call parents onDetected so pose cursor is shown
        super.onDetected(event, newdetected);

        post(new Runnable() {
            @Override
            public void run() { //TODO: Mess with this section so that the pose doesn't need to change, just held
                if(newdetected){

                    //if(shouldClick){
                    if(touch_event_time == -1 && event.handpose.pose() == POSE_TOUCH){
                        return;
                    }
                    //    if this is the end of the touch event, trigger touch up event
                    if(event.handpose.pose() == POSE_HOVER && mPreviousPose == POSE_TOUCH){
                        if(mDraggable){
                            touchUpEvent();
                        }
                    }
                    moveCursor(event);

                    //   if this is the start of a touch, trigger the touchdown event
                    if(event.handpose.pose() == POSE_TOUCH){
                        if(mDraggable){
                            touchDownEvent();
                        }
                    }
                   // } else {
//                    if(touch_event_time == -1 && event.handpose.pose() == POSE_HOVER){
//                        touchDownEvent();
//                        return;
//                    }
                   // }
                    // remove cancel touch timeout
                    removeCallbacks(mCancelTouchTask);
                } else{
                    moveCursor(event);

                    //if(shouldClick){
                        // dispatch touch down(touch move) event
                        if(touch_event_time != -1 && event.handpose.pose() == POSE_TOUCH){
                            if(mDraggable){
                                touchDownEvent();
                            }
                        }
//                    } else {
//                        if(event.handpose.pose() == POSE_HOVER && touch_event_time != -1){
//                            touchDownEvent();
//                        }
//                    }
                    // remove cancel touch timeout
                    removeCallbacks(mCancelTouchTask);
                }
            }
        });
    }

    @Override
    public void onLost(final HandPoseEvent event){
        Log.d(TAG, "onLost: " + event);
        //Call parents onlost event so that the cursor is updated (3 seconds)
        super.onLost(event);

//        if(shouldClick){
            if(event.handpose.pose() == POSE_TOUCH){
                if(mDraggable){
                    postDelayed(mCancelTouchTask, 3000);
                }
            }
//        } else {
//            if(event.handpose.pose() == POSE_HOVER){
//                tick = 0;
//                postDelayed(mCancelTouchTask, 1000);
//            }
//        }
    }

    @Override
    public void onMotion(final HandPoseEvent event){}

    public final HandTransitionListener touchDownListener = new HandTransitionListener() {
        @Override
        public void onTransition(final HandTransitionEvent event) {
            post(new Runnable() {
                @Override
                public void run() {
                    if(mDraggable){
                        //start dragging
                        touchDownEvent();
                    } else {
                        //dispatch click event
                        dispatchClickEvent();
                    }
                }
            });
        }
    };
}
