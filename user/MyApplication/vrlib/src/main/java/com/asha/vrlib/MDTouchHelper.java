package com.asha.vrlib;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.LinkedList;
import java.util.List;

public class MDTouchHelper {

    private static final float sScaleMin = 1;
    private static final float sScaleMax = 4;

    private MDVRLibrary.IAdvanceGestureListener mAdvanceGestureListener;
    private List<MDVRLibrary.IGestureListener> mClickListeners = new LinkedList<>();
    private GestureDetector mGestureDetector;
    private int mCurrentMode = 0;
    private PinchInfo mPinchInfo = new PinchInfo();
    private boolean mPinchEnabled;
    private float mGlobalScale = sScaleMin;

    private static final int MODE_INIT = 0;
    private static final int MODE_PINCH = 1;

    public long time_s=0;
    public long time_e=0;
    public long touch_t=0;
    public boolean isT=false;

    public MDTouchHelper(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mCurrentMode == MODE_PINCH) return false;

                for (MDVRLibrary.IGestureListener listener : mClickListeners){
                    listener.onClick(e);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mCurrentMode == MODE_PINCH) return false;

                if (mAdvanceGestureListener != null)
                    mAdvanceGestureListener.onDrag(distanceX / mGlobalScale, distanceY / mGlobalScale);
                return true;
            }
        });
    }


    /*

    ACTION_DOWN은 터치 포인트가 1개(손가락 1개로 터치)일 때만 발생
    ACTION_POINTER_DOWN은 터치 포인트가 2개 이상(여러곳 동시 터치)일 때만 발생하므로
    필요에 따라 두 가지 모두 구현 해야 함

    다중 터치 도중 하나 이상의 터치 포인트가 해제될 경우 좌표 변수 안에 입력 되있는 좌표
    데이터 배열 순서가 뒤바뀔 수 있으므로 터치 포인터의 고유 번호를 비교하는 방식으로 구현함

     */
    public boolean handleTouchEvent(MotionEvent event, MDGLScreenWrapper sw) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {    // 땔 때 (up & cancel)
            if (mCurrentMode == MODE_PINCH) {
                // end anim (여러곳을 눌렀다가 전부 떼는 순간)
                // 여기에 구현을 해보자
                //sw.onPause();
            }
            mCurrentMode = MODE_INIT;
        } else if (action == MotionEvent.ACTION_POINTER_UP) {   // 두 포인터를 떼는 순간
            // one point up
            if (mCurrentMode == MODE_PINCH) {
                // more than 2 pointer (So Don't Modify this;;)
                if (event.getPointerCount() > 2) {
                    if (event.getAction() >> 8 == 0) {
                        // 0 up
                        markPinchInfo(event.getX(1), event.getY(1), event.getX(2), event.getY(2));
                    } else if (event.getAction() >> 8 == 1) {
                        // 1 up
                        markPinchInfo(event.getX(0), event.getY(0), event.getX(2), event.getY(2));
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            // >= 2 pointer // Double Point Touch
            mCurrentMode = MODE_PINCH;
            markPinchInfo(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
        } else if (action == MotionEvent.ACTION_MOVE) {
            // Double Point Drag
            if (mCurrentMode == MODE_PINCH && event.getPointerCount() > 1) {
                // Display Size Control
                    float distance = calDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    // float[] lineCenter = MathUtils.getCenterPoint(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    // mLastMovePoint.set(lineCenter[0], lineCenter[1]);
                    handlePinch(distance);
            }
            // One Point Drag
            else
            {

            }
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private void handlePinch(float distance) {
        if (mPinchEnabled){
            float scale = mPinchInfo.pinch(distance);
            if (mAdvanceGestureListener != null)
                mAdvanceGestureListener.onPinch(scale);

            mGlobalScale = scale;
        }
    }

    public void reset(){
        float currentPinch = mPinchInfo.reset();
        mGlobalScale = currentPinch;
        if (mAdvanceGestureListener != null)
            mAdvanceGestureListener.onPinch(currentPinch);
    }

    private void markPinchInfo(float x1, float y1, float x2, float y2) {
        mPinchInfo.mark(x1, y1, x2, y2);
    }

    private static float calDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public void addClickListener(MDVRLibrary.IGestureListener gestureListener) {
        if (gestureListener != null) mClickListeners.add(gestureListener);
    }

    public void setAdvanceGestureListener(MDVRLibrary.IAdvanceGestureListener listener) {
        this.mAdvanceGestureListener = listener;
    }

    public void setPinchEnabled(boolean mPinchEnabled) {
        this.mPinchEnabled = mPinchEnabled;
    }

    private static class PinchInfo{
        private static final float sSensitivity = 3;
        private float x1;
        private float y1;
        private float x2;
        private float y2;
        private float oDistance;
        private float prevScale = sScaleMin;
        private float currentScale = sScaleMin;

        public void mark(float x1, float y1, float x2, float y2){
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            oDistance = calDistance(x1, y1, x2, y2);
            prevScale = currentScale;
        }

        public float pinch(float distance) {
            if (oDistance == 0) oDistance = distance;
            float scale = distance / oDistance - 1;
            scale *= sSensitivity;
            currentScale = prevScale + scale;
            // range
            if (currentScale < sScaleMin) currentScale = sScaleMin;
            else if (currentScale > sScaleMax) currentScale = sScaleMax;
            return currentScale;
        }

        public float reset(){
            prevScale = sScaleMin;
            currentScale = sScaleMin;
            return currentScale;
        }
    }
}
