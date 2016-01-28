package ua.kaganovych.superlocationproject.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import ua.kaganovych.superlocationproject.Config;

public class LocationUpdateService extends IntentService {

    private static int type = -1;

    public LocationUpdateService() {
        super(LocationUpdateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            final DetectedActivity detectedActivity = result.getMostProbableActivity();
            final int mostProbableType = detectedActivity.getType();

            if (mostProbableType == type) {
                stopSelf(); 
                return;
            }

            switch (mostProbableType) {
                case DetectedActivity.STILL:
                    type = DetectedActivity.STILL;
                    sendData(DetectedActivity.STILL);
                    break;
                case DetectedActivity.WALKING:
                    type = DetectedActivity.WALKING;
                    sendData(DetectedActivity.WALKING);
                    break;
                case DetectedActivity.ON_FOOT:
                    type = DetectedActivity.ON_FOOT;
                    sendData(DetectedActivity.ON_FOOT);
                    break;
                case DetectedActivity.RUNNING:
                    type = DetectedActivity.RUNNING;
                    sendData(DetectedActivity.RUNNING);
                    break;
                case DetectedActivity.TILTING:
                    type = DetectedActivity.TILTING;
                    sendData(DetectedActivity.TILTING);
                    break;
                default:
                    type = DetectedActivity.UNKNOWN;
                    sendData(DetectedActivity.UNKNOWN);
                    break;
            }
        }
    }

    private void sendData(int type) {
        final Intent intent = new Intent(Config.ACTIVITY_TYPE_ACTION);
        intent.putExtra("type", type);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        stopSelf();
    }
}
