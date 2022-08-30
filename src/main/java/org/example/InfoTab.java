package org.example;

public class InfoTab {
    private String keyWords;
    private int expereinceAmount;
    private int regionId;
    public String getKeyWords(){
        return this.keyWords;
    }
    public int getExpereinceAmount(){
        return this.expereinceAmount;
    }
    public int getRegionId(){
        return this.regionId;
    }
    public InfoTab(String keyWords, int expereinceAmount,int regionId) {
        if(expereinceAmount > 5 || expereinceAmount < 1) {
            throw new IllegalArgumentException("expereinceAmount more than 5 or less then 1");
        }
        this.keyWords = keyWords;
        this.expereinceAmount =expereinceAmount;
        this.regionId=regionId;

    }
}
