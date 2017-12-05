package com.example.bryan.odginformar.AR_interaction_utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.augumenta.agapi.HandPose;
import com.augumenta.agapi.HandPoseEvent;
import com.augumenta.agapi.HandPoseListener;
import com.augumenta.agapi.Poses;
import com.example.bryan.odginformar.R;

/**
 * Created by Bryan on 10/25/2017.
 */

public class PoseCursorLayout extends RelativeLayout implements HandPoseListener {

    public static final float MARGIN_TOP = -0.2f;
    public static final float MARGIN_RIGHT = -0.2f;
    public static final float MARGIN_BOTTOM = -0.2f;
    public static final float MARGIN_LEFT = -0.2f;

    //used to map the camera margins so it's possible to interact with the
    // views near the edges
    public final RelativeMargin margin = new RelativeMargin(MARGIN_LEFT, MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM);

    private static final SparseArray<Integer> POSE_CURSORS = new SparseArray<>();
    static {
        POSE_CURSORS.put(Poses.P001, R.drawable.p001);
        POSE_CURSORS.put(Poses.P008, R.drawable.p008);
        POSE_CURSORS.put(Poses.P032, R.drawable.p032);
        POSE_CURSORS.put(Poses.P141, R.drawable.p032);
        POSE_CURSORS.put(Poses.P201, R.drawable.p201);
    }

    //Views showing the cursor poses, left and right
    private ImageView mCursorViewRight;
    private ImageView mCursorViewLeft;

    public PoseCursorLayout(Context context){
        this(context, null, -1);
    }

    public PoseCursorLayout(Context context, AttributeSet attrs){
        this(context, attrs, -1);
    }

    public PoseCursorLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);

        //create cursor views for both hands
        mCursorViewLeft = createCursorView(context);
        mCursorViewRight = createCursorView(context);
        // mirror right hand cursor view
        mCursorViewRight.setRotation(180);

        addView(mCursorViewLeft);
        addView(mCursorViewRight);
    }

    // Create default pose cursor view
    private ImageView createCursorView(Context context){
        ImageView cursor = new ImageView(context);
        cursor.setVisibility(GONE);

        int size = getResources().getDimensionPixelSize(R.dimen.pose_cursor_size);
        LayoutParams params = new LayoutParams(size, size);
        cursor.setLayoutParams(params);

        return cursor;
    }

    //Show/hide cursors based on hand side
    public void setCursorVisible(HandPose.HandSide handSide, boolean visible){
        if(handSide == HandPose.HandSide.LEFT && mCursorViewLeft != null){
            mCursorViewLeft.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if(handSide == HandPose.HandSide.RIGHT && mCursorViewRight != null){
            mCursorViewRight.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);

        //ensure the cursors views are up front
        if(mCursorViewLeft != null){
            mCursorViewLeft.bringToFront();
        }
        if(mCursorViewRight != null){
            mCursorViewRight.bringToFront();
        }
    }

    //Update the cursor views based on the event
    //Positions the cursor on the screen and selects the view to use
    //based on the handside
    private void updateCursor(HandPoseEvent event){
        ImageView view = null;
        if(event.handpose.handside() == HandPose.HandSide.LEFT){
            view = mCursorViewLeft;
        }
        if(event.handpose.handside() == HandPose.HandSide.RIGHT){
            view = mCursorViewRight;
        }

        if(view != null){
            int x = (int) (margin.translateX(event.rect.centerX()) * getWidth());
            int y = (int) (margin.translateY(event.rect.centerY()) * getHeight());
            int pose = event.handpose.pose();

            MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();

            params.leftMargin = x - (view.getWidth() / 2);
            params.topMargin = y - (view.getHeight() / 2);
            view.setLayoutParams(params);

            view.setImageResource(POSE_CURSORS.get(pose, R.drawable.fallback_cursor));
        }
    }

    @Override
    public void onDetected(final HandPoseEvent event, final boolean newdetection){
        if(newdetection){
            HandDetectionNotifier.notifyDetected();
        }

        post(new Runnable() {
            @Override
            public void run() {
                //new pose, new cursor
                updateCursor(event);
                if(newdetection){
                    setCursorVisible(event.handpose.handside(), true);
                }
            }
        });
    }

    @Override
    public void onLost(final HandPoseEvent event){
        HandDetectionNotifier.notifyLost();

        post(new Runnable(){
            @Override
            public void run(){
                //pose lost, hide cursor
                setCursorVisible(event.handpose.handside(), false);
            }
        });
    }

    @Override
    public void onMotion(final HandPoseEvent event){
    }

}
