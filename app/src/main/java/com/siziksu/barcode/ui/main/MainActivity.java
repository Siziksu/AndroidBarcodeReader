package com.siziksu.barcode.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.siziksu.barcode.R;
import com.siziksu.barcode.common.Constants;
import com.siziksu.barcode.ui.barcode.BarcodeCaptureActivity;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int BARCODE_CAPTURE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusMessage = (TextView) findViewById(R.id.status_message);
        barcodeValue = (TextView) findViewById(R.id.barcode_value);
        findViewById(R.id.read_barcode).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            startActivityForResult(intent, BARCODE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null && data.getExtras() != null && data.getExtras().containsKey(BarcodeCaptureActivity.BARCODE_KEY)) {
                    String barcode = data.getExtras().getString(BarcodeCaptureActivity.BARCODE_KEY);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode);
                    Log.i(Constants.TAG, "Barcode read: " + barcode);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    barcodeValue.setText("");
                    Log.i(Constants.TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
