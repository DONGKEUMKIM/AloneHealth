package com.example.caucse.alonehealth;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class ExerciseListViewAdapter extends BaseAdapter {
    private ArrayList<ScheduleData> listViewItemList = new ArrayList<ScheduleData>() ;

    private ViewHolder viewHolder;

    public ExerciseListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        View view = convertView;
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_item, parent, false);

            /*LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.listview_item,null);*/

            viewHolder= new ViewHolder();

            viewHolder.exerciseNameTextView = (TextView) view.findViewById(R.id.exerciseName) ;
            viewHolder.exerciseSetTextView = (TextView) view.findViewById(R.id.exerciseSet) ;
            viewHolder.exerciseNumberTextView = (TextView) view.findViewById(R.id.exerciseNumber) ;

            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)view.getTag();
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        /*if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }*/


        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        /*TextView exerciseNameTextView = (TextView) convertView.findViewById(R.id.exerciseName) ;
        TextView exerciseSetTextView = (TextView) convertView.findViewById(R.id.exerciseSet) ;
        TextView exerciseNumberTextView = (TextView) convertView.findViewById(R.id.exerciseNumber) ;*/

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ScheduleData listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        if(listViewItem != null) {
            //ViewHolder viewHolder = (ViewHolder)view.getTag();
            viewHolder.exerciseNameTextView.setText(SQLiteManager.sqLiteManager.selectExerciseNameFromId(listViewItem.getExercise_id()));
            viewHolder.exerciseSetTextView.setText(String.valueOf(listViewItem.getSet()) + " SET");
            viewHolder.exerciseNumberTextView.setText(String.valueOf(listViewItem.getNumber()) + " 회");

            //운동 유무에 따라 글자색 표시
            if(listViewItem.getIsDone() == 1)
            {
                //운동을 실시했을때
                viewHolder.exerciseNameTextView.setTextColor(Color.GREEN);
                viewHolder.exerciseSetTextView.setTextColor(Color.GREEN);
                viewHolder.exerciseNumberTextView.setTextColor(Color.GREEN);
            }
            else if(listViewItem.getIsDone() == 0)
            {
                //운동을 실시하지 않았을때
                viewHolder.exerciseNameTextView.setTextColor(Color.RED);
                viewHolder.exerciseSetTextView.setTextColor(Color.RED);
                viewHolder.exerciseNumberTextView.setTextColor(Color.RED);
            }
        }
        //운동 유무에 따라 글자색 표시
        /*if(listViewItem.getIsDone() == 1)
        {
            //운동을 실시했을때
            exerciseNameTextView.setTextColor(Color.GREEN);
            exerciseSetTextView.setTextColor(Color.GREEN);
            exerciseNumberTextView.setTextColor(Color.GREEN);
        }
        else if(listViewItem.getIsDone() == 0)
        {
            //운동을 실시하지 않았을때
            exerciseNameTextView.setTextColor(Color.RED);
            exerciseSetTextView.setTextColor(Color.RED);
            exerciseNumberTextView.setTextColor(Color.RED);
        }*/

        return view;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 지정한 위치에 있는 아이템 삭제
    public void delete(int position){
        listViewItemList.remove(position);
    }
    // 아이템 데이터 추가를 위한 함수.
    public void setListViewItemList(ArrayList<ScheduleData> itemList) {
       if(itemList != null)
           listViewItemList = itemList;
    }

    private class ViewHolder {
        TextView exerciseNameTextView;
        TextView exerciseSetTextView;
        TextView exerciseNumberTextView;
    }

}
