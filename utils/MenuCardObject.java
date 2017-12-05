package com.example.bryan.odginformar.utils;

/**
 * Created by bryan on 10/12/2017.
 */

public class MenuCardObject {
    private String menuTitle;
    private String menuNavigateString;
    private int imageFileResource;

    public MenuCardObject(){
        menuTitle = "";
        menuNavigateString = "";
        imageFileResource = 0;
    }

    public String getMenuTitle() { return this.menuTitle;}
    public String getMenuNavigateString() {return this.menuNavigateString;}
    public int getImageFileResource(){return this.imageFileResource;}

    public void setMenuTitle(String title){this.menuTitle=title;}
    public void setMenuNavigateString(String string){this.menuNavigateString=string;}
    public void setImageFileResource(int integer){this.imageFileResource=integer;}

}
