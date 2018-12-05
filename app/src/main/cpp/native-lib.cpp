#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_MainActivity_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                                  jlong matAddrInput,
                                                                  jlong matAddrResult) {

    // TODO
// 입력 RGBA 이미지를 GRAY 이미지로 변환
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_ExerciseShotActivity_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                                jlong matAddrInput,
                                                                jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;

    Mat &matResult = *(Mat *)matAddrResult;


    cvtColor(matInput, matResult, CV_RGBA2GRAY);


}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_PictureSamplingTest_ConvertRGBtoGray(JNIEnv *env,
                                                                         jobject instance,
                                                                         jlong matAddrInput,
                                                                         jlong matAddrResult) {

    // TODO

    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;
    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_PictureSamplingTest_InvertMat(JNIEnv *env, jobject instance,
                                                                  jlong matAddrInput,
                                                                  jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;
    flip(matInput,matResult,1);

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_ExerciseShotActivity_InvertMat(JNIEnv *env, jobject instance,
                                                                   jlong matAddrInput,
                                                                   jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;
    flip(matInput,matResult,1);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_DifferenceImageTest_ConvertRGBtoGray(JNIEnv *env,
                                                                         jobject instance,
                                                                         jlong matAddrInput,
                                                                         jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;
    cvtColor(matInput, matResult, CV_RGBA2GRAY);

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_DifferenceImageTest_InvertMat(JNIEnv *env, jobject instance,
                                                                  jlong matAddrInput,
                                                                  jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;
    flip(matInput,matResult,1);

}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_caucse_alonehealth_DifferenceImageTest_CountWhitePixels(JNIEnv *env,
                                                                         jobject instance,
                                                                         jlong matAddrInput) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    int count = 0;
    uchar r,g,b;

    for(int y = 0; y < matInput.rows; ++y){
        //y번째 줄에서 첫 번째 픽셀에 대한 포인터
        Vec3b* pixel = matInput.ptr<Vec3b>(y);
        for(int x = 0; x < matInput.cols; ++x){
            //픽셀에서 값 가져오기
            r = pixel[x][2];
            g = pixel[x][1];
            b = pixel[x][0];

            if(r == 255 && g == 255 && b == 255)
                count++;
        }
    }

    return count;

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_DifferenceImageTest_CreateDifferenceImage(JNIEnv *env,
                                                                              jobject instance,
                                                                              jlong matAddrSource,
                                                                              jlong matAddrTarget,
                                                                              jlong matAddrResult) {

    // TODO
    IplImage matSource = *(Mat *)matAddrSource;
    IplImage matTarget = *(Mat *)matAddrTarget;
    Mat &matResult = *(Mat *)matAddrResult;
    IplImage* sourceImage;
    IplImage* targetImage;
    IplImage* resultImage;

    //Mat -> IplImage 변환
    IplImage copy;
    copy = matResult;
    resultImage = &copy;
    sourceImage = &matSource;
    targetImage = &matTarget;

    // 차영상 생성
    cvAbsDiff(sourceImage,targetImage,resultImage);
    cvThreshold(resultImage,resultImage,50,255,CV_THRESH_BINARY);

    //IplImage -> Mat 변환
    matResult = cvarrToMat(resultImage);

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_ComplexImageTest_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                                      jlong matAddrInput,
                                                                      jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;
    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_ComplexImageTest_InvertMat(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;
    flip(matInput,matResult,1);
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_caucse_alonehealth_ComplexImageTest_CountWhitePixels(JNIEnv *env, jobject instance,
                                                                      jlong matAddrInput) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    int count = 0;
    uchar r,g,b;

    for(int y = 0; y < matInput.rows; ++y){
        //y번째 줄에서 첫 번째 픽셀에 대한 포인터
        Vec3b* pixel = matInput.ptr<Vec3b>(y);
        for(int x = 0; x < matInput.cols; ++x){
            //픽셀에서 값 가져오기
            r = pixel[x][2];
            g = pixel[x][1];
            b = pixel[x][0];

            if(r == 255 && g == 255 && b == 255)
                count++;
        }
    }

    return count;

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_ComplexImageTest_CreateDifferenceImage(JNIEnv *env,
                                                                           jobject instance,
                                                                           jlong matAddrSource,
                                                                           jlong matAddrTarget,
                                                                           jlong matAddrResult) {

    // TODO
    IplImage matSource = *(Mat *)matAddrSource;
    IplImage matTarget = *(Mat *)matAddrTarget;
    Mat &matResult = *(Mat *)matAddrResult;
    IplImage* sourceImage;
    IplImage* targetImage;
    IplImage* resultImage;

    //Mat -> IplImage 변환
    IplImage copy;
    copy = matResult;
    resultImage = &copy;
    sourceImage = &matSource;
    targetImage = &matTarget;

    // 차영상 생성
    cvAbsDiff(sourceImage,targetImage,resultImage);
    cvThreshold(resultImage,resultImage,50,255,CV_THRESH_BINARY);

    //IplImage -> Mat 변환
    matResult = cvarrToMat(resultImage);
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_caucse_alonehealth_ExerciseShotActivity_CountWhitePixels(JNIEnv *env,
                                                                          jobject instance,
                                                                          jlong matAddrInput) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    int count = 0;
    uchar r,g,b;

    for(int y = 0; y < matInput.rows; ++y){
        //y번째 줄에서 첫 번째 픽셀에 대한 포인터
        Vec3b* pixel = matInput.ptr<Vec3b>(y);
        for(int x = 0; x < matInput.cols; ++x){
            //픽셀에서 값 가져오기
            r = pixel[x][2];
            g = pixel[x][1];
            b = pixel[x][0];

            if(r == 255 && g == 255 && b == 255)
                count++;
        }
    }

    return count;

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_caucse_alonehealth_ExerciseShotActivity_CreateDifferenceImage(JNIEnv *env,
                                                                               jobject instance,
                                                                               jlong matAddrSource,
                                                                               jlong matAddrTarget,
                                                                               jlong matAddrResult) {

    // TODO
    IplImage matSource = *(Mat *)matAddrSource;
    IplImage matTarget = *(Mat *)matAddrTarget;
    Mat &matResult = *(Mat *)matAddrResult;
    IplImage* sourceImage;
    IplImage* targetImage;
    IplImage* resultImage;

    //Mat -> IplImage 변환
    IplImage copy;
    copy = matResult;
    resultImage = &copy;
    sourceImage = &matSource;
    targetImage = &matTarget;

    // 차영상 생성
    cvAbsDiff(sourceImage,targetImage,resultImage);
    cvThreshold(resultImage,resultImage,50,255,CV_THRESH_BINARY);

    //IplImage -> Mat 변환
    matResult = cvarrToMat(resultImage);

}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_caucse_alonehealth_DifferenceImageTest_CountWhitePixelsInOneRow(JNIEnv *env,
                                                                                 jobject instance,
                                                                                 jlong matAddrInput,
                                                                                 jint indexOfStart,
                                                                                 jint indexOfEnd) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    int count = 0;
    uchar r,g,b;

    for(int y = indexOfStart; y < indexOfEnd; ++y){
        //y번째 줄에서 첫 번째 픽셀에 대한 포인터
        Vec3b* pixel = matInput.ptr<Vec3b>(y);
        for(int x = 0; x < matInput.cols; ++x){
            //픽셀에서 값 가져오기
            r = pixel[x][2];
            g = pixel[x][1];
            b = pixel[x][0];

            if(r == 255 && g == 255 && b == 255)
                count++;
        }
    }

    return count;

}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_caucse_alonehealth_ExerciseShotActivity_CountWhitePixelsInOneRow(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jlong matAddrInput,
                                                                                  jint indexOfStart,
                                                                                  jint indexOfEnd) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    int count = 0;
    uchar r,g,b;

    for(int y = indexOfStart; y < indexOfEnd; ++y){
        //y번째 줄에서 첫 번째 픽셀에 대한 포인터
        Vec3b* pixel = matInput.ptr<Vec3b>(y);
        for(int x = 0; x < matInput.cols; ++x){
            //픽셀에서 값 가져오기
            r = pixel[x][2];
            g = pixel[x][1];
            b = pixel[x][0];

            if(r == 255 && g == 255 && b == 255)
                count++;
        }
    }

    return count;
}