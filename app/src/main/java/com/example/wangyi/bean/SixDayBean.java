package com.example.wangyi.bean;

/**
 * Created by wangyi on 2018/11/12.
 */
public class SixDayBean {
    private String weather;
    private String windforce;
    private String date;
    private String high;
    private String low;

    public String getLow() {
        return low;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    @Override
    public String toString() {
        return "SixDayBean{" +
                "weather='" + weather + '\'' +
                ", windforce='" + windforce + '\'' +
                ", date='" + date + '\'' +
                ", high='" + high + '\'' +
                ", low='" + low + '\'' +
                '}';
    }

    public void setWindforce(String windforce) {
        this.windforce = windforce;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWindforce() {

        return windforce;
    }

    public String getDate() {
        return date;
    }

    public String getWeather() {
        return weather;
    }
}
