package com.example.bryan.odginformar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.bryan.odginformar.R;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import static android.view.View.GONE;

/**
 * Created by bryan on 10/12/2017.
 */

public class GeneralCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int TYPE_CONTENT_CARD=1, TYPE_TROUBLESHOOTING_CARD=4, TYPE_EQUIPMENT_CARD = 5;

    private List<Object> mDataset = new ArrayList();
    private Context context;

    public GeneralCardAdapter(Context context){
        this.context = context;
    }

    public void setmDataset(List<Object> mDataset){
        this.mDataset = mDataset;

    }

    // We need to override this as we need to differentiate
    // which type of viewholder to be attached
    // This is being called from onBindViewHolder method
    @Override
    public int getItemViewType(int position){
        if(mDataset.get(position) instanceof ContentCardViewObject){
            return TYPE_CONTENT_CARD;
        } else if (mDataset.get(position) instanceof TroubleshootingCardViewObject){
            return TYPE_TROUBLESHOOTING_CARD;
        } else if (mDataset.get(position) instanceof EquipmentCardObject){
            return TYPE_EQUIPMENT_CARD;
        }
        return -1;
    }


    //Invoked by the layout manager to replace the content of the views
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position){
        int viewType = viewHolder.getItemViewType();
        switch(viewType){
            case TYPE_CONTENT_CARD:
                ContentCardViewObject contentCardViewObject = (ContentCardViewObject) mDataset.get(position);
                ((ContentCardViewHolder) viewHolder).bind(contentCardViewObject);
                break;
            case TYPE_TROUBLESHOOTING_CARD:
                TroubleshootingCardViewObject troubleshootingCardViewObject = (TroubleshootingCardViewObject)mDataset.get(position);
                ((TroubleshootingCardViewHolder)viewHolder).bind(troubleshootingCardViewObject, context);
                break;
            case TYPE_EQUIPMENT_CARD:
                EquipmentCardObject equipmentCardObject = (EquipmentCardObject)mDataset.get(position);
                ((EquipmentCardViewHolder)viewHolder).bind(equipmentCardObject);
                break;
        }
    }


    @Override
    public int getItemCount(){return mDataset.size();}

    //Invoked by Layout Manager to create new views
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Attach layout for single cell
        RecyclerView.ViewHolder viewHolder;
        //Identfy viewType returned by getItemViewType(...)
        //and return ViewHolder accordingly
        switch(viewType){
            //insert logic here after creating the classes below
            case TYPE_CONTENT_CARD:
                View contentView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.swipe_card_view, parent, false);
                viewHolder = new ContentCardViewHolder(contentView);
                break;
            case TYPE_TROUBLESHOOTING_CARD:
                View troubleshootingView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.swipe_card_view, parent, false);
                viewHolder = new TroubleshootingCardViewHolder(troubleshootingView, context);
                break;
            case TYPE_EQUIPMENT_CARD:
                View equipmentView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.equipment_card_view, parent, false);
                viewHolder = new EquipmentCardViewHolder(equipmentView);
                break;
            default:
                viewHolder=null;
                break;
        }
        return viewHolder;
    }

    private static class TroubleshootingCardViewHolder extends RecyclerView.ViewHolder{
        private TextView cardTitle;
        private HtmlTextView cardContent;
        private ImageView imageContent;
        private TextView needsVerify;
        private ImageView canProceed;
        private TextView cardIndex;
        private ImageView successButton;
        private ImageView failureButton;
        private ImageView solutionOutcome;
        private NestedScrollView nsv;
        public boolean swipeable;

        public TroubleshootingCardViewHolder(View itemView, Context context) {
            super(itemView);
            cardTitle = (TextView) itemView.findViewById(R.id.cv_title_text);
            cardContent = (HtmlTextView) itemView.findViewById(R.id.cv_content_html);
            nsv = (NestedScrollView) itemView.findViewById(R.id.cv_contenthtml_container);
            imageContent = (ImageView) itemView.findViewById(R.id.cv_image_content);
            needsVerify = (TextView) itemView.findViewById(R.id.cv_verify_to_continue);
            //canProceed = itemView.findViewById(R.id.cv_can_continue_icon);
            cardIndex = (TextView) itemView.findViewById(R.id.cv_card_index);
            successButton = (ImageView) itemView.findViewById(R.id.cv_success_button);
            failureButton = (ImageView) itemView.findViewById(R.id.cv_failure_button);
            solutionOutcome = (ImageView) itemView.findViewById(R.id.cv_solution_outcome);

        }

        public void bind(final TroubleshootingCardViewObject troubleshootingCardViewObject, Context context){
            //Attach values for each item
            Log.d("binder:", String.valueOf(troubleshootingCardViewObject.isCanContinue()));
            swipeable = troubleshootingCardViewObject.isCanContinue();
            cardTitle.setText(troubleshootingCardViewObject.getCardTitle());
            cardContent.setHtml(troubleshootingCardViewObject.getCardContent());
            successButton.setImageResource(R.drawable.ic_thumb_up_black_24dp);
            successButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.green), PorterDuff.Mode.SRC_IN);
            failureButton.setImageResource(R.drawable.ic_thumb_down_black_24dp);
            failureButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.red), PorterDuff.Mode.SRC_IN);
            solutionOutcome.setVisibility(GONE);
            cardIndex.setText((this.getAdapterPosition() + 1) + " / " + troubleshootingCardViewObject.getMaxCardIndex());
            if(troubleshootingCardViewObject.getImageResource() != 0){
                imageContent.setImageResource(troubleshootingCardViewObject.getImageResource());
                imageContent.setImageDrawable(context.getResources().getDrawableForDensity(troubleshootingCardViewObject.getImageResource(), DisplayMetrics.DENSITY_MEDIUM));
            } else if (!troubleshootingCardViewObject.getVideoResourceName().equals("0")) {
                imageContent.setImageResource(R.drawable.video_placeholder);
                //cardContent.setText(R.string.videoInstructions);
            } else {
                imageContent.setVisibility(GONE);
            }
            if(!troubleshootingCardViewObject.isCanContinue()){
                needsVerify.setText(R.string.needsVerify);
            } else {
                needsVerify.setVisibility(View.INVISIBLE);
            }
            if(troubleshootingCardViewObject.getSuccessStatus().equals("true")){
                //fadeColor(R.color.green, successButton, failureButton, context);
                successButton.setBackgroundColor(itemView.getResources().getColor(R.color.green));
                successButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
                failureButton.setVisibility(View.INVISIBLE);
                Animation moveRight = AnimationUtils.loadAnimation(context, R.anim.thumb_move_right);
                moveRight.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        failureButton.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                successButton.startAnimation(moveRight);
            } else if (troubleshootingCardViewObject.getSuccessStatus().equals("false")){
                //fadeColor(R.color.red, failureButton, successButton, context);
                failureButton.setBackgroundColor(itemView.getResources().getColor(R.color.red));
                failureButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.black), PorterDuff.Mode.SRC_IN);
                successButton.setVisibility(GONE);
            } else {
                solutionOutcome.setVisibility(GONE);
            }
            if(troubleshootingCardViewObject.getCardTitle().equals("Go back")){
                solutionOutcome.setVisibility(View.INVISIBLE);
                successButton.setVisibility(View.INVISIBLE);
                failureButton.setVisibility(View.INVISIBLE);
                cardIndex.setVisibility(View.INVISIBLE);
            }
        }
        private void fadeColor(int color, View view, View fadeoutView, Context context){
            ColorDrawable[] colorFade = {new ColorDrawable(context.getResources().getColor(android.R.color.transparent))
                    , new ColorDrawable(context.getResources().getColor(color))};
            TransitionDrawable transitionDrawable = new TransitionDrawable(colorFade);
            view.setBackgroundDrawable(transitionDrawable);
            transitionDrawable.startTransition(1000);
            fadeoutView.setVisibility(View.INVISIBLE);
        }
    }





    private static class ContentCardViewHolder extends RecyclerView.ViewHolder {
        private TextView cardTitle;
        private TextView holdForVideo;
        private HtmlTextView cardHtml;
        private ImageView imageContent;
        private TextView cardIndex;
        private LinearLayout successButtonContainer;
        private VideoView videoView;
        private NestedScrollView sv;
        private ImageView largeImageView;
        private CardView cvParent;

        public ContentCardViewHolder(View itemView){
            super(itemView);
            cardTitle = (TextView) itemView.findViewById(R.id.cv_title_text);
            cardHtml = (HtmlTextView) itemView.findViewById(R.id.cv_content_html);
            imageContent = (ImageView)itemView.findViewById(R.id.cv_image_content);
            cardIndex = (TextView) itemView.findViewById(R.id.cv_card_index);
            NestedScrollView hiddensv = (NestedScrollView) itemView.findViewById(R.id.cv_content_container);
            hiddensv.setVisibility(GONE);
            sv = (NestedScrollView) itemView.findViewById(R.id.cv_contenthtml_container);
            sv.setVisibility(View.VISIBLE);
            successButtonContainer = (LinearLayout) itemView.findViewById(R.id.cv_success_container);
            largeImageView = (ImageView) itemView.findViewById(R.id.cv_image_large_view);
            cvParent = (CardView) itemView.findViewById(R.id.cv);
        }


        public void bind(final ContentCardViewObject contentCardViewObject){
            //Attach values for each item
            cardTitle.setText(contentCardViewObject.getCardTitle());
            //cardContent.setVisibility(GONE);
            cardHtml.setHtml(contentCardViewObject.getCardContent());
            //cardContent.setText(Html.fromHtml(contentCardViewObject.getCardContent()));
            //sv.loadData(contentCardViewObject.getCardContent(),"text/html", "utf-8");
            cardIndex.setText("Step " + (contentCardViewObject.getStepNumber()) +
                    " of " + (contentCardViewObject.getMaxCardIndex()));
            if(contentCardViewObject.getImageResource() != 0){
                imageContent.setImageResource(contentCardViewObject.getImageResource());
                if(contentCardViewObject.getImageResource() == R.mipmap.m300_1){
                    imageContent.setBackgroundColor(itemView.getResources().getColor(R.color.white));
                }
            }  else {
                imageContent.setVisibility(GONE);
                if(!contentCardViewObject.getVideoResourceName().equals("0")){
                    imageContent.setImageResource(R.drawable.video_placeholder);
                } else {
                    imageContent.setVisibility(GONE);
                }
            }
            if(contentCardViewObject.getCardTitle().equals("Go back")){
                imageContent.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                cardIndex.setVisibility(View.INVISIBLE);
            }
            successButtonContainer.setVisibility(GONE);
        }
    }

    private static class EquipmentCardViewHolder extends RecyclerView.ViewHolder {
        private TextView cardTitle;
        private HtmlTextView cardHtml;
        private ImageView imageContent;
        private TextView cardIndex;
        private VideoView videoView;
        private NestedScrollView sv;

        public EquipmentCardViewHolder(View itemView){
            super(itemView);
            cardTitle = (TextView) itemView.findViewById(R.id.cv_title_text);
            cardHtml = (HtmlTextView) itemView.findViewById(R.id.cv_content_html);
            imageContent = (ImageView)itemView.findViewById(R.id.cv_image_content);
            cardIndex = (TextView) itemView.findViewById(R.id.cv_card_index);
            //NestedScrollView hiddensv = (NestedScrollView) itemView.findViewById(R.id.cv_content_container);
            //hiddensv.setVisibility(GONE);
            sv = (NestedScrollView) itemView.findViewById(R.id.cv_contenthtml_container);
            sv.setVisibility(View.VISIBLE);
        }

        public void bind(final EquipmentCardObject contentCardViewObject){
            //Attach values for each item
            cardTitle.setText(contentCardViewObject.getCardTitle());
            //cardContent.setVisibility(GONE);
            cardHtml.setHtml(contentCardViewObject.getCardContent());
            //cardContent.setText(Html.fromHtml(contentCardViewObject.getCardContent()));
            //sv.loadData(contentCardViewObject.getCardContent(),"text/html", "utf-8");
            cardIndex.setText("Card " + contentCardViewObject.getStepNumber() +
                    " of " + contentCardViewObject.getMaxCardIndex());
            if(contentCardViewObject.getImageResource() != 0){
                imageContent.setImageResource(contentCardViewObject.getImageResource());
            }  else {
                if(!contentCardViewObject.getVideoResourceName().equals("0")){
                    imageContent.setImageResource(R.drawable.video_placeholder);
                } else {
                    imageContent.setVisibility(GONE);
                    FrameLayout fl = (FrameLayout)itemView.findViewById(R.id.equip_fl_image);
                    fl.setVisibility(GONE);
                    sv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3));
                }
            }
            if(cardTitle.equals("Go back")){
                cardIndex.setVisibility(View.INVISIBLE);
            }
        }
    }


    //Load image from network resource
    class BmpAsync extends AsyncTask<URL, Void, Bitmap> {
        ImageView iV;


        public BmpAsync(ImageView view){
            iV = view;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {

            Bitmap networkBitmap = null;

            URL networkUrl = urls[0]; //Load the first element
            InputStream s = null;
            try{
                s = networkUrl.openConnection().getInputStream();
            } catch (IOException e){
                e.printStackTrace();
            }
            final BufferedInputStream is = new BufferedInputStream(s, 32*1024);

            try {
                final BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
                final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
                decodeBoundsOptions.inJustDecodeBounds = true;
                is.mark(32*1024);
                BitmapFactory.decodeStream(is, null, decodeBoundsOptions);
                is.reset();

                final int originalWidth = decodeBoundsOptions.outWidth;
                final int originalHeight = decodeBoundsOptions.outHeight;

                decodeBitmapOptions.inSampleSize = Math.max(1, Math.min(originalWidth/200, originalHeight/200));
                return BitmapFactory.decodeStream(is, null, decodeBitmapOptions);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try{
                    is.close();
                } catch (IOException ignored){}
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap result){
            iV.setImageBitmap(result);
        }

    }




}
