package com.example.caucse.alonehealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.w3c.dom.CharacterData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class ExerciseData{
    private String id;
    private String name;
    private int screenorientation;    //운동 방향(0 = 가로, 1 = 세로)
    private int stage;          //운동의 표본 갯수
    private int chest;
    private int arm;
    private int abs;
    private int shoulder;
    private int back;
    private int leg;

    ExerciseData(){}
    ExerciseData(String id, String name, int screenorientation, int stage, int chest, int arm, int abs, int shoulder, int back, int leg){
        this.id = id;
        this.name = name;
        this.screenorientation = screenorientation;         //추가
        this.stage = stage;                                 //추가
        this.chest = chest;
        this.arm = arm;
        this.abs = abs;
        this.shoulder = shoulder;
        this.back = back;
        this.leg = leg;
    }


    public String getId(){return this.id;}
    public String getName(){return this.name;}
    public int getScreenorientation(){return this.screenorientation;}       //GET 함수
    public int getStage(){return this.stage;}                               //GET 함수
    public int getChest(){return this.chest;}
    public int getArm(){return this.arm;}
    public int getAbs(){return this.abs;}
    public int getShoulder(){return this.shoulder;}
    public int getBack(){return this.back;}
    public int getLeg(){return this.leg;}

    public void setId(String id){this.id = id;}
    public void setName(String name){this.name = name;}
    public void setScreenorientation(int screenorientation){this.screenorientation = screenorientation;}        //SET 함수
    public void setStage(int stage){this.stage = stage;}                                                        //SET 함수
    public void setChest(int data){this.chest = data;}
    public void setArm(int data){this.arm = data;}
    public void setAbs(int data){this.abs = data;}
    public void setShoulder(int data){this.shoulder = data;}
    public void setBack(int data){this.back = data;}
    public void setLeg(int data){this.leg = data;}

}
class ScheduleData{
    private String id;
    private String date;
    private String Exercise_id;
    private int set;
    private int number;
    private boolean isDone;
    
    ScheduleData(){}
    ScheduleData(String id, String date, String Exercise_id, int set, int number, int isDone){
        this.id = id;
        this.date = date;
        this.Exercise_id = Exercise_id;
        this.set = set;
        this.number = number;
        if(isDone == 1) this.isDone = true;
        else this.isDone = false;
    }

    public String getId(){return this.id;}
    public String getExercise_id(){return this.Exercise_id;}
    public String getDate(){return this.date;}
    public int getSet(){return this.set;}
    public int getNumber(){return this.number;}
    public int getIsDone(){if(isDone) return 1; else return 0;}

    public void setId(String id){this.id = id;}
    public void setDate(String date){this.date = date;}
    public void setExercise_id(String Exercise_id){this.Exercise_id = Exercise_id;}
    public void setSet(int set){this.set = set;}
    public void setNumber(int number){this.number = number;}
    public void setIsDone(boolean isDone){this.isDone = isDone;}

}
class CharacterStatData{
    private String date;
    private int chest;
    private int arm;
    private int abs;
    private int shoulder;
    private int back;
    private int leg;

    CharacterStatData(){}
    CharacterStatData(String date, int chest, int arm, int abs, int shoulder, int back, int leg){
        this.date = date;
        this.chest = chest;
        this.arm = arm;
        this.abs = abs;
        this.shoulder = shoulder;
        this.back = back;
        this.leg = leg;
    }

    public String getDate(){return this.date;}
    public int getChest(){return this.chest;}
    public int getArm(){return this.arm;}
    public int getAbs(){return this.abs;}
    public int getShoulder(){return this.shoulder;}
    public int getBack(){return this.back;}
    public int getLeg(){return this.leg;}

    public void setDate(String date){this.date = date;}
    public void setChest(int chest){this.chest = chest;}
    public void setArm(int arm){this.arm = arm;}
    public void setAbs(int abs){this.abs = abs;}
    public void setShoulder(int shoulder){this.shoulder = shoulder;}
    public void setBack(int back){this.back = back;}
    public void setLeg(int leg){this.leg = leg;}

}
public class SQLiteManager extends SQLiteOpenHelper {

    public static SQLiteManager sqLiteManager = null;
    public static final String DATABASE_NAME = "AloneHealth.db";
    public static final int DB_VERSION = 1;

