package ua.kaganovych.superlocationproject.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import ua.kaganovych.superlocationproject.Config;

public class ActivityDetectionService extends IntentService {

    private static int type = -1;

    public ActivityDetectionService() {
        super(ActivityDetectionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            final DetectedActivity detectedActivity = result.getMostProbableActivity();
            final int mostProbableType = detectedActivity.getType();

            if (mostProbableType == type) {
                return;
            }
            type = mostProbableType;
            switch (mostProbableType) {
                case DetectedActivity.STILL:
                    sendData(mostProbableType);
                    break;
                case DetectedActivity.WALKING:
                    sendData(mostProbableType);
                    break;
                case DetectedActivity.ON_FOOT:
                    sendData(mostProbableType);
                    break;
                case DetectedActivity.RUNNING:
                    sendData(mostProbableType);
                    break;
                case DetectedActivity.TILTING:
                    sendData(mostProbableType);
                    break;
                default:
                    sendData(DetectedActivity.UNKNOWN);
                    break;
            }
        }
    }

    private void sendData(int type) {
        final Intent intent = new Intent(Config.ACTIVITY_TYPE_ACTION);
        intent.putExtra("type", type);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "Service destroyed"); //for debugging
    }
}
