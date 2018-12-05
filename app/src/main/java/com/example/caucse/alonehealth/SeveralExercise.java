package com.example.caucse.alonehealth;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Locale;
import static android.speech.tts.TextToSpeech.ERROR;

public class SeveralExercise extends AppCompatActivity implements  CameraBridgeViewBase.CvCameraViewListener2{
    //initialize
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInversion;
    private Mat matGray;
    private Mat matInput;
    MatCirCularQueue frameBuffer;

    //////
    private Button startButton;

    private TextView exercisename_Text;
    private TextView exerciseCountTextView;
    private TextView exerciseSetNumTextView;
    private TextView exerciseCurSetNumTextView;
    private TextToSpeech tts;
    private ImageView exercise_Guide;
    private ImageView guide_Exit;
    private ImageView exercise_Exit;

    //표본 Mat
    private Mat matFirstSample;
    private Mat matSecondSample;
    private Mat matThirdSample;
    //표본 간 차이
    int max_difference12 = 0, max_difference23 = 0, max_difference31 = 0;

    //user state
    int user_state;
    private final static int INIT_STATE = -1;
    private final static int START_STATE = 0;
    private final static int FIRST_STATE = 1;
    private final static int SECOND_STATE = 2;
    private final static int THIRD_STATE = 3;
    private final static int READY_STATE = 4;
    private final static int COUNT_STATE = 5;
    private final static int REST_STATE = 6;
    private final static int EXIT_STATE = 7;

    private int count_state;
    private final static int FIRST_POSITION = 1;
    private final static int SECOND_POSITION = 2;
    private final static int THIRD_POSITION = 3;

    //쓰레드 핸들러
    Handler mHandler = null;

    //딜레이 카운트
    private int count = 7;

    //운동
    int exercise_set, exercise_count;
    String exercise_name;
    String exercise_date;    //운동 날짜
    String characterdate;   //캐릭터 날짜
    String id;
    //현재 운동 횟수, 세트수
    int current_count = 0, current_set = 0;


    //time interval
    int exercisePrepareInterval = 7;
    int samplingInterval = 4;
    int setInterval = 10;


