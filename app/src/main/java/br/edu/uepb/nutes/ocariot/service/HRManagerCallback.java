package br.edu.uepb.nutes.ocariot.service;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface HRManagerCallback extends DeviceStatusCallback {
    /**
     * Heart rate measurement receiver.
     *
     * @param device    {@link BluetoothDevice} Device that collected the measurement.
     * @param heartRate Measurement value in bpm.
     * @param timestamp Datetime of collection.
     */
    void onMeasurementReceived(
            @NonNull final BluetoothDevice device,
            final int heartRate,
            final String timestamp
    );
}