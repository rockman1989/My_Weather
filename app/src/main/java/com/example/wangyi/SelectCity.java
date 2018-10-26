package com.example.wangyi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.wangyi.app.MyApplication;
import com.example.wangyi.bean.City;
import com.example.wangyi.my_weather.R;

import java.util.ArrayList;

/**
 * Created by wangyi on 2018/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView mBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);


        //获得CityList
        ArrayList<City> myCityList = (ArrayList<City>) MyApplication.getInstance().getCityList();
        //获得myCityList的长度
        int City_length = myCityList.size();
        //存城市名字的数组
        String[] CityName = new String[City_length];
        //存城市编码的数组
        final String[] CityNum = new String[City_length];
        //给2个城市数组赋值
        for(int i = 0; i < City_length; i ++){
            CityName[i] = myCityList.get(i).getCity();
            CityNum[i] = myCityList.get(i).getNumber();
        }

        //初始化listview
        ListView mlistView = (ListView)findViewById(R.id.list_view);
        //初始化适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this, android.R.layout.simple_list_item_1,CityName);

        mlistView.setAdapter(adapter);
        //设置点击效果
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(SelectCity.this, CityNum[position], Toast.LENGTH_SHORT).show();

                Intent i = new Intent();
                i.putExtra("cityCode", CityNum[position]);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", "101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;

        }
    }
}