    /**TTS 브로드캐스트 리시버*/
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;
    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public native void InvertMat(long matAddrInput, long matAddrResult);
    public native int CountWhitePixels(long matAddrInput);
    public native void CreateDifferenceImage(long matAddrSource, long matAddrTarget, long matAddrResult);
    public native int CountWhitePixelsInOneRow(long matAddrInput, int indexOfStart, int indexOfEnd);

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_several);
        frameBuffer = new MatCirCularQueue();

        /**TTS 브로드캐스트 리시버*/
        intentFilter = new IntentFilter();
        intentFilter.addAction(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String act = intent.getAction();
                if(act.equals(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)){
                    SeveralExercise.TimerThread timerThread;
                    switch(user_state){
                        case INIT_STATE:
                            user_state = START_STATE;
                            timerThread = new SeveralExercise.TimerThread();
                            count = exercisePrepareInterval;
                            timerThread.start();
                            break;
                        case START_STATE:
                            user_state = FIRST_STATE;
                            timerThread = new SeveralExercise.TimerThread();
                            count = samplingInterval;
                            timerThread.start();
                            break;
                        case FIRST_STATE:
                            user_state = SECOND_STATE;
                            timerThread = new SeveralExercise.TimerThread();
                            count = samplingInterval;
                            timerThread.start();
                            break;
                        case SECOND_STATE:
                            user_state = THIRD_STATE;
                            timerThread = new SeveralExercise.TimerThread();
                            count = samplingInterval;
                            timerThread.start();
                            break;
                        case READY_STATE:
                            user_state = COUNT_STATE;
                            mHandler.sendEmptyMessage(0);
                            break;
                    }
                    unregisterReceiver(broadcastReceiver);
                }
            }
        };

        //전자녀
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);


        //상태 초기화
        user_state = INIT_STATE;
        count_state = FIRST_POSITION;

        //운동 이름, set 수 넘겨 받기
        Intent intent = getIntent();
        exercise_name = intent.getExtras().getString("Exercise");
        exercise_count = intent.getExtras().getInt("Number");
        exercise_set = intent.getExtras().getInt("Set");
        exercise_date = intent.getExtras().getString("DATE");
        characterdate = intent.getExtras().getString("CHARACTERDATE");
        id = intent.getExtras().getString("ID");

        startButton = (Button) findViewById(R.id.start_button);
        exercisename_Text = (TextView) findViewById(R.id.exerciseName);
        exerciseCountTextView = (TextView)findViewById(R.id.count_textview);
        exerciseSetNumTextView = (TextView)findViewById(R.id.exerciseSetNum);
        exercise_Guide = (ImageView) findViewById(R.id.exercise_guide);
        guide_Exit = (ImageView) findViewById(R.id.guide_exit);
        exercise_Exit = (ImageView) findViewById(R.id.exercise_exit);
        exerciseCurSetNumTextView  = (TextView) findViewById(R.id.exerciseCurSetNum);

        exercisename_Text.setText(exercise_name);
        exerciseSetNumTextView.setText(String.format("목표 : %d 세트 %d 개",exercise_set,exercise_count));
        exerciseCurSetNumTextView.setText(String.format("현재 : %d 세트 %d 개",current_set,current_count));
        exerciseCountTextView.setText("운동 준비");


        //시작 버튼
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(startButton.GONE);
                exercise_Guide.setVisibility(exercise_Guide.VISIBLE);
                guide_Exit.setVisibility(guide_Exit.VISIBLE);
            }
        });

        guide_Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                exercise_Guide.setVisibility(exercise_Guide.GONE);
                guide_Exit.setVisibility(guide_Exit.GONE);
                if(user_state == INIT_STATE){
                    startButton.setVisibility(View.GONE);
                    tts.setPitch(1.0f);
                    tts.setSpeechRate(1.0f);
                    tts.speak(String.format("운동을 시작합니다. %d초 안에 자세를 취해주세요",exercisePrepareInterval), TextToSpeech.QUEUE_FLUSH, null);
                    exerciseCountTextView.setVisibility(View.VISIBLE);
                    registerReceiver(broadcastReceiver,intentFilter);
                }
            }
        });

        //화면 클릭 시 정지 버튼 활성화
        mOpenCvCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility() == View.GONE) {
                    if(exercise_Exit.getVisibility() == View.INVISIBLE) {
                        exercise_Exit.setVisibility(View.VISIBLE);
                        exerciseCountTextView.setVisibility(View.INVISIBLE);
                    }
                    else if(exercise_Exit.getVisibility() == View.VISIBLE){
                        exercise_Exit.setVisibility(View.INVISIBLE);
                        exerciseCountTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //운동 정지 확인
        exercise_Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exercisename_Text.bringToFront();
                AlertDialog.Builder builder = new AlertDialog.Builder(SeveralExercise.this)
                        .setTitle("운동 중지")
                        .setMessage("운동을 그만 두시겠습니까?").setPositiveButton("예", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dlg, int value) {
                                Intent intent = new Intent(getApplicationContext(),
                                        MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                user_state = INIT_STATE;
                                tts.shutdown();
                                startActivity(intent);

                            }
                        })
                        .setNegativeButton("아니요", null);
                builder.show();
            }
        });

        //UI처리 쓰레드 핸들러
        mHandler = new Handler(){
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    switch (user_state) {
                        case INIT_STATE:
                            break;
                        case START_STATE:
                            exerciseCountTextView.setText("1 번째 자세");
                            tts.speak(String.format("표본 추출을 위해 운동 첫번째 자세를 취해 주세요.",samplingInterval), TextToSpeech.QUEUE_FLUSH, null);
                            registerReceiver(broadcastReceiver,intentFilter);
                            break;
                        case FIRST_STATE:
                            exerciseCountTextView.setText("2 번째 자세");
                            tts.speak(String.format("운동 두번쨰 자세를 취해 주세요.",samplingInterval), TextToSpeech.QUEUE_FLUSH, null);
                            registerReceiver(broadcastReceiver,intentFilter);
                            break;
                        case SECOND_STATE:
                            exerciseCountTextView.setText("3 번째 자세");
                            tts.speak(String.format("운동 세번째 자세를 취해 주세요.",samplingInterval), TextToSpeech.QUEUE_FLUSH, null);
                            registerReceiver(broadcastReceiver,intentFilter);
                            break;
                        case THIRD_STATE:
                            user_state = READY_STATE;
                            exerciseCountTextView.setText("표본추출 완료");
                            tts.speak(String.format("표본 추출이 완료되었습니다.",samplingInterval), TextToSpeech.QUEUE_FLUSH, null);
                            registerReceiver(broadcastReceiver,intentFilter);
                            TimerThread timerThread = new TimerThread();
                            count = samplingInterval;
                            timerThread.start();
                            break;
                        case READY_STATE:
                            ///////////////////
                            Mat matDiff = new Mat(matGray.rows(), matGray.cols(), matGray.type());
                            CreateDifferenceImage(matFirstSample.getNativeObjAddr(), matSecondSample.getNativeObjAddr(), matDiff.getNativeObjAddr());
                            max_difference12 = CountWhitePixels(matDiff.getNativeObjAddr());
                            CreateDifferenceImage(matSecondSample.getNativeObjAddr(), matThirdSample.getNativeObjAddr(), matDiff.getNativeObjAddr());
                            max_difference23 = CountWhitePixels(matDiff.getNativeObjAddr());
                            CreateDifferenceImage(matThirdSample.getNativeObjAddr(), matFirstSample.getNativeObjAddr(), matDiff.getNativeObjAddr());
                            max_difference31 = CountWhitePixels(matDiff.getNativeObjAddr());
                            exerciseCountTextView.setText("운동 시작");
                            tts.speak(String.format("운동을 시작해주세요."), TextToSpeech.QUEUE_FLUSH, null);
                            registerReceiver(broadcastReceiver,intentFilter);
                            break;
                        case COUNT_STATE:
                            if (current_count >= exercise_count) {
                                current_set++;
                                exerciseCountTextView.setTextSize(100);
                                exerciseCountTextView.setText(current_count);
                                exerciseCurSetNumTextView.setText(String.format("현재 : %d 세트 %d 개",current_set,current_count));
                                if (current_set >= exercise_set) {
                                    user_state = EXIT_STATE;
                                    tts.speak(String.format("운동이 완료되었습니다. ",setInterval), TextToSpeech.QUEUE_FLUSH, null);
                                    TimerThread timerThread2 = new TimerThread();
                                    count = 3;
                                    timerThread2.start();
                                } else {
                                    exerciseCountTextView.setTextSize(50);
                                    exerciseCountTextView.setText("세트 완료");
                                    tts.speak(String.format("%d 세트가 완료되었습니다. %d초간 휴식을 취해주세요. ", current_set, setInterval), TextToSpeech.QUEUE_FLUSH, null);
                                    user_state = REST_STATE;
                                    count = setInterval + 5;
                                    TimerThread timerThread1 = new TimerThread();
                                    timerThread1.start();
                                    current_count = 0;
                                }
                            }
                            break;
                        case REST_STATE:
                            exerciseCountTextView.setText("운동 시작");
                            tts.speak(String.format("운동을 다시 시작해주세요. ",setInterval), TextToSpeech.QUEUE_FLUSH, null);
                            current_count = 0;
                            user_state = COUNT_STATE;
                            break;
                        case EXIT_STATE:
                            //부위 능력치 변화
                            ExerciseData thisExercise = SQLiteManager.sqLiteManager.selectExerciseDataFormExerciseName(exercise_name);
                            CharacterStatData ThisMonthCharacterStat = new CharacterStatData();
                            ThisMonthCharacterStat = SQLiteManager.sqLiteManager.selectCharacterStatDataFromDate(characterdate);

                            int totalTimes = exercise_set * exercise_count;     //운동의 총 횟수
                            int arm = 0;
                            int shoulder=0;
                            int back = 0;
                            int chest = 0;
                            int abs = 0;
                            int legs = 0;
                            if(thisExercise.getArm() != 0)
                                arm = (int)(Math.ceil(ThisMonthCharacterStat.getArm() + thisExercise.getArm()*totalTimes*0.02));
                            else
                                arm = ThisMonthCharacterStat.getArm();

                            if(thisExercise.getShoulder() != 0)
                                shoulder = (int)(Math.ceil(ThisMonthCharacterStat.getShoulder() + thisExercise.getShoulder()*totalTimes*0.02));
                            else
                                shoulder = ThisMonthCharacterStat.getShoulder();

                            if(thisExercise.getBack() != 0)
                                back = (int)(Math.ceil(ThisMonthCharacterStat.getBack() + thisExercise.getBack()*totalTimes*0.02));
                            else
                                back = ThisMonthCharacterStat.getBack();

                            if(thisExercise.getChest() != 0)
                                chest = (int)(Math.ceil(ThisMonthCharacterStat.getChest() + thisExercise.getChest()*totalTimes*0.02));
                            else
                                chest = ThisMonthCharacterStat.getChest();

                            if(thisExercise.getAbs() != 0)
                                abs = (int)(Math.ceil(ThisMonthCharacterStat.getAbs() + thisExercise.getAbs()*totalTimes*0.02));
                            else
                                abs = ThisMonthCharacterStat.getAbs();

                            if(thisExercise.getLeg() != 0)
                                legs = (int)(Math.ceil(ThisMonthCharacterStat.getLeg() + thisExercise.getLeg()*totalTimes*0.02));
                            else
                                legs = ThisMonthCharacterStat.getLeg();

                            SQLiteManager.sqLiteManager.updateCharacterData(new CharacterStatData(characterdate,
                                    chest, arm, abs, shoulder, back,legs));

                            SQLiteManager.sqLiteManager.updateScheduleData(new ScheduleData(id,exercise_date,thisExercise.getId(),exercise_set,exercise_count,1 ));
                            user_state = INIT_STATE;
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            break;
                    }
                }
            }
        };
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    public void onDestroy() {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onDestroy();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        matInput = inputFrame.rgba();
        if(matInversion == null)
            matInversion = new Mat(matInput.rows(),matInput.cols(),matInput.type());
        InvertMat(matInput.getNativeObjAddr(),matInversion.getNativeObjAddr());
        if(matGray == null)
            matGray = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        ConvertRGBtoGray(matInversion.getNativeObjAddr(), matGray.getNativeObjAddr());

        if(!tts.isSpeaking()) {
            switch (user_state) {
                case INIT_STATE:
                    break;
                case START_STATE:
                    if (matFirstSample == null) {
                        matFirstSample = matGray.clone();
                    }
                    break;
                case SECOND_STATE:
                    if (matSecondSample == null) {
                        matSecondSample = matGray.clone();
                    }
                    break;
                case THIRD_STATE:
                    if (matThirdSample == null) {
                        matThirdSample = matGray.clone();
                    }
                    break;
                case READY_STATE:
                    break;
                case COUNT_STATE:
                    frameBuffer.Enqueue(matGray);
                    CountingThread countingThread = new CountingThread();
                    countingThread.start();
                    break;

            }
        }
        return matInversion;
    }

    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA"};

    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( SeveralExercise.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }


    public class TimerThread extends Thread{
        public void run(){
            while(count > 0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count --;
            }
            mHandler.sendEmptyMessage(0);
        }
    }

    public class CountingThread extends Thread{
        public void run(){
            if(user_state == COUNT_STATE) {
                countUsingDiff();
            }
        }
    }

    public void countUsingDiff(){
        Mat matFrame = frameBuffer.Dequeue();
        Mat matFirstDiff= new Mat(matFrame.rows(), matFrame.cols(), matFrame.type());
        Mat matSecondDiff= new Mat(matFrame.rows(), matFrame.cols(), matFrame.type());
        Mat matThirdDiff= new Mat(matFrame.rows(), matFrame.cols(), matFrame.type());

        CreateDifferenceImage(matFrame.getNativeObjAddr(), matFirstSample.getNativeObjAddr(), matFirstDiff.getNativeObjAddr());
        CreateDifferenceImage(matFrame.getNativeObjAddr(), matSecondSample.getNativeObjAddr(), matSecondDiff.getNativeObjAddr());
        CreateDifferenceImage(matFrame.getNativeObjAddr(), matThirdSample.getNativeObjAddr(), matThirdDiff.getNativeObjAddr());

        int differenceWithFirst = sumOfWhitePixels(matFirstDiff,8);
        int differenceWithSecond = sumOfWhitePixels(matSecondDiff,8);
        int differenceWithThird = sumOfWhitePixels(matThirdDiff,8);
        counting(differenceWithFirst,differenceWithSecond, differenceWithThird);
    }

    private synchronized void counting(int differenceWithFirst,int differenceWithSecond,int differenceWithThird){
        switch(count_state) {
            case FIRST_POSITION:
                if (differenceWithSecond < max_difference12 / 3) {
                    count_state = SECOND_POSITION;
                }
                break;
            case SECOND_POSITION:
                if (differenceWithThird < max_difference23 / 3) {
                    count_state = THIRD_POSITION;
                }
                break;
            case THIRD_POSITION:
                if (differenceWithFirst < max_difference31 / 3) {
                    current_count++;
                    count_state = FIRST_POSITION;
                    if(current_count >= exercise_count) {
                        TimerThread timerThread = new TimerThread();
                        count = 1;
                        timerThread.start();
                    }
                    else
                        mHandler.sendEmptyMessage(0);
                    //exercisecount_Text.setText(current_set + " SET " + current_count);

                    tts.speak(String.format("%d",current_count), TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
        }
    }

    class CountPixelThread extends Thread{
        int indexOfStart;
        int indexOfEnd;
        long matInput;
        int ans;
        CountPixelThread(long matInput, int start, int end){ this.matInput = matInput; this.indexOfStart = start; this.indexOfEnd = end; this.ans = 0;}

        @Override
        public void run() {
            this.ans = CountWhitePixelsInOneRow(matInput,indexOfStart,indexOfEnd);
        }

    }

    private int sumOfWhitePixels(Mat mSource, int numberOfThread){
        int sum = 0;
        CountPixelThread countPixelThread[] = new CountPixelThread[numberOfThread];
        for(int i = 0; i < numberOfThread; i ++){
            countPixelThread[i] = new CountPixelThread(mSource.getNativeObjAddr(),(i*mSource.rows())/numberOfThread,((i+1)*mSource.rows())/numberOfThread);
            countPixelThread[i].start();
        }
        for(int i = 0; i < numberOfThread; i++){
            try {
                countPixelThread[i].join();
                sum += countPixelThread[i].ans;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return sum;
    }

    @Override
    public void onBackPressed() {

    }
}