package com.example.weather.db;

import org.litepal.crud.LitePalSupport;

public class County extends LitePalSupport {

    // 存放县级数据
    private String countyName;
    private String countyCode;
    private String cityCode;


    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String  getCityCode(){
        return cityCode;
    }

    public void setCityCode(String  cityCode){
        this.cityCode = cityCode;
    }

    public void setCountyCode(String  countyCode){
        this.countyCode = countyCode;
    }

    public String   getCountyCode(){
        return countyCode;
    }


}
