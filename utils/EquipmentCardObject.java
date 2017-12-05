package com.example.bryan.odginformar.utils;

/**
 * Created by bryan on 11/27/2017.
 */

public class EquipmentCardObject {
    private String cardTitle;
    private String cardContent;
    private String videoResourceName;
    private int imageResource;
    private int maxCardIndex;
    private int stepNumber;

    public EquipmentCardObject(String cardTitle, String cardContent, int imageResource, String videoResourceName, int stepNumber, int maxCardIndex){
        if(cardTitle != null){
            this.cardTitle = cardTitle;
        } else {
            this.cardTitle = "";
        }
        if(cardContent != null){
            this.cardContent = cardContent;
        } else {
            this.cardContent = "";
        }
        if(imageResource != 0) {
            this.imageResource = imageResource;
        }
        if(!videoResourceName.equals("")){
            this.videoResourceName = videoResourceName;
        }
        if(maxCardIndex != 0){
            this.maxCardIndex = maxCardIndex;
        }
        this.stepNumber = stepNumber;
    }

    public EquipmentCardObject(){
        cardTitle = "";
        cardContent = "";
        imageResource = 0;
        videoResourceName = "";
        maxCardIndex = 1;
        stepNumber = 0;
    }

    public String getCardTitle(){
        return this.cardTitle;
    }
    public String getCardContent() {return this.cardContent;}
    public int getImageResource() {return this.imageResource;}
    public int getMaxCardIndex() {return this.maxCardIndex;}
    public int getStepNumber() {return this.stepNumber;}
    public String getVideoResourceName(){ return this.videoResourceName;}

    public void setCardTitle(String string){ this.cardTitle = string;}
    public void setCardContent(String string) {this.cardContent = string;}
    public void setImageResource(int resource){this.imageResource = resource;}
    public void setMaxCardIndex(int maxCardIndex){
        this.maxCardIndex = maxCardIndex;
    }
    public void setStepNumber(int stepNumber){this.stepNumber = stepNumber;}
    public void setVideoResourceName(String videoResourceName){this.videoResourceName = videoResourceName;}
}
