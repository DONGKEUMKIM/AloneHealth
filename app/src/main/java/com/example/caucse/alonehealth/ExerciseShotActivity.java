package com.example.caucse.alonehealth;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
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
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class ExerciseShotActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {
    //sampling variable
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInversion;
    private Mat matGray;
    private Mat matInput;
    MatCirCularQueue frameBuffer;
    //테스트 시작 버튼
    private Button startTestButton;
    //테스트 로그 텍스트뷰
    private TextView testStateTextView;
    //딜레이 카운트
    private int count = 7;
    //쓰레드 핸들러
    Handler mHandler = null;
    //타이머 쓰레드
    TimerThread timerThread;
    //표본 Mat
    private Mat mStartSample;
    private Mat mEndSample;
    int max_difference = 0;
    // 운동 진행 state
    private final static int INIT = -1;
    private final static int START_EXERCISE = 0;
    private final static int START_SAMPLING = 1;
    private final static int END_SAMPLING = 2;
    private final static int TOWARD_SAMPLING = 3;
    private final static int TOWARD_EXERCISE = 4;
    private final static int COMPLETE_EXERCISE = 5;
    private final static int REST_EXERCISE = 6;
    int progress = INIT;
    ///////////////////////////////////
    Button startButton;
    ImageView stopImage;
    TextView exercisetext, settext;
    TextView currentSetNumberTextView;
    TextToSpeech tts;
    int set, num;
    String exercise;
    String date;
    //time interval
    int exercisePrepareInterval = 7;
    int samplingInterval = 4;
    int setInterval = 10;

    //운동 카운트 플래그
    boolean isFirstPosition = true;
    boolean isLastPosition = false;
    int exercise_count = -1;
    int exercise_set = 0;

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
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_shot);
        frameBuffer = new MatCirCularQueue();

        /**TTS 브로드캐스트 리시버*/
        intentFilter = new IntentFilter();
        intentFilter.addAction(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String act = intent.getAction();
                if(act.equals(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)){
                    TimerThread timerThread;
                    switch(progress){
                        case INIT:
                            progress = START_EXERCISE;
                            timerThread = new TimerThread();
                            count = exercisePrepareInterval;
                            timerThread.start();
                            break;
                        case START_EXERCISE:
                            progress = START_SAMPLING;
                            timerThread = new TimerThread();
                            count = samplingInterval;
                            timerThread.start();
                            break;
                        case START_SAMPLING:
                            progress = END_SAMPLING;
                            timerThread = new TimerThread();
                            count = samplingInterval;
                            timerThread.start();
                            break;
                    }
                    unregisterReceiver(broadcastReceiver);
                    Toast.makeText( ExerciseShotActivity.this , String.format("Progress : %d",progress), Toast.LENGTH_SHORT ).show();
                }
            }
        };


        //운동 이름 text view
        exercisetext = (TextView) findViewById(R.id.exercisetext);
        //운동 set 수 text view
        settext = (TextView) findViewById(R.id.settext);
        currentSetNumberTextView = (TextView)findViewById(R.id.current_set_number);

        //운동 이름, set 수 넘겨 받기
        Intent intent = getIntent();
        /**
         * 운동 이름 셋트 횟수 셋팅
         */
        exercise = intent.getExtras().getString("Exercise");
        num = intent.getExtras().getInt("Number");
        set = intent.getExtras().getInt("Set");
        date = intent.getExtras().getString("DATE");
        exercisetext.setText(exercise);
        settext.setText(set + " SET " + num);

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

        //시작 버튼 & 정지 이미지
        startButton = (Button) findViewById(R.id.startbutton);
        stopImage = (ImageView) findViewById(R.id.stop);

        /**운동 시작*/
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(startButton.GONE);
                tts.setPitch(1.0f);
                tts.setSpeechRate(1.0f);
                tts.speak(String.format("운동을 시작합니다. %d초 안에 자세를 취해주세요",exercisePrepareInterval), TextToSpeech.QUEUE_FLUSH, null);
                registerReceiver(broadcastReceiver,intentFilter);
            }
        });

        mOpenCvCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility() == startButton.GONE) {
                    stopImage.setVisibility(stopImage.VISIBLE);
                    stopImage.bringToFront();
                }
            }
        });

        stopImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseShotActivity.this)
                        .setTitle("운동 중지")
                        .setMessage("운동을 그만 두시겠습니까?").setPositiveButton("예", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dlg, int value) {
                                Intent intent = new Intent(getApplicationContext(),
                                        MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                progress = INIT;
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
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    switch(progress){
                        case START_EXERCISE:
                            tts.speak(String.format("표본 추출을 위해 운동 시작 자세를 취해 주세요.",samplingInterval), TextToSpeech.QUEUE_FLUSH, null);
                            registerReceiver(broadcastReceiver,intentFilter);
                            break;
                        case START_SAMPLING:
                            tts.speak(String.format("운동 끝 자세를 취해 주세요.",samplingInterval), TextToSpeech.QUEUE_FLUSH, null);
                            registerReceiver(broadcastReceiver,intentFilter);
                            break;
                        case END_SAMPLING:
                            progress = TOWARD_SAMPLING;
                            tts.speak(String.format("표본 추출이 완료되었습니다.",samplingInterval), TextToSpeech.QUEUE_FLUSH, null);
                            TimerThread timerThread = new TimerThread();
                            count = samplingInterval;
                            timerThread.start();
                            break;
                        case TOWARD_SAMPLING:
                            Mat matDiff = new Mat(matGray.rows(), matGray.cols(), matGray.type());
                            CreateDifferenceImage(mStartSample.getNativeObjAddr(), mEndSample.getNativeObjAddr(), matDiff.getNativeObjAddr());
                            max_difference = CountWhitePixels(matDiff.getNativeObjAddr());
                            tts.speak(String.format("운동을 시작해주세요."), TextToSpeech.QUEUE_FLUSH, null);
                            progress = TOWARD_EXERCISE;
                            break;
                        case TOWARD_EXERCISE:
                            if(exercise_count >= num){
                                if(exercise_set >= set){
                                    progress = COMPLETE_EXERCISE;
                                    tts.speak(String.format("운동이 완료되었습니다. ",setInterval), TextToSpeech.QUEUE_FLUSH, null);
                                    TimerThread timerThread2 = new TimerThread();
                                    count = 3;
                                    timerThread2.start();
                                }
                                else{
                                    tts.speak(String.format("한 세트가 완료되었습니다. %d초간 휴식을 취해주세요. ",setInterval), TextToSpeech.QUEUE_FLUSH, null);
                                    progress = REST_EXERCISE;
                                    count = setInterval + 5;
                                    TimerThread timerThread1 = new TimerThread();
                                    timerThread1.start();
                                    exercise_count = 0;

                                }
                            }
                            currentSetNumberTextView.setText(String.format("%d SET %d",exercise_set+1,exercise_count));
                            break;
                        case REST_EXERCISE:
                            tts.speak(String.format("운동을 다시 시작해주세요. ",setInterval), TextToSpeech.QUEUE_FLUSH, null);
                            progress = TOWARD_EXERCISE;
                            break;
                        case COMPLETE_EXERCISE:

                            //부위 능력치 변화
                            ExerciseData thisExercise = SQLiteManager.sqLiteManager.selectExerciseDataFormExerciseName(exercise);
                            CharacterStatData ThisMonthCharacterStat = new CharacterStatData();
                            ThisMonthCharacterStat = SQLiteManager.sqLiteManager.selectCharacterStatDataFromDate(date);

                            int totalTimes = set * num;     //운동의 총 횟수
                            int arm = 0;
                            int shoulder=0;
                            int back = 0;
                            int chest = 0;
                            int abs = 0;
                            int legs = 0;
                            if(thisExercise.getArm() != 0)
                                arm = (int)(ThisMonthCharacterStat.getArm() + thisExercise.getArm()*totalTimes*0.02);
                            else
                                arm = ThisMonthCharacterStat.getArm();

                            if(thisExercise.getShoulder() != 0)
                                shoulder = (int)(ThisMonthCharacterStat.getShoulder() + thisExercise.getShoulder()*totalTimes*0.02);
                            else
                                shoulder = ThisMonthCharacterStat.getShoulder();

                            if(thisExercise.getBack() != 0)
                                back = (int)(ThisMonthCharacterStat.getBack() + thisExercise.getBack()*totalTimes*0.02);
                            else
                                back = ThisMonthCharacterStat.getBack();

                            if(thisExercise.getChest() != 0)
                                chest = (int)(ThisMonthCharacterStat.getChest() + thisExercise.getChest()*totalTimes*0.02);
                            else
                                chest = ThisMonthCharacterStat.getChest();

                            if(thisExercise.getAbs() != 0)
                                abs = (int)(ThisMonthCharacterStat.getAbs() + thisExercise.getAbs()*totalTimes*0.02);
                            else
                                abs = ThisMonthCharacterStat.getAbs();

                            if(thisExercise.getLeg() != 0)
                                legs = (int)(ThisMonthCharacterStat.getLeg() + thisExercise.getLeg()*totalTimes*0.02);
                            else
                                legs = ThisMonthCharacterStat.getLeg();

                            SQLiteManager.sqLiteManager.updateCharacterData(new CharacterStatData(date,
                                    chest, arm, abs, shoulder, back,legs));


                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                    }
                }


            }
        };

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        tts.shutdown();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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
            switch (progress) {
                case START_EXERCISE:
                    break;
                case START_SAMPLING:
                    mStartSample = matGray.clone();
                    break;
                case END_SAMPLING:
                    mEndSample = matGray.clone();
                    break;
                case TOWARD_SAMPLING:
                /*SamplingThread samplingThread = new SamplingThread();
                samplingThread.start();*/
                    break;
                case TOWARD_EXERCISE:
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
    String[] PERMISSIONS = {"android.permission.CAMERA"};

    private boolean hasPermissions(String[] permissions) {
        int result;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions) {
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED) {
                //허가 안된 퍼미션 발견
                return false;
            }
        }
        //모든 퍼미션이 허가되었음
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseShotActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(getBaseContext(), "resultCode : " + resultCode, Toast.LENGTH_SHORT).show();
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
            if(progress == TOWARD_EXERCISE) {
                countUsingDiff();
            }
        }
    }
    public void countUsingDiff(){
        Mat matFrame = frameBuffer.Dequeue();
        Mat matStartDiff= new Mat(matFrame.rows(), matFrame.cols(), matFrame.type());
        Mat matEndDiff= new Mat(matFrame.rows(), matFrame.cols(), matFrame.type());

        CreateDifferenceImage(matFrame.getNativeObjAddr(), mEndSample.getNativeObjAddr(), matEndDiff.getNativeObjAddr());
        CreateDifferenceImage(matFrame.getNativeObjAddr(), mStartSample.getNativeObjAddr(), matStartDiff.getNativeObjAddr());

        int differenceWithStart = sumOfWhitePixels(matStartDiff,8);
        int differenceWithEnd = sumOfWhitePixels(matEndDiff,8);
        counting(differenceWithStart,differenceWithEnd);
    }
    private synchronized void counting(int differenceWithStart, int differenceWithEnd){
        if(isFirstPosition){
            if(differenceWithEnd < max_difference / 10){
                isFirstPosition = false;
                isLastPosition = true;
            }

        }
        else if(isLastPosition){
            if(differenceWithStart < max_difference / 10){
                exercise_count++;
                isFirstPosition = true;
                isLastPosition = false;
                if(exercise_count >= num){
                    TimerThread timerThread = new TimerThread();
                    count = 1;
                    timerThread.start();
                }
                else
                    mHandler.sendEmptyMessage(0);
                tts.speak(String.format("%d",exercise_count), TextToSpeech.QUEUE_FLUSH, null);

            }
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
}