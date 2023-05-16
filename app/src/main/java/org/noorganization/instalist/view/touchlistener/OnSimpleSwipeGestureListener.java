/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalist.view.touchlistener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by TS on 04.05.2015.
 * Touchlistener that proceed gesturedetection of swiping and tapping.
 */
public class  OnSimpleSwipeGestureListener implements View.OnTouchListener, IOnItemTouchEvents{

    private final GestureDetector mGestureDetector;

    public OnSimpleSwipeGestureListener(Context context, View recyclerView){
        mGestureDetector = new GestureDetector(context, new GestureListener(this, recyclerView));

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }


    public void onSwipeRight(View childView) {}
    public void onSwipeLeft(View childView) {}
    public void onSingleTap(View childView){}
    public void onLongTap(View childView) {}



    private static final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 20;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        private OnSimpleSwipeGestureListener mGestureListener;
        private View mRecyclerView;

        /**
         * Creates a new instance of a GestureListener.
         * @param _OnGestureListener The listener, that listens to those events.
         * @param _RecyclerView the recyclerview attached to layout.
         */
        public GestureListener(OnSimpleSwipeGestureListener _OnGestureListener, View _RecyclerView){
            this.mGestureListener   = _OnGestureListener;
            this.mRecyclerView      = _RecyclerView;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mGestureListener.onSingleTap(mRecyclerView);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mGestureListener.onLongTap(mRecyclerView);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // boolean result = false;
            if(e1 == null || e2 == null) return true;

            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                if (diffX > 0) {
                    mGestureListener.onSwipeRight(mRecyclerView);
                } else {
                    mGestureListener.onSwipeLeft(mRecyclerView);
                }
            }
            return false;
        }
    }
}