    //Exercise table
    public static final String EXERCISE_TABLE_NAME = "EXERCISE";
    public static final String EXERCISE_ID = "ID";
    public static final String EXERCISE_NAME = "NAME";
    public static final String EXERCISE_ORIENTATION = "ORIENTATION";    //운동방향 DB에 추가
    public static final String EXERCISE_STAGE = "STAGE";                //표본 갯수 DB에 추가
    public static final String EXERCISE_CHEST = "CHEST";
    public static final String EXERCISE_ARM = "ARM";
    public static final String EXERCISE_ABS = "ABS";
    public static final String EXERCISE_SHOULDER = "SHOULDER";
    public static final String EXERCISE_BACK = "BACK";
    public static final String EXERCISE_LEG = "LEG";

    //Schedule table
    public static final String SCHEDULE_TABLE_NAME = "SCHEDULE";
    public static final String SCHEDULE_ID = "ID";
    public static final String SCHEDULE_DATE = "DATE";
    public static final String SCHEDULE_EXERCISE_ID = "EXERCISE_ID";
    public static final String SCHEDULE_SET = "SETS";
    public static final String SCHEDULE_NUMBER = "NUMBER";
    public static final String SCHEDULE_ISDONE = "ISDONE";

    //Character table
    public static final String CHARACTER_TABLE_NAME = "CHARACTER";
    public static final String CHARACTER_DATE = "DATE";
    public static final String CHARACTER_CHEST = "CHEST";
    public static final String CHARACTER_ARM = "ARM";
    public static final String CHARACTER_ABS = "ABS";
    public static final String CHARACTER_SHOULDER = "SHOUDLER";
    public static final String CHARACTER_BACK = "BACK";
    public static final String CHARACTER_LEG = "LEG";

    private SQLiteDatabase db;

    public static SQLiteManager getInstance(Context context){ // 싱글턴 패턴으로 구현하였다.
        if(sqLiteManager == null){
            sqLiteManager = new SQLiteManager(context);
        }

        return sqLiteManager;
    }

