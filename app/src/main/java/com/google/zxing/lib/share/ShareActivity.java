/*
 * Copyright (C) 2008 ZXing authors
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

package com.google.zxing.lib.share;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.lib.Contents;
import com.google.zxing.lib.Intents;
import com.google.zxing.lib.clipboard.ClipboardInterface;
import com.sensoro.loratool.R;

/**
 * Barcode Scanner can share data like contacts and bookmarks by displaying a QR Code on screen,
 * such that another user can scan the barcode with their phone.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ShareActivity extends Activity {

    private static final String TAG = ShareActivity.class.getSimpleName();

    private static final int PICK_BOOKMARK = 0;
    private static final int PICK_APP = 2;

    private final View.OnClickListener clipboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Should always be true, because we grey out the clipboard button in onResume() if it's empty
            CharSequence text = ClipboardInterface.getText(ShareActivity.this);
            if (text != null) {
                launchSearch(text.toString());
            }
        }
    };

    private final View.OnKeyListener textListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String text = ((TextView) view).getText().toString();
                if (text != null && !text.isEmpty()) {
                    launchSearch(text);
                }
                return true;
            }
            return false;
        }
    };

    private void launchSearch(String text) {
        Intent intent = new Intent(Intents.Encode.ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, text);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.zxing_share);

        findViewById(R.id.share_text_view).setOnKeyListener(textListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_BOOKMARK:
                case PICK_APP:
                    showTextAsBarcode(intent.getStringExtra(Browser.EXTRA_APPLICATION_ID));
                    break;
            }
        }
    }

    private void showTextAsBarcode(String text) {
        Log.i(TAG, "Showing text as barcode: " + text);
        if (text == null) {
            return; // Show error?
        }
        Intent intent = new Intent(Intents.Encode.ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, text);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        startActivity(intent);
    }

    private static String massageContactData(String data) {
        // For now -- make sure we don't put newlines in shared contact data. It messes up
        // any known encoding of contact data. Replace with space.
        if (data.indexOf('\n') >= 0) {
            data = data.replace("\n", " ");
        }
        if (data.indexOf('\r') >= 0) {
            data = data.replace("\r", " ");
        }
        return data;
    }
}
