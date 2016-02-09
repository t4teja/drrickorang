/*
 * Copyright (C) 2015 The Android Open Source Project
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

package org.drrickorang.loopback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.TextView;


/**
 * This activity displays all settings that can be adjusted by the user.
 */

public class SettingsActivity extends Activity implements OnItemSelectedListener {

    private static final String TAG = "SettingsActivity";

    private Spinner      mSpinnerMicSource;
    private Spinner      mSpinnerSamplingRate;
    private Spinner      mSpinnerAudioThreadType;
    private TextView     mTextSettingsInfo;
    private Spinner      mSpinnerChannelIndex;
    private SettingsPicker mPlayerBufferUI;
    private SettingsPicker mRecorderBufferUI;
    private SettingsPicker mBufferTestDurationUI;
    private SettingsPicker mWavePlotDurationUI;
    private SettingsPicker mLoadThreadUI;

    ArrayAdapter<CharSequence> mAdapterSamplingRate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity. You can find it
        View view = getLayoutInflater().inflate(R.layout.settings_activity, null);
        setContentView(view);
        mTextSettingsInfo = (TextView) findViewById(R.id.textSettingsInfo);

        int micSource = getApp().getMicSource();
        mSpinnerMicSource = (Spinner) findViewById(R.id.spinnerMicSource);
        ArrayAdapter<CharSequence> adapterMicSource = ArrayAdapter.createFromResource(this,
                R.array.mic_source_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterMicSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinnerMicSource.setAdapter(adapterMicSource);
        //set current value
        mSpinnerMicSource.setSelection(micSource, false);
        mSpinnerMicSource.setOnItemSelectedListener(this);

        int samplingRate = getApp().getSamplingRate();
        //init spinner, etc
        mSpinnerSamplingRate = (Spinner) findViewById(R.id.spinnerSamplingRate);
        mAdapterSamplingRate = ArrayAdapter.createFromResource(this,
                R.array.samplingRate_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        mAdapterSamplingRate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinnerSamplingRate.setAdapter(mAdapterSamplingRate);
        //set current value
        String currentValue = String.valueOf(samplingRate);
        int nPosition = mAdapterSamplingRate.getPosition(currentValue);
        mSpinnerSamplingRate.setSelection(nPosition, false);
        mSpinnerSamplingRate.setOnItemSelectedListener(this);

        //spinner native
        int audioThreadType = getApp().getAudioThreadType();
        mSpinnerAudioThreadType = (Spinner) findViewById(R.id.spinnerAudioThreadType);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.audioThreadType_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinnerAudioThreadType.setAdapter(adapter2);
        //set current value
        mSpinnerAudioThreadType.setSelection(audioThreadType, false);
        if (!getApp().isSafeToUseSles())
            mSpinnerAudioThreadType.setEnabled(false);
        mSpinnerAudioThreadType.setOnItemSelectedListener(this);

        mSpinnerChannelIndex = (Spinner) findViewById(R.id.spinnerChannelIndex);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
                R.array.channelIndex_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinnerChannelIndex.setAdapter(adapter3);
        mSpinnerChannelIndex.setOnItemSelectedListener(this);

        // Settings Picker for Buffer Test Duration
        mBufferTestDurationUI = (SettingsPicker) findViewById(R.id.bufferTestDurationSetting);
        mBufferTestDurationUI.setMinMaxDefault(Constant.BUFFER_TEST_DURATION_SECONDS_MIN,
                Constant.BUFFER_TEST_DURATION_SECONDS_MAX, getApp().getBufferTestDuration());
        mBufferTestDurationUI.setTitle(getResources().getString(R.string.labelBufferTestDuration,
                Constant.BUFFER_TEST_DURATION_SECONDS_MAX));
        mBufferTestDurationUI.setSettingsChangeListener(new SettingsPicker.SettingChangeListener() {
            @Override
            public void settingChanged(int seconds) {
                log("buffer test new duration: " + seconds);
                getApp().setBufferTestDuration(seconds);
                setSettingsHaveChanged();
            }
        });

        // Settings Picker for Wave Plot Duration
        mWavePlotDurationUI = (SettingsPicker) findViewById(R.id.wavePlotDurationSetting);
        mWavePlotDurationUI.setMinMaxDefault(Constant.BUFFER_TEST_WAVE_PLOT_DURATION_SECONDS_MIN,
                Constant.BUFFER_TEST_WAVE_PLOT_DURATION_SECONDS_MAX,
                getApp().getBufferTestWavePlotDuration());
        mWavePlotDurationUI.setTitle(getResources().getString(
                R.string.labelBufferTestWavePlotDuration,
                Constant.BUFFER_TEST_WAVE_PLOT_DURATION_SECONDS_MAX));
        mWavePlotDurationUI.setSettingsChangeListener(new SettingsPicker.SettingChangeListener() {
            @Override
            public void settingChanged(int value) {
                log("buffer test's wave plot new duration:" + value);
                getApp().setBufferTestWavePlotDuration(value);
                setSettingsHaveChanged();
            }
        });

        // Settings Picker for Player Buffer Period
        mPlayerBufferUI = (SettingsPicker) findViewById(R.id.playerBufferSetting);
        mPlayerBufferUI.setMinMaxDefault(Constant.PLAYER_BUFFER_FRAMES_MIN,
                Constant.PLAYER_BUFFER_FRAMES_MAX,
                getApp().getPlayerBufferSizeInBytes() / Constant.BYTES_PER_FRAME);
        mPlayerBufferUI.setTitle(getResources().getString(
                R.string.labelPlayerBuffer, Constant.PLAYER_BUFFER_FRAMES_MAX));
        mPlayerBufferUI.setSettingsChangeListener(new SettingsPicker.SettingChangeListener() {
            @Override
            public void settingChanged(int value) {
                log("player buffer new size " + value);
                getApp().setPlayerBufferSizeInBytes(value * Constant.BYTES_PER_FRAME);
                int audioThreadType = mSpinnerAudioThreadType.getSelectedItemPosition();
                // in native mode, recorder buffer size = player buffer size
                if (audioThreadType == Constant.AUDIO_THREAD_TYPE_NATIVE) {
                    getApp().setRecorderBufferSizeInBytes(value * Constant.BYTES_PER_FRAME);
                    mRecorderBufferUI.setValue(value);
                }
                setSettingsHaveChanged();
            }
        });

        // Settings Picker for Recorder Buffer Period
        mRecorderBufferUI = (SettingsPicker) findViewById(R.id.recorderBufferSetting);
        mRecorderBufferUI.setMinMaxDefault(Constant.RECORDER_BUFFER_FRAMES_MIN,
                Constant.RECORDER_BUFFER_FRAMES_MAX,
                getApp().getRecorderBufferSizeInBytes() / Constant.BYTES_PER_FRAME);
        mRecorderBufferUI.setTitle(getResources().getString(R.string.labelRecorderBuffer,
                Constant.RECORDER_BUFFER_FRAMES_MAX));
        mRecorderBufferUI.setSettingsChangeListener(new SettingsPicker.SettingChangeListener() {
            @Override
            public void settingChanged(int value) {
                log("recorder buffer new size:" + value);
                getApp().setRecorderBufferSizeInBytes(value * Constant.BYTES_PER_FRAME);
                setSettingsHaveChanged();
            }
        });

        // Settings Picker for Number of Load Threads
        mLoadThreadUI = (SettingsPicker) findViewById(R.id.numLoadThreadsSetting);
        mLoadThreadUI.setMinMaxDefault(Constant.MIN_NUM_LOAD_THREADS, Constant.MAX_NUM_LOAD_THREADS,
                getApp().getNumberOfLoadThreads());
        mLoadThreadUI.setTitle(getResources().getString(R.string.loadThreadsLabel));
        mLoadThreadUI.setSettingsChangeListener(new SettingsPicker.SettingChangeListener() {
            @Override
            public void settingChanged(int value) {
                log("new num load threads:" + value);
                getApp().setNumberOfLoadThreads(value);
                setSettingsHaveChanged();
            }
        });

        refresh();
    }


    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        log("on back pressed");
        setSettingsHaveChanged();
        finish();
    }


