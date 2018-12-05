package com.example.caucse.alonehealth;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import org.w3c.dom.Text;

import java.lang.annotation.Documented;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

import decorators.EventDecorator;
import decorators.OneDayDecorator;
import decorators.SaturdayDecorator;
import decorators.SundayDecorator;

public class CalendarManager extends AppCompatActivity {

    String time,kcal,menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;
    ListView listView;
    ExerciseListViewAdapter adapter;
    Button addExerciseButton;
    Button editExerciseButton;
    Button deleteExerciseButton;
    AddExerciseDialog addExerciseDialog;
    AddExerciseDialog editExerciseDialog;
    AlertDialog deleteExerciseDialog;
    TextView currentDateTextView;
    Date selectedDate;
    SimpleDateFormat simpleDateFormat;
    int indexOfSelectedItem = -1;
    String idOfSelectedItem;
    int isDone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        /**초기 세팅*/
        simpleDateFormat = new SimpleDateFormat("yyyy. MM. dd");
        //schedule list view adapter
        adapter = new ExerciseListViewAdapter();
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        /**운동 추가 다이얼로그 창 설정*/
        addExerciseDialog = new AddExerciseDialog(this,adapter,true);
        addExerciseDialog.getWindow().setGravity(Gravity.BOTTOM);

        /**운동 수정 다이얼로그 창 설정*/
        editExerciseDialog = new AddExerciseDialog(this,adapter,false);
        editExerciseDialog.getWindow().setGravity(Gravity.BOTTOM);

        /**운동 수정 다이얼로그 창 설정*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("일정 삭제")
                .setMessage("정말 삭제 하시겠습니까?")
                .setPositiveButton("예",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dlg, int value){
                        SQLiteManager.sqLiteManager.deleteScheduleRecord(idOfSelectedItem);
                        adapter.delete(indexOfSelectedItem);
                        adapter.notifyDataSetChanged();
                        editExerciseButton.setVisibility(View.INVISIBLE);
                        deleteExerciseButton.setVisibility(View.INVISIBLE);
                        addExerciseButton.setVisibility(View.VISIBLE);
                        indexOfSelectedItem = -1;


                    }
                })
                .setNegativeButton("아니요",null);
        deleteExerciseDialog = builder.create();
        /////////////////////////////////////////////////////////////////////
        /**운동 추가, 수정, 삭제 버튼**/
        addExerciseButton = findViewById(R.id.add_exercise_button);
        editExerciseButton = findViewById(R.id.edit_exercise_button);
        deleteExerciseButton = findViewById(R.id.delete_exercise_button);

        ///////////////////////////////툴바//////////////////////////////////
        Toolbar toolbar = (Toolbar)findViewById(R.id.myToolbar);
        /*일정관리 버튼 누를시
         *일정관리페이지로 이동*/
        toolbar.findViewById(R.id.Schedule_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        CalendarManager.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        /*홈 버튼 누를시
         *첫화면으로 이동*/
        toolbar.findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        /////////////////////////////////////////////////////////////////
        /**날짜 최신화*/
        currentDateTextView = (TextView)findViewById(R.id.currentDate);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        addExerciseDialog.currentDate = new Date(now);
        editExerciseDialog.currentDate = new Date(now);
        selectedDate = new Date(now);
        currentDateTextView.setText(simpleDateFormat.format(date));
        adapter.setListViewItemList(SQLiteManager.sqLiteManager.selectScheduleFromDate(simpleDateFormat.format(date)));
        /////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////
        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        TitleFormatter titleFormatter = new TitleFormatter() {

            @Override
            public CharSequence format(CalendarDay day) {
                String format = new String(""+day.getYear()+"년 "+(day.getMonth()+1)+"월  ");
                return format;
            }
        };
        materialCalendarView.setTitleFormatter(titleFormatter);
        materialCalendarView.setTileSizeDp(44);
        materialCalendarView.setArrowColor(Color.WHITE);
        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);

