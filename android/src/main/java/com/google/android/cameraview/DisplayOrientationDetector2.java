/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;


/**
 * Monitors the value returned from {@link Display#getRotation()}.
 */
abstract class DisplayOrientationDetector2 extends DisplayOrientationDetector implements SensorEventListener {
    SensorManager sensorManager;
    Sensor sensor;

    public static final int PORTRAIT = 1;
    public static final int LANDSCAPE_RIGHT = 2;
    public static final int UPSIDE_DOWN = 3;
    public static final int LANDSCAPE_LEFT = 4;

    private int ORIENTATION_UNKNOWN = -1;

    private static final int _DATA_X = 0;
    private static final int _DATA_Y = 1;
    private static final int _DATA_Z = 2;

    public int mOrientationDeg;
    private int tempOrientRounded = 0;
    public int mOrientationRounded;

    public DisplayOrientationDetector2(Context context) {
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void enable(Display display) {
        mDisplay = display;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(display.getRotation()));
    }

    public void disable() {
        sensorManager.unregisterListener(this);
        mDisplay = null;
    }

    @Override
    public void onAccuracyChanged(Sensor var1, int var2) {
        //Do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float[] values = sensorEvent.values;
        int orientation = ORIENTATION_UNKNOWN;
        float X = -values[_DATA_X];
        float Y = -values[_DATA_Y];
        float Z = -values[_DATA_Z];
        float magnitude = X*X + Y*Y;

        if (magnitude * 4 >= Z*Z) {
            float OneEightyOverPi = 57.29577957855f;
            float angle = (float)Math.atan2(-Y, X) * OneEightyOverPi;
            orientation = 90 - (int)Math.round(angle);
            // normalize to 0 - 359 range
            while (orientation >= 360) {
                orientation -= 360;
            }
            while (orientation < 0) {
                orientation += 360;
            }
        }

        if (orientation != mOrientationDeg)
        {
            mOrientationDeg = orientation;
            if(orientation == -1){//basically flat

            }
            else if(orientation < 10 || orientation > 350){
                tempOrientRounded = 1;//portrait
            }
            else if(orientation > 80 && orientation < 100){
                tempOrientRounded = 2; //lsRight
            }
            else if(orientation > 170 && orientation < 190){
                tempOrientRounded = 3; //upside down
            }
            else if(orientation > 260 && orientation < 280){
                tempOrientRounded = 4;//lsLeft
            }

        }
        if(mOrientationRounded != tempOrientRounded){
            mOrientationRounded = tempOrientRounded;
            switch (mOrientationRounded){
                case PORTRAIT:
                    dispatchOnDisplayOrientationChanged(0);
                    break;
                case LANDSCAPE_LEFT:
                    dispatchOnDisplayOrientationChanged(90);
                    break;
                case UPSIDE_DOWN:
                    dispatchOnDisplayOrientationChanged(180);
                    break;
                case LANDSCAPE_RIGHT:
                    dispatchOnDisplayOrientationChanged(270);
                    break;
            }
        }
    }
}