    private void refresh() {
        mBufferTestDurationUI.setValue(getApp().getBufferTestDuration());
        mWavePlotDurationUI.setValue(getApp().getBufferTestWavePlotDuration());

        mPlayerBufferUI.setValue(getApp().getPlayerBufferSizeInBytes() / Constant.BYTES_PER_FRAME);
        mRecorderBufferUI.setValue(
                getApp().getRecorderBufferSizeInBytes() / Constant.BYTES_PER_FRAME);

        mRecorderBufferUI.setEnabled(
                getApp().getAudioThreadType() == Constant.AUDIO_THREAD_TYPE_JAVA);

        int samplingRate = getApp().getSamplingRate();
        String currentValue = String.valueOf(samplingRate);
        int nPosition = mAdapterSamplingRate.getPosition(currentValue);
        mSpinnerSamplingRate.setSelection(nPosition);


        if (getApp().getAudioThreadType() == Constant.AUDIO_THREAD_TYPE_JAVA) {
            mSpinnerChannelIndex.setSelection(getApp().getChannelIndex() + 1, false);
            mSpinnerChannelIndex.setEnabled(true);
        } else {
            mSpinnerChannelIndex.setSelection(0, false);
            mSpinnerChannelIndex.setEnabled(false);
        }

        String info = getApp().getSystemInfo();
        mTextSettingsInfo.setText("SETTINGS - " + info);
    }


    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        log("item selected!");