        String[] result = {"2017,03,18","2017,04,18","2017,05,18","2017,06,18"};

        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());

        // 날짜 클릭 이벤트 리스너
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                String shot_Day = Year + "." + Month + "." + Day;

                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();

                oneDayDecorator.setDate(date.getDate());
                selectedDate = date.getDate();
                materialCalendarView.removeDecorator(oneDayDecorator);
                materialCalendarView.addDecorator(oneDayDecorator);
                currentDateTextView.setText(simpleDateFormat.format(date.getDate()));
                adapter.setListViewItemList(SQLiteManager.sqLiteManager.selectScheduleFromDate(simpleDateFormat.format(date.getDate())));
                adapter.notifyDataSetChanged();

            }
        });
        /////////////////////////////////////////////////////////////////////

        /**버튼 클릭 리스너*/
        //추가 버튼
        addExerciseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                addExerciseDialog.setSelectedDate(selectedDate);
                addExerciseDialog.show();
            }
        });
        //수정버튼
        editExerciseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                    editExerciseDialog.setSelectedDate(selectedDate);
                    editExerciseDialog.setSelectedItemId(idOfSelectedItem);
                    editExerciseDialog.show();
            }
        });
        //삭제버튼
        deleteExerciseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                deleteExerciseDialog.show();
            }
        });
        /////////////////////////////////////////////////////////////////////

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listView.refreshDrawableState();
                ScheduleData temp = (ScheduleData)adapterView.getItemAtPosition(i);
                idOfSelectedItem = temp.getId();
                isDone = temp.getIsDone();
                    if(indexOfSelectedItem != i)
                    {
                        indexOfSelectedItem = i;
                        if(isDone == 0) {
                            editExerciseButton.setVisibility(View.VISIBLE);
                            deleteExerciseButton.setVisibility(View.VISIBLE);
                            addExerciseButton.setVisibility(View.INVISIBLE);
                        }
                        for(int iCnt = 0; iCnt< listView.getAdapter().getCount();iCnt++)
                        {
                            if(iCnt == i)
                            {
                                getViewByPosition(iCnt,listView).setBackgroundColor(Color.rgb(210,210,210));
                            }
                            else
                            {
                                getViewByPosition(iCnt,listView).setBackgroundColor(Color.TRANSPARENT);
                            }
                        }
                    }
                    else if(indexOfSelectedItem == i)
                    {
                        editExerciseButton.setVisibility(View.INVISIBLE);
                        deleteExerciseButton.setVisibility(View.INVISIBLE);
                        addExerciseButton.setVisibility(View.VISIBLE);
                        getViewByPosition(i,listView).setBackgroundColor(Color.TRANSPARENT);
                        indexOfSelectedItem = -1;
                    }
                adapter.notifyDataSetChanged();


               /* if(indexOfSelectedItem == i){
                    editExerciseButton.setVisibility(View.INVISIBLE);
                    deleteExerciseButton.setVisibility(View.INVISIBLE);
                    addExerciseButton.setVisibility(View.VISIBLE);
                    indexOfSelectedItem = -1;
                }
                else{
                    editExerciseButton.setVisibility(View.VISIBLE);
                    deleteExerciseButton.setVisibility(View.VISIBLE);
                    addExerciseButton.setVisibility(View.INVISIBLE);
                    indexOfSelectedItem = i;
                }*/
            }
        });



    }

    @Override
    public void onBackPressed(){

    }

    //리스트뷰에서 포지션위치의 자식을 받아오는 함수
    public View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return listView.getAdapter().getView(position, listView.getChildAt(position), listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;

        ApiSimulator(String[] Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for(int i = 0 ; i < Time_Result.length ; i ++){
                CalendarDay day = CalendarDay.from(calendar);
                String[] time = Time_Result[i].split(",");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                dates.add(day);
                calendar.set(year,month-1,dayy);
            }



            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (isFinishing()) {
                return;
            }

            materialCalendarView.addDecorator(new EventDecorator(Color.GREEN, calendarDays,CalendarManager.this));
        }
    }

}