    private SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + EXERCISE_TABLE_NAME + " ("
                + EXERCISE_ID + " TEXT PRIMARY KEY, "
                + EXERCISE_NAME + " TEXT, "
                + EXERCISE_ORIENTATION + " INTEGER, "
                + EXERCISE_STAGE + " INTEGER, "                 //추가
                + EXERCISE_CHEST + " INTEGER, "                 //추가
                + EXERCISE_ARM + " INTEGER, "
                + EXERCISE_ABS + " INTEGER, "
                + EXERCISE_SHOULDER + " INTEGER, "
                + EXERCISE_BACK + " INTEGER, "
                + EXERCISE_LEG + " INTEGER"
                + ")");
        db.execSQL("create table if not exists " + SCHEDULE_TABLE_NAME + " ("
                + SCHEDULE_ID + " TEXT PRIMARY KEY, "
                + SCHEDULE_DATE + " TEXT, "
                + SCHEDULE_EXERCISE_ID + " TEXT, "
                + SCHEDULE_SET + " INTEGER, "
                + SCHEDULE_NUMBER + " INTEGER, "
                + SCHEDULE_ISDONE + " INTEGER"
                + ")");
        db.execSQL("create table if not exists " + CHARACTER_TABLE_NAME + " ("
                + CHARACTER_DATE + " TEXT PRIMARY KEY , "
                + CHARACTER_CHEST + " INTEGER, "
                + CHARACTER_ARM + " INTEGER, "
                + CHARACTER_ABS + " INTEGER, "
                + CHARACTER_SHOULDER + " INTEGER, "
                + CHARACTER_BACK + " INTEGER, "
                + CHARACTER_LEG + " INTEGER"
                + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EXERCISE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CHARACTER_TABLE_NAME);
        onCreate(db);
    }
    /**DB 초기화*/
    public boolean init(){
        ArrayList<String> exercises;
        ArrayList<String> chracterstatdate;
        //날짜 포맷 설정
        Date today = new Date();
        SimpleDateFormat dateYearAndMonth = new SimpleDateFormat("yyyy. MM");
        SimpleDateFormat dateDay = new SimpleDateFormat("dd");
        String date_str = dateYearAndMonth.format(today);
        String date_day_str = dateDay.format(today);
        if(sqLiteManager != null){
            exercises = selectAllExerciseName();
            chracterstatdate = selectAllCharacterStatDate();
            if(exercises.size() > 0)
            {
                if(chracterstatdate.size() > 0)       //해당 달에 이미 추가된 데이터값이 있을때
                {
                    //날짜가 1일 일때
                    if(date_day_str.compareTo("01") == 0)
                    {
                        sqLiteManager.insertCharacterData(new CharacterStatData(date_str, 29,29,29,29,29,29));
                    }
                    return false;
                }
                else
                {
                    sqLiteManager.insertCharacterData(new CharacterStatData(date_str, 29,29,29,29,29,29));
                }
            }
            else{
                //능력치 부분 초기화                                                                                        //가슴 팔 복근 어깨 등 다리
                sqLiteManager.insertExerciseData(new ExerciseData("EX4","스쿼트", 0, 2,0,0,1,0,0,2));
                sqLiteManager.insertExerciseData(new ExerciseData("EX3","풀업",0, 2, 0,1,0,1,2,0));
                sqLiteManager.insertExerciseData(new ExerciseData("EX2","윗몸일으키기",0,2,0,0,2,0,0,1));
                sqLiteManager.insertExerciseData(new ExerciseData("EX1","팔굽혀펴기",0,2,2,2,0,2,0,0));
                sqLiteManager.insertExerciseData(new ExerciseData("EX5","프론트사이드레이즈",0,3,0,2,0,2,0,0));

                if(chracterstatdate.size() > 0)       //해당 달에 이미 추가된 데이터값이 있을때
                {
                    //날짜가 1일 일때
                    if(date_day_str.compareTo("01") == 0)
                    {
                        sqLiteManager.insertCharacterData(new CharacterStatData(date_str, 29,29,29,29,29,29));
                    }
                    return false;
                }
                else
                {
                    sqLiteManager.insertCharacterData(new CharacterStatData(date_str, 29,29,29,29,29,29));
                }
            }
            return true;
            }
        else
        {
            return false;
        }
    }
    /**데이터 Insert**/
    //Exercise Data insert
    public boolean insertExerciseData(ExerciseData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXERCISE_ID, data.getId());
        contentValues.put(EXERCISE_NAME, data.getName());
        contentValues.put(EXERCISE_ORIENTATION, data.getScreenorientation());
        contentValues.put(EXERCISE_STAGE, data.getStage());
        contentValues.put(EXERCISE_CHEST, data.getChest());
        contentValues.put(EXERCISE_ARM, data.getArm());
        contentValues.put(EXERCISE_ABS, data.getAbs());
        contentValues.put(EXERCISE_SHOULDER, data.getShoulder());
        contentValues.put(EXERCISE_BACK, data.getBack());
        contentValues.put(EXERCISE_LEG, data.getLeg());
        long result = db.insert(EXERCISE_TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }
    //Schedule Data insert
    public boolean insertScheduleData(ScheduleData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SCHEDULE_ID, data.getId());
        contentValues.put(SCHEDULE_DATE, data.getDate());
        contentValues.put(SCHEDULE_EXERCISE_ID, data.getExercise_id());
        contentValues.put(SCHEDULE_SET, data.getSet());
        contentValues.put(SCHEDULE_NUMBER, data.getNumber());
        contentValues.put(SCHEDULE_ISDONE, data.getIsDone());
        long result = db.insert(SCHEDULE_TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }
    //Schedule Data Update
    public boolean updateScheduleData(ScheduleData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SCHEDULE_DATE, data.getDate());
        contentValues.put(SCHEDULE_EXERCISE_ID, data.getExercise_id());
        contentValues.put(SCHEDULE_SET, data.getSet());
        contentValues.put(SCHEDULE_NUMBER, data.getNumber());
        contentValues.put(SCHEDULE_ISDONE, data.getIsDone());
        long result = db.update(SCHEDULE_TABLE_NAME,contentValues,SCHEDULE_ID + " = " + "\'" + data.getId() + "\'",null);
        if(result == -1)
            return false;
        else
            return true;
    }
    //Character Data insert
    public boolean insertCharacterData(CharacterStatData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHARACTER_DATE, data.getDate());
        contentValues.put(CHARACTER_CHEST, data.getChest());
        contentValues.put(CHARACTER_ARM, data.getArm());
        contentValues.put(CHARACTER_ABS, data.getAbs());
        contentValues.put(CHARACTER_SHOULDER, data.getShoulder());
        contentValues.put(CHARACTER_BACK, data.getBack());
        contentValues.put(CHARACTER_LEG, data.getLeg());
        long result = db.insert(CHARACTER_TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }
    //Character Data update
    public boolean updateCharacterData(CharacterStatData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHARACTER_DATE, data.getDate());
        contentValues.put(CHARACTER_CHEST, data.getChest());
        contentValues.put(CHARACTER_ARM, data.getArm());
        contentValues.put(CHARACTER_ABS, data.getAbs());
        contentValues.put(CHARACTER_SHOULDER, data.getShoulder());
        contentValues.put(CHARACTER_BACK, data.getBack());
        contentValues.put(CHARACTER_LEG, data.getLeg());
        long result = db.update(CHARACTER_TABLE_NAME,contentValues,CHARACTER_DATE + " = " + "\'" + data.getDate() + "\'",null);
        if(result == -1)
            return false;
        else
            return true;
    }

    // 데이터의 존재 유무 확인
    public boolean isExerciseSelectData(String key){
        boolean isData = false;
        String sql = "select * from "+EXERCISE_TABLE_NAME+" where "+EXERCISE_ID+" = "+key+";";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor)객체가 비어있으면 false 리턴
        if(result.moveToFirst()){
            isData = true;
        }
        result.close();
        return isData;
    }

    public boolean isScheduleSelectData(String key){
        boolean isData = false;
        String sql = "select * from "+SCHEDULE_TABLE_NAME+" where "+SCHEDULE_ID+" = "+key+";";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor)객체가 비어있으면 false 리턴
        if(result.moveToFirst()){
            isData = true;
        }
        result.close();
        return isData;
    }

    public boolean isCharacterSelectData(String key){
        boolean isData = false;
        String sql = "select * from "+CHARACTER_TABLE_NAME+" where "+CHARACTER_DATE+" = "+key+";";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor)객체가 비어있으면 false 리턴
        if(result.moveToFirst()){
            isData = true;
        }
        result.close();
        return isData;
    }
    /*
    // 원하는 회차 조회
    public NumberData selectData(int drwno){
        NumberData lottoData = null;
        String sql = "select * from "+TABLE_NAME+" where "+COL_0+" = "+drwno+";";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor)객체가 비어있으면 false 리턴
        if(result.moveToFirst()){
            lottoData = new NumberData();
            lottoData.setDrwNo(result.getInt(1));
            lottoData.setDrwtNo1(result.getInt(2));
            lottoData.setDrwtNo2(result.getInt(3));
            lottoData.setDrwtNo3(result.getInt(4));
            lottoData.setDrwtNo4(result.getInt(5));
            lottoData.setDrwtNo5(result.getInt(6));
            lottoData.setDrwtNo6(result.getInt(7));
            lottoData.setBnusNo(result.getInt(8));
            lottoData.setFirstPrzwnerCo(result.getInt(9));
            lottoData.setFirstWinamnt(result.getLong(10));
            lottoData.setTotSellamnt(result.getLong(11));
            lottoData.setDrwNoDate(result.getString(12));
            result.close();
            return lottoData;
        }
        result.close();
        return lottoData;
    }
    */
    // Exercise 전체 조회
    public List<ExerciseData> selectAll(){
        List<ExerciseData> dataResultList = new ArrayList<ExerciseData>();
        String sql = "select * from "+EXERCISE_TABLE_NAME+" ORDER BY "+EXERCISE_ID+" ASC;";
        Cursor results = db.rawQuery(sql, null);

        if(results.moveToFirst()){
            do{
                ExerciseData exerciseData
                        = new ExerciseData(results.getString(1), results.getString(2), // ID, Name
                        results.getInt(3), results.getInt(4),   //Orientation, Stage
                        results.getInt(5),results.getInt(6),results.getInt(7),      // Chest, Arm, Abs
                        results.getInt(8),results.getInt(9),results.getInt(10));     // Shoulder, Back, Leg
                dataResultList.add(exerciseData);
            }while(results.moveToNext());
        }
        return dataResultList;
    }

    public ArrayList<String> selectAllExerciseName(){
        ArrayList<String> dataResultList = new ArrayList<String>();
        String sql = "select "+EXERCISE_NAME+" from "+EXERCISE_TABLE_NAME+" ORDER BY "+EXERCISE_ID+" DESC;";
        Cursor results = db.rawQuery(sql, null);

        if(results.moveToFirst()){
            do{
                String exerciseData = results.getString(results.getColumnIndex(EXERCISE_NAME));
                dataResultList.add(exerciseData);
            }while(results.moveToNext());
        }
        return dataResultList;
    }

    //Exercise의 id로 부터 이름 조회
    public String selectExerciseNameFromId(String exerciseId){
        String dataResult = new String();
        String sql = "select " + EXERCISE_NAME + " from " + EXERCISE_TABLE_NAME+" where " + EXERCISE_ID + " = \'" + exerciseId+"\' ;";
        Cursor result = db.rawQuery(sql,null);
        if(result.moveToFirst()){
            dataResult = result.getString(0);
        }
        return dataResult;
    }

    //Exercise의 id로 부터 해당 ExerciseData 조회
    public ExerciseData selectExerciseDataFormExerciseName(String exercisename)
    {
        ExerciseData dataResult = new ExerciseData();
        String sql = "select * from "+EXERCISE_TABLE_NAME+" where " + EXERCISE_NAME + " = \'" + exercisename+"\' ;";
        Cursor results = db.rawQuery(sql,null);
        if(results.moveToFirst()){
            ExerciseData exerciseData
                    = new ExerciseData(results.getString(0), results.getString(1), // ID, Name
                    results.getInt(2), results.getInt(3),   //Orientation, Stage
                    results.getInt(4),results.getInt(5),results.getInt(6),      // Chest, Arm, Abs
                    results.getInt(7),results.getInt(8),results.getInt(9));     // Shoulder, Back, Leg

            dataResult = exerciseData;
        }
        return dataResult;
    }

    //Exercise의 id로 부터 orientation값 조회
    public int selectOrientationFromId(String exerciseId){
        int dataResult = 0;
        String sql = "select " + EXERCISE_ORIENTATION + " from " + EXERCISE_TABLE_NAME+" where " + EXERCISE_ID + " = \'" + exerciseId+"\' ;";
        Cursor result = db.rawQuery(sql,null);
        if(result.moveToFirst()){
            dataResult = result.getInt(0);
        }
        return dataResult;
    }

    //Exercise의 이름으로 부터 id조회
    public String selectExerciseIdFromName(String exerciseName){
        String dataResult = new String();
        String sql = "select " + EXERCISE_ID + " from " + EXERCISE_TABLE_NAME+" where " + EXERCISE_NAME + " = \'" + exerciseName+"\' ;";
        Cursor result = db.rawQuery(sql,null);
        if(result.moveToFirst()){
            dataResult = result.getString(0);
        }
        return dataResult;
    }
    //날짜로 스케줄 조회
    public ArrayList<ScheduleData> selectScheduleFromDate(String date){
        ArrayList<ScheduleData> dataResultList = new ArrayList<ScheduleData>();
        String sql = "select * from "+SCHEDULE_TABLE_NAME+" where "+ SCHEDULE_DATE + " = \'" + date + "\';";
        Cursor results = db.rawQuery(sql,null);

        if(results.moveToFirst()){
            do{
                ScheduleData scheduleData
                        = new ScheduleData(results.getString(0), results.getString(1), results.getString(2), // ID, Date, Exercise_ID
                        results.getInt(3),results.getInt(4),results.getInt(5));
                dataResultList.add(scheduleData);
            }while(results.moveToNext());
        }
        return dataResultList;
    }

    // 스케줄 삭제
    public int deleteScheduleRecord(String data){
        int deleteRecordCnt = db.delete(SCHEDULE_TABLE_NAME, SCHEDULE_ID + " = " + "\'" + data + "\'", null);

        return deleteRecordCnt;
    }

    public ArrayList<String> selectAllCharacterStatDate(){
        ArrayList<String> dataResultList = new ArrayList<String>();
        String sql = "select "+CHARACTER_DATE+" from "+CHARACTER_TABLE_NAME+" ORDER BY "+CHARACTER_DATE+" DESC;";
        Cursor results = db.rawQuery(sql, null);

        if(results.moveToFirst()){
            do{
                String CharacterStatDate = results.getString(results.getColumnIndex(CHARACTER_DATE));
                dataResultList.add(CharacterStatDate);
            }while(results.moveToNext());
        }
        return dataResultList;
    }

    //날짜로 CharacterStatData 조회
    public CharacterStatData selectCharacterStatDataFromDate(String date){
        CharacterStatData dataResult = new CharacterStatData();
        String sql = "select * from "+CHARACTER_TABLE_NAME+" where "+ CHARACTER_DATE + " = \'" + date + "\';";
        Cursor results = db.rawQuery(sql,null);

        if(results.moveToFirst()){
            CharacterStatData characterstatdata = new CharacterStatData(results.getString(0), results.getInt(1), results.getInt(2), // Date, Chest, Arm
                    results.getInt(3),results.getInt(4),results.getInt(5),results.getInt(6));  //Abs, Shoulder, back, legs
            dataResult = characterstatdata;
        }
        return dataResult;
    }
}