        switch (parent.getId()) {
        case R.id.spinnerSamplingRate:
            String stringValue = mSpinnerSamplingRate.getSelectedItem().toString();
            int samplingRate = Integer.parseInt(stringValue);
            getApp().setSamplingRate(samplingRate);
            setSettingsHaveChanged();
            log("Sampling Rate: " + stringValue);
            refresh();
            break;
        case R.id.spinnerAudioThreadType:
            int audioThreadType = mSpinnerAudioThreadType.getSelectedItemPosition();
            getApp().setAudioThreadType(audioThreadType);
            getApp().computeDefaults();
            setSettingsHaveChanged();
            log("AudioThreadType:" + audioThreadType);
            refresh();
            break;
        case R.id.spinnerChannelIndex:
            int channelIndex = mSpinnerChannelIndex.getSelectedItemPosition() - 1;
            getApp().setChannelIndex(channelIndex);
            getApp().computeDefaults();
            setSettingsHaveChanged();
            log("channelIndex:" + channelIndex);
            refresh();
            break;
        case R.id.spinnerMicSource:
            int micSource = mSpinnerMicSource.getSelectedItemPosition();
            getApp().setMicSource(micSource);
            setSettingsHaveChanged();
            log("mic Source:" + micSource);
            refresh();
            break;
        }
    }

    private void setSettingsHaveChanged() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    /** Called when the user clicks the button */
    public void onButtonClick(View view) {
        getApp().computeDefaults();
        refresh();
    }

// Below is work in progress by Ricardo
//    public void onButtonRecordDefault(View view) {
//        int samplingRate = getApp().getSamplingRate();
//
//        int minRecorderBufferSizeInBytes =  AudioRecord.getMinBufferSize(samplingRate,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        getApp().setRecorderBufferSizeInBytes(minRecorderBufferSizeInBytes);
//
//        refresh();
//    }

//    private void computeDefaults() {
//
////        if (getApp().getAudioThreadType() == LoopbackApplication.AUDIO_THREAD_TYPE_JAVA) {
////            mNumberPickerRecorderBuffer.setEnabled(true);
////        else
////            mNumberPickerRecorderBuffer.setEnabled(false);
//
//        int samplingRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
//        getApp().setSamplingRate(samplingRate);
//        int minPlayerBufferSizeInBytes = AudioTrack.getMinBufferSize(samplingRate,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        getApp().setPlayerBufferSizeInBytes(minPlayerBufferSizeInBytes);
//
//        int minRecorderBufferSizeInBytes =  AudioRecord.getMinBufferSize(samplingRate,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        getApp().setRecorderBufferSizeInBytes(minRecorderBufferSizeInBytes);
//        getApp().setRecorderBufferSizeInBytes(minRecorderBufferSizeInBytes);
//
//        log("computed defaults");
//
//    }

    private LoopbackApplication getApp() {
        return (LoopbackApplication) this.getApplication();
    }


    private static void log(String msg) {
        Log.v(TAG, msg);
    }


}
