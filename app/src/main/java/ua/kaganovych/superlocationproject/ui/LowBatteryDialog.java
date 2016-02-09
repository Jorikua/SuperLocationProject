package ua.kaganovych.superlocationproject.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import ua.kaganovych.superlocationproject.R;

public class LowBatteryDialog extends DialogFragment {

    private TurnOffLocationCallback turnOffLocationCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.text_low_battery_dialog_message)
                .setPositiveButton(R.string.text_low_battery_dialog_turn_off_btn, turnOffListener)
                .setNegativeButton(R.string.text_dismiss, closeListener);

        return builder.create();
    }

    private DialogInterface.OnClickListener turnOffListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (turnOffLocationCallback != null) {
                turnOffLocationCallback.onTurnOffLocation();
            }
            dismiss();
        }
    };

    private DialogInterface.OnClickListener closeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    };

    public void setTurnOffLocationCallback(TurnOffLocationCallback turnOffLocationCallback) {
        this.turnOffLocationCallback = turnOffLocationCallback;
    }

    public interface TurnOffLocationCallback {
        void onTurnOffLocation();
    }
}
