package org.example;

import java.util.ArrayList;
import java.util.List;

public class VacancyData {
    private double zp;
    private  int vacancyAmount;
    public void setVacancyAmount(int vacancyAmount)
    {
        this.vacancyAmount = vacancyAmount;
    }
    public int getVacancyAmount(){return this.vacancyAmount;}
    public void setZp(double zp){this.zp=zp;}
    public double getZp(){return this.zp;}
    public VacancyData(double zp, int vacancyAmount){
        setVacancyAmount(vacancyAmount);
        setZp(zp);
    }


}
