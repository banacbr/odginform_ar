package com.example.bryan.odginformar.utils;


import com.example.bryan.odginformar.R;

/**
 * Created by Bryan on 10/19/2017.
 */

public class TroubleshootingCardViewObject {
    private String cardTitle;
    private String cardContent;
    private int imageResource;
    private boolean needVerificationToContinue;
    private boolean canContinue;
    private int canContinueIcon;
    private int maxCardIndex;
    private String packageName;
    private String videoResourceName;
    private String successful;

    //Full constructor and empty constructor

    public TroubleshootingCardViewObject(String cardTitle, String cardContent, int imageResource, boolean needVerificationToContinue, String packageName, int maxCardIndex, String videoResourceName){
        this.cardTitle = cardTitle;
        this.cardContent = cardContent;
        this.imageResource = imageResource;
        this.needVerificationToContinue = needVerificationToContinue;
        if(needVerificationToContinue){
            this.canContinue = false;
            this.canContinueIcon = R.drawable.ic_pan_tool_black_24dp;
        }else {
            this.canContinue = true;
        }
        this.canContinueIcon = R.drawable.ic_thumb_up_black_24dp;
        this.maxCardIndex = maxCardIndex;
        this.packageName = packageName;
        this.videoResourceName = videoResourceName;
        this.successful = "uninitialized";
    }
    public TroubleshootingCardViewObject(){
        this.cardTitle = "";
        this.cardContent = "";
        this.imageResource = 0;
        this.needVerificationToContinue = false;
        this.canContinue = true;
        this.canContinueIcon = R.drawable.ic_thumb_up_black_24dp;
        this.maxCardIndex = 1;
        this.videoResourceName = "";
        this.successful = "uninitialized";
    }

    //Getter methods
    public String getCardTitle(){
        return this.cardTitle;
    }
    public String getCardContent() {return this.cardContent;}
    public int getImageResource() {return this.imageResource;}
    public boolean isNeedVerificationToContinue() { return this.needVerificationToContinue;}
    public boolean isCanContinue(){return this.canContinue;}
    public int getCanContinueIcon(){return this.canContinueIcon;}
    public int getMaxCardIndex() {return this.maxCardIndex;}
    public String getPackageName() {return this.packageName;}
    public String getVideoResourceName() {return this.videoResourceName;}
    public String getSuccessStatus() { return this.successful; }

    //Setter methods
    public void setCardTitle(String string){ this.cardTitle = string;}
    public void setCardContent(String string) {this.cardContent = string;}
    public void setImageResource(int resource){this.imageResource = resource;}
    public void setNeedVerificationToContinue(boolean bool){
        //If set to true, user will need to verify via gesture to continue in steps
        //If set to false, fires setCanContinue method to change icon and unlock card to proceed
        this.needVerificationToContinue = bool;
        if(bool == false){
            this.setCanContinue(true);
        }
    }
    public void setCanContinue(boolean bool){
        //If method is set to true, card is unlocked for user to proceed, change icon to thumbs-up
        this.canContinue = bool;
        this.canContinueIcon = R.drawable.ic_thumb_up_black_24dp;
    }
    public void setMaxCardIndex(int maxCardIndex){
        this.maxCardIndex = maxCardIndex;
    }
    public void setPackageName(String packageName) {this.packageName = packageName;}
    public void setVideoResourceName(String videoResourceName) {this.videoResourceName = videoResourceName;}
    public void setSuccessful(boolean success){ this.successful = String.valueOf(success); }
}
