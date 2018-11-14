package com.example.wangyi.my_weather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangyi.bean.SixDayBean;
import com.example.wangyi.bean.TodayWeather;
import com.example.wangyi.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyi on 2018/9/28.
 */
public class MainActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {
    private ImageView mUpdateBtn;

    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private static final int UPDATE_TODAY_WEATHER = 1;

    //天气预报天数
    private static int Weather_Count = 6;
    //存6天天气数值的javaBean
    private List<SixDayBean> sixDayList = new ArrayList<SixDayBean>();
    //存6天天气的中用来展示的Textview对象
    TextView[][] sixdayIDs;

    private ImageView[] dots;
    private int[] ids = {R.id.v1,R.id.v2};

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private ViewPageAdapt viewPageAdapt;
    private ViewPager vp;
    private List<View> views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        for(int i = 0; i < Weather_Count; i ++){
            sixDayList.add(new SixDayBean());
        }

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this,"网络OK！", Toast.LENGTH_LONG).show();
        }else
        {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        initView();
        initDots();


    }

    void initDots(){
        dots = new ImageView[views.size()];
        for(int i = 0; i < views.size(); i ++){
            dots[i] = (ImageView)findViewById(ids[i]);
        }
    }

    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality
        );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature
        );
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.sixday_1,null));
        views.add(inflater.inflate(R.layout.sixday_2,null));
        viewPageAdapt = new ViewPageAdapt(views, this);
        vp = (ViewPager)findViewById(R.id.viewPager);
        vp.setAdapter(viewPageAdapt);
        vp.setOnPageChangeListener(this);

        sixdayIDs = initSixDayIDs(views);
        setSixDayViewText(sixdayIDs,null);
    }


    @Override
    public void onClick(View view) {

        Log.d("view.id",view.getId()+"");

        if (view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this, SelectCity.class);
            startActivityForResult(i,1);
        }

        if (view.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("myWeather",cityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");

                findViewById(R.id.updat_rel).setVisibility(View.INVISIBLE);
                findViewById(R.id.title_update_progress).setVisibility(View.VISIBLE);

                queryWeatherCode(cityCode);
            }else
            {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        findViewById(R.id.updat_rel).setVisibility(View.VISIBLE);
        findViewById(R.id.title_update_progress).setVisibility(View.INVISIBLE);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);
            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }
        }
    }
    /**
     *
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather = null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();

                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);
                    //parseXML(responseStr);

                    todayWeather = parseXML(responseStr);

                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                        try {
                                Thread.currentThread().sleep(2000);
                                findViewById(R.id.title_update_progress).setVisibility(View.INVISIBLE);
                                findViewById(R.id.updat_rel).setVisibility(View.VISIBLE);
                            }catch (Exception e){}
                    }
                }
            }
        }).start();
    }

    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

        setSixDayViewText(sixdayIDs,sixDayList);
    }


    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        int myfengli = 1;
        int flag = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
// 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
// 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                    eventType = xmlPullParser.next();
                            todayWeather.setCity(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("updatetime")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setUpdatetime(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("shidu")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setShidu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("wendu")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setWendu(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("pm25")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setPm25(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("quality")) {
                        eventType = xmlPullParser.next();
                        todayWeather.setQuality(xmlPullParser.getText());
                    } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                        eventType = xmlPullParser.next();
                        todayWeather.setFengxiang(xmlPullParser.getText());
                        fengxiangCount++;
                    } else if (xmlPullParser.getName().equals("fengli")) {
                                eventType = xmlPullParser.next();
                                if (fengliCount == 0){
                                    todayWeather.setFengli(xmlPullParser.getText());
                                }else if(fengliCount % 2 == 1){
                                    sixDayList.get(myfengli ++).setWindforce(xmlPullParser.getText());
                                }
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date")) {
                        eventType = xmlPullParser.next();
                                if(dateCount == 0){
                                    todayWeather.setDate(xmlPullParser.getText());
                                }
                                sixDayList.get(dateCount + 1).setDate(xmlPullParser.getText());
                                dateCount++;
                    } else if (xmlPullParser.getName().equals("high")) {
                                eventType = xmlPullParser.next();
                                if(highCount == 0){
                                    todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                }
                                sixDayList.get(highCount + 1).setHigh(xmlPullParser.getText().substring(2).trim());
                                    highCount++;
                    } else if (xmlPullParser.getName().equals("low")) {
                                eventType = xmlPullParser.next();
                                if(lowCount == 0) {
                                    todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                }
                                sixDayList.get(lowCount + 1).setLow(xmlPullParser.getText().substring(2).trim());
                                    lowCount++;

                    } else if (xmlPullParser.getName().equals("type")) {
                        eventType = xmlPullParser.next();
                                if(typeCount == 0) {
                                    todayWeather.setType(xmlPullParser.getText());
                                }
                                if(typeCount % 2 == 0) {
                                    sixDayList.get(typeCount/2 + 1).setWeather(xmlPullParser.getText());
                                }
                                    typeCount++;
                    }else if(xmlPullParser.getName().equals("date_1")){
                                eventType = xmlPullParser.next();
                                sixDayList.get(0).setDate(xmlPullParser.getText());
                    }else if(xmlPullParser.getName().equals("high_1")){
                                eventType = xmlPullParser.next();
                                sixDayList.get(0).setHigh(xmlPullParser.getText().substring(2).trim());
                    }else if(xmlPullParser.getName().equals("low_1")){
                            eventType = xmlPullParser.next();
                            sixDayList.get(0).setLow(xmlPullParser.getText().substring(2).trim());
                    }else if(xmlPullParser.getName().equals("type_1") && flag % 2 == 0){
                                eventType = xmlPullParser.next();
                                sixDayList.get(0).setWeather(xmlPullParser.getText());
                    }else if(xmlPullParser.getName().equals("fl_1") && flag % 2 == 0){
                                eventType = xmlPullParser.next();
                                sixDayList.get(0).setWindforce(xmlPullParser.getText());
                                flag = 1;
                    }
                }
                break;
                // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    break;
            }
            // 进入下一个元素并触发相应事件
            eventType = xmlPullParser.next();
        }
    } catch (XmlPullParserException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
        Log.d("sixday",sixDayList.toString());
    return todayWeather;
}
    @Override
    public void onPageScrolled(int i, float v, int i2) {}

    @Override
    public void onPageSelected(int i) {
        for(int j = 0; j < ids.length; j ++){
            if(j == i){
                dots[j].setImageResource(R.drawable.page_indicator_focused);
            }else {
                dots[j].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {}

    //找到6天天气指定的TextView对象
    public TextView[][] initSixDayIDs( List<View> xml){
        TextView[][] views = new TextView[Weather_Count][4];
        int j = 0;
        for(int i = 0; i < xml.size(); i ++){
            views[j][0] = (TextView)xml.get(i).findViewById(R.id.week_today1);
            views[j][1] = (TextView)xml.get(i).findViewById(R.id.temperature1);
            views[j][2] = (TextView)xml.get(i).findViewById(R.id.climate1);
            views[j][3] = (TextView)xml.get(i).findViewById(R.id.windforce1);
            j ++;
            views[j][0] = (TextView)xml.get(i).findViewById(R.id.week_today2);
            views[j][1] = (TextView)xml.get(i).findViewById(R.id.temperature2);
            views[j][2] = (TextView)xml.get(i).findViewById(R.id.climate2);
            views[j][3] = (TextView)xml.get(i).findViewById(R.id.windforce2);
            j ++;
            views[j][0] = (TextView)xml.get(i).findViewById(R.id.week_today3);
            views[j][1] = (TextView)xml.get(i).findViewById(R.id.temperature3);
            views[j][2] = (TextView)xml.get(i).findViewById(R.id.climate3);
            views[j][3] = (TextView)xml.get(i).findViewById(R.id.windforce3);
            j ++;
        }
      return views;
    }

    //给6天天气赋值
    public void setSixDayViewText(TextView[][] myviews, List<SixDayBean> list){
        for(int i = 0; i < myviews.length; i ++){
            if(list == null){
                myviews[i][0].setText("N/A");
                myviews[i][1].setText("N/A");
                myviews[i][2].setText("N/A");
                myviews[i][3].setText("N/A");
            }else {
                myviews[i][0].setText(list.get(i).getDate());
                myviews[i][1].setText(list.get(i).getLow() + "~" + list.get(i).getHigh());
                myviews[i][2].setText(list.get(i).getWeather());
                myviews[i][3].setText(list.get(i).getWindforce());
            }
        }
    }
}