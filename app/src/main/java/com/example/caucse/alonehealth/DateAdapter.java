package com.example.caucse.alonehealth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateAdapter extends PagerAdapter {

    Date today = new Date();

    ArrayList<DateForm> DateList = new ArrayList<DateForm>();

    private LayoutInflater inflater;
    private Context context;

    public DateAdapter(Context context)
    {
        //처음 5개 추가
        //리스트 사이즈는 추가되든 삭제되든 항상 5개로 유지
        addToList(false);

        addToList(true);
        CalWhatday(1,true);
        addToList(true);
        CalWhatday(2, true);

        addToList(false);
        CalWhatday(0,false);

        addToList(false);
        CalWhatday(0,false);
        this.context = context;
    }
    //리스트에 요소 추가 함수
    //true일때 맨 뒤에
    //false일때 맨 앞에 추가
    public void addToList(boolean I)
    {
        DateForm df = new DateForm();
        df.date_str = df.df_date.format(today);
        df.day_str = df.df_day.format(today);
        if(I)
        {
            DateList.add(df);
        }
        else
        {
            DateList.add(0,df);
        }
        //데이터 변경
        notifyDataSetChanged();
    }
    //리스트에 요소 삭제 함수
    //true일때 맨 뒤에
    //false일때 맨 앞에 삭제
    public void deletetoList(boolean I)
    {
        if(I)
        {
            DateList.remove(DateList.size()-1);
        }
        else
        {
            DateList.remove(0);
        }
        //데이터 변경
        notifyDataSetChanged();
    }

    //position 위치에 해당하는 ArrayList안의 날짜 포맷을
    //이 전 날짜와 비교해서 계산하는 함수
    //true 일때 +1
    //false 일때 -1
    public void CalWhatday(int position, boolean LeftOrRight)
    {
        //true 일때
        if(LeftOrRight)
        {
            SimpleDateFormat DATESDF = new SimpleDateFormat("yyyy. MM. dd");
            SimpleDateFormat DAYSDF = new SimpleDateFormat("E", Locale.KOREAN);
            try{
                Date date = DATESDF.parse(DateList.get(position-1).date_str);
                Date day = DAYSDF.parse(DateList.get(position-1).day_str);

                Calendar cal_date = Calendar.getInstance();
                Calendar cal_day = Calendar.getInstance();

                cal_date.setTime(date);
                cal_date.add(Calendar.DATE,1);

                cal_day.setTime(day);
                cal_day.add(Calendar.DAY_OF_WEEK, 1);

                DateList.get(position).date_str = DATESDF.format(cal_date.getTime());
                DateList.get(position).day_str = DAYSDF.format(cal_day.getTime());
            }
            catch(ParseException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            SimpleDateFormat DATESDF = new SimpleDateFormat("yyyy. MM. dd");
            SimpleDateFormat DAYSDF = new SimpleDateFormat("E", Locale.KOREAN);
            try{
                Date date = DATESDF.parse(DateList.get(position+1).date_str);
                Date day = DAYSDF.parse(DateList.get(position+1).day_str);

                Calendar cal_date = Calendar.getInstance();
                Calendar cal_day = Calendar.getInstance();

                cal_date.setTime(date);
                cal_date.add(Calendar.DATE,-1);

                cal_day.setTime(day);
                cal_day.add(Calendar.DAY_OF_WEEK, -1);

                DateList.get(position).date_str = DATESDF.format(cal_date.getTime());
                DateList.get(position).day_str = DAYSDF.format(cal_day.getTime());
            }
            catch(ParseException e)
            {
                e.printStackTrace();
            }

        }
    }
    /////////////////////////////////////////////////
    @Override
    public int getCount() {
        return DateList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == ((LinearLayout)o);
    }

    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.fragment_date, container, false);
        TextView tv_date = (TextView)v.findViewById(R.id.fragment_date);
        TextView tv_day = (TextView)v.findViewById(R.id.fragment_day);

        tv_date.setText(DateList.get(position).date_str);
        tv_day.setText(DateList.get(position).day_str);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.invalidate();
    }
}

