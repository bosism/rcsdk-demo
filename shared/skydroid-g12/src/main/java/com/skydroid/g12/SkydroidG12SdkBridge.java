package com.skydroid.g12;

import android.content.Context;
import android.os.SystemClock;

import com.skydroid.rcsdk.KeyManager;
import com.skydroid.rcsdk.RCSDKManager;
import com.skydroid.rcsdk.SDKManagerCallBack;
import com.skydroid.rcsdk.common.callback.CompletionCallbackWith;
import com.skydroid.rcsdk.common.error.SkyException;
import com.skydroid.rcsdk.common.remotecontroller.ChannelItem;
import com.skydroid.rcsdk.common.remotecontroller.ChannelSettings;
import com.skydroid.rcsdk.common.remotecontroller.ControlMode;
import com.skydroid.rcsdk.key.RemoteControllerKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class SkydroidG12SdkBridge {
    public static final int DEFAULT_MAX_OVERLAPPING_READS = 6;
    public static final long DEFAULT_SLOW_POLL_LATENCY_MS = 200L;
    public static final String METADATA_CONTROL_MODE = "control_mode";
    public static final String METADATA_CHANNEL_SETTINGS = "channel_settings";

    private static final long MIN_POLL_INTERVAL_MS = 1L;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicInteger pendingReads = new AtomicInteger(0);
    private final AtomicInteger maxPendingReads = new AtomicInteger(0);
    private final AtomicInteger skippedPolls = new AtomicInteger(0);
    private final AtomicInteger failedPolls = new AtomicInteger(0);
    private final AtomicInteger successfulPolls = new AtomicInteger(0);
    private final AtomicInteger stalePolls = new AtomicInteger(0);
    private final AtomicInteger slowPolls = new AtomicInteger(0);
    private final AtomicLong totalPollLatencyMs = new AtomicLong(0);
    private final AtomicLong lastPollLatencyMs = new AtomicLong(-1);
    private final AtomicLong maxPollLatencyMs = new AtomicLong(-1);
    private final AtomicLong lastRequestElapsedMs = new AtomicLong(0);
    private final AtomicLong lastRequestIntervalMs = new AtomicLong(-1);
    private final AtomicLong maxRequestIntervalMs = new AtomicLong(-1);
    private final AtomicInteger requestGapsOverManualInterval = new AtomicInteger(0);
    private final AtomicLong lastCallbackElapsedMs = new AtomicLong(0);
    private final AtomicLong lastCallbackIntervalMs = new AtomicLong(-1);
    private final AtomicLong maxCallbackIntervalMs = new AtomicLong(-1);
    private final AtomicInteger callbackGapsOverManualInterval = new AtomicInteger(0);
    private final AtomicInteger callbackGapsOverDoubleManualInterval = new AtomicInteger(0);
    private final AtomicLong issuedReadSequence = new AtomicLong(0);
    private final AtomicLong deliveredReadSequence = new AtomicLong(0);
    private final AtomicLong pollGeneration = new AtomicLong(0);

    private volatile Config config = Config.defaults();
    private volatile Listener listener;
    private volatile Thread pollThread;
    private volatile String deviceType = "UNKNOWN";
    private volatile String controlMode = "UNKNOWN";

    public void start(Context context, Config config, Listener listener) {
        this.listener = listener;
        updateConfig(config);
        if (!started.compareAndSet(false, true)) {
            return;
        }

        RCSDKManager.INSTANCE.initSDK(context, new SDKManagerCallBack() {
            @Override
            public void onRcConnected() {
                handleConnected();
            }

            @Override
            public void onRcConnectFail(SkyException e) {
                Listener currentListener = SkydroidG12SdkBridge.this.listener;
                if (currentListener != null) {
                    currentListener.onRcConnectFailed(errorText(e));
                }
            }

            @Override
            public void onRcDisconnect() {
                stopPolling();
                Listener currentListener = SkydroidG12SdkBridge.this.listener;
                if (currentListener != null) {
                    currentListener.onRcDisconnected();
                }
            }
        });
        RCSDKManager.INSTANCE.setMainThreadCallBack(this.config.mainThreadCallbacks);
        RCSDKManager.INSTANCE.connectToRC();
    }

    public void stop() {
        if (!started.getAndSet(false)) {
            return;
        }
        stopPolling();
        RCSDKManager.INSTANCE.disconnectRC();
    }

    public void updateConfig(Config config) {
        this.config = config == null ? Config.defaults() : config.sanitized();
        Thread thread = pollThread;
        if (thread != null) {
            synchronized (thread) {
                thread.notifyAll();
            }
        }
    }

    public void refreshMetadata() {
        KeyManager.INSTANCE.get(
            RemoteControllerKey.INSTANCE.getKeyControlMode(),
            new CompletionCallbackWith<ControlMode>() {
                @Override
                public void onSuccess(ControlMode value) {
                    controlMode = value == null ? "UNKNOWN" : value.name();
                    Listener currentListener = listener;
                    if (currentListener != null) {
                        currentListener.onControlMode(controlMode);
                    }
                }

                @Override
                public void onFailure(SkyException e) {
                    controlMode = "UNKNOWN";
                    Listener currentListener = listener;
                    if (currentListener != null) {
                        currentListener.onMetadataReadFailed(METADATA_CONTROL_MODE, errorText(e));
                    }
                }
            }
        );

        KeyManager.INSTANCE.get(
            RemoteControllerKey.INSTANCE.getKeyChannelSettings(),
            new CompletionCallbackWith<ChannelSettings>() {
                @Override
                public void onSuccess(ChannelSettings value) {
                    List<ChannelCalibration> calibrations = channelCalibrations(value);
                    Listener currentListener = listener;
                    if (currentListener != null) {
                        currentListener.onChannelSettings(calibrations);
                    }
                }

                @Override
                public void onFailure(SkyException e) {
                    Listener currentListener = listener;
                    if (currentListener != null) {
                        currentListener.onMetadataReadFailed(METADATA_CHANNEL_SETTINGS, errorText(e));
                    }
                }
            }
        );
    }

    public Diagnostics getDiagnostics() {
        int successCount = successfulPolls.get();
        return new Diagnostics(
            successCount,
            stalePolls.get(),
            skippedPolls.get(),
            failedPolls.get(),
            slowPolls.get(),
            pendingReads.get(),
            maxPendingReads.get(),
            lastRequestIntervalMs.get(),
            maxRequestIntervalMs.get(),
            requestGapsOverManualInterval.get(),
            lastCallbackIntervalMs.get(),
            maxCallbackIntervalMs.get(),
            callbackGapsOverManualInterval.get(),
            callbackGapsOverDoubleManualInterval.get(),
            lastPollLatencyMs.get(),
            successCount > 0 ? totalPollLatencyMs.get() / successCount : -1L,
            maxPollLatencyMs.get()
        );
    }

    public void resetDiagnostics() {
        maxPendingReads.set(pendingReads.get());
        skippedPolls.set(0);
        failedPolls.set(0);
        successfulPolls.set(0);
        stalePolls.set(0);
        slowPolls.set(0);
        totalPollLatencyMs.set(0);
        lastPollLatencyMs.set(-1);
        maxPollLatencyMs.set(-1);
        lastRequestElapsedMs.set(0);
        lastRequestIntervalMs.set(-1);
        maxRequestIntervalMs.set(-1);
        requestGapsOverManualInterval.set(0);
        lastCallbackElapsedMs.set(0);
        lastCallbackIntervalMs.set(-1);
        maxCallbackIntervalMs.set(-1);
        callbackGapsOverManualInterval.set(0);
        callbackGapsOverDoubleManualInterval.set(0);
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getControlMode() {
        return controlMode;
    }

    private void handleConnected() {
        deviceType = readDeviceType();
        Listener currentListener = listener;
        if (currentListener != null) {
            currentListener.onRcConnected(deviceType);
        }
        refreshMetadata();
        startPolling();
    }

    private void startPolling() {
        Thread currentThread = pollThread;
        if (currentThread != null && currentThread.isAlive()) {
            return;
        }
        resetPollDiagnostics();
        long generation = pollGeneration.incrementAndGet();
        Thread nextThread = new PollThread(generation);
        pollThread = nextThread;
        nextThread.start();
    }

    private void stopPolling() {
        pollGeneration.incrementAndGet();
        Thread thread = pollThread;
        pollThread = null;
        if (thread != null) {
            thread.interrupt();
            synchronized (thread) {
                thread.notifyAll();
            }
            try {
                thread.join(Math.max(config.pollIntervalMs * 2L, 100L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        pendingReads.set(0);
    }

    private void resetPollDiagnostics() {
        pendingReads.set(0);
        resetDiagnostics();
        issuedReadSequence.set(0);
        deliveredReadSequence.set(0);
    }

    private long beginRead(long generation) {
        if (!isCurrentGeneration(generation)) {
            return -1L;
        }
        Config currentConfig = config;
        if (currentConfig.preventOverlappingReads) {
            if (pendingReads.compareAndSet(0, 1)) {
                updateMaxPendingReads(1);
                return issuedReadSequence.incrementAndGet();
            }
            skippedPolls.incrementAndGet();
            return -1L;
        }
        int pending = beginOverlappingRead(currentConfig.maxOverlappingReads);
        if (pending < 0) {
            skippedPolls.incrementAndGet();
            return -1L;
        }
        updateMaxPendingReads(pending);
        return issuedReadSequence.incrementAndGet();
    }

    private int beginOverlappingRead(int maxOverlappingReads) {
        while (true) {
            int current = pendingReads.get();
            if (current >= maxOverlappingReads) {
                return -1;
            }
            if (pendingReads.compareAndSet(current, current + 1)) {
                return current + 1;
            }
        }
    }

    private void finishRead() {
        while (true) {
            int current = pendingReads.get();
            if (current <= 0) {
                return;
            }
            if (pendingReads.compareAndSet(current, current - 1)) {
                return;
            }
        }
    }

    private void recordPollLatency(long latencyMs) {
        long sanitizedLatencyMs = Math.max(0L, latencyMs);
        successfulPolls.incrementAndGet();
        if (sanitizedLatencyMs > config.slowPollLatencyMs) {
            slowPolls.incrementAndGet();
        }
        totalPollLatencyMs.addAndGet(sanitizedLatencyMs);
        lastPollLatencyMs.set(sanitizedLatencyMs);
        updateMax(maxPollLatencyMs, sanitizedLatencyMs);
    }

    private void recordRequestStarted(long requestedAtElapsedMs) {
        long previousRequestElapsedMs = lastRequestElapsedMs.getAndSet(requestedAtElapsedMs);
        if (previousRequestElapsedMs <= 0L) {
            return;
        }
        long intervalMs = Math.max(0L, requestedAtElapsedMs - previousRequestElapsedMs);
        lastRequestIntervalMs.set(intervalMs);
        updateMax(maxRequestIntervalMs, intervalMs);
        if (intervalMs > config.manualIntervalMs) {
            requestGapsOverManualInterval.incrementAndGet();
        }
    }

    private void recordCallbackDelivered(long callbackAtElapsedMs) {
        long previousCallbackElapsedMs = lastCallbackElapsedMs.getAndSet(callbackAtElapsedMs);
        if (previousCallbackElapsedMs <= 0L) {
            return;
        }
        long intervalMs = Math.max(0L, callbackAtElapsedMs - previousCallbackElapsedMs);
        long manualIntervalMs = config.manualIntervalMs;
        lastCallbackIntervalMs.set(intervalMs);
        updateMax(maxCallbackIntervalMs, intervalMs);
        if (intervalMs > manualIntervalMs) {
            callbackGapsOverManualInterval.incrementAndGet();
        }
        if (intervalMs > manualIntervalMs * 2L) {
            callbackGapsOverDoubleManualInterval.incrementAndGet();
        }
    }

    private boolean markDelivered(long readSequence) {
        while (true) {
            long currentDeliveredSequence = deliveredReadSequence.get();
            if (readSequence <= currentDeliveredSequence) {
                stalePolls.incrementAndGet();
                return false;
            }
            if (deliveredReadSequence.compareAndSet(currentDeliveredSequence, readSequence)) {
                return true;
            }
        }
    }

    private void updateMaxPendingReads(int pending) {
        updateMax(maxPendingReads, pending);
    }

    private void updateMax(AtomicInteger target, int value) {
        while (true) {
            int currentMax = target.get();
            if (value <= currentMax) {
                return;
            }
            if (target.compareAndSet(currentMax, value)) {
                return;
            }
        }
    }

    private void updateMax(AtomicLong target, long value) {
        while (true) {
            long currentMax = target.get();
            if (value <= currentMax) {
                return;
            }
            if (target.compareAndSet(currentMax, value)) {
                return;
            }
        }
    }

    private boolean isCurrentGeneration(long generation) {
        return started.get() && generation == pollGeneration.get();
    }

    private String readDeviceType() {
        try {
            Object value = RCSDKManager.INSTANCE.getDeviceType();
            return value == null ? "UNKNOWN" : value.toString();
        } catch (Throwable t) {
            return "UNKNOWN";
        }
    }

    private static String errorText(Throwable error) {
        return error == null ? "unknown error" : error.toString();
    }

    private static List<ChannelCalibration> channelCalibrations(ChannelSettings settings) {
        if (settings == null || settings.getChannels() == null) {
            return Collections.emptyList();
        }
        ChannelItem[] items = settings.getChannels();
        List<ChannelCalibration> calibrations = new ArrayList<>(items.length);
        for (ChannelItem item : items) {
            if (item == null) {
                continue;
            }
            calibrations.add(new ChannelCalibration(
                item.getIndex(),
                item.getMin(),
                item.getMiddle(),
                item.getMax(),
                item.getReverse()
            ));
        }
        return Collections.unmodifiableList(calibrations);
    }

    public interface Listener {
        void onRcConnected(String deviceType);
        void onRcConnectFailed(String error);
        void onRcDisconnected();
        void onChannels(ChannelSample sample);
        void onControlMode(String controlMode);
        void onChannelSettings(List<ChannelCalibration> channels);
        void onMetadataReadFailed(String metadata, String error);
    }

    public static final class Config {
        public final long pollIntervalMs;
        public final long manualIntervalMs;
        public final boolean preventOverlappingReads;
        public final int maxOverlappingReads;
        public final long slowPollLatencyMs;
        public final boolean mainThreadCallbacks;

        public Config(
            long pollIntervalMs,
            long manualIntervalMs,
            boolean preventOverlappingReads,
            int maxOverlappingReads,
            long slowPollLatencyMs,
            boolean mainThreadCallbacks
        ) {
            this.pollIntervalMs = pollIntervalMs;
            this.manualIntervalMs = manualIntervalMs;
            this.preventOverlappingReads = preventOverlappingReads;
            this.maxOverlappingReads = maxOverlappingReads;
            this.slowPollLatencyMs = slowPollLatencyMs;
            this.mainThreadCallbacks = mainThreadCallbacks;
        }

        public static Config defaults() {
            return new Config(
                10L,
                20L,
                false,
                DEFAULT_MAX_OVERLAPPING_READS,
                DEFAULT_SLOW_POLL_LATENCY_MS,
                true
            );
        }

        public Config sanitized() {
            return new Config(
                Math.max(MIN_POLL_INTERVAL_MS, pollIntervalMs),
                Math.max(MIN_POLL_INTERVAL_MS, manualIntervalMs),
                preventOverlappingReads,
                Math.max(1, maxOverlappingReads),
                Math.max(0L, slowPollLatencyMs),
                mainThreadCallbacks
            );
        }
    }

    public static final class ChannelSample {
        public final int[] channels;
        public final long sampledAtElapsedMs;
        public final long readSequence;
        public final long pollLatencyMs;

        public ChannelSample(
            int[] channels,
            long sampledAtElapsedMs,
            long readSequence,
            long pollLatencyMs
        ) {
            this.channels = channels;
            this.sampledAtElapsedMs = sampledAtElapsedMs;
            this.readSequence = readSequence;
            this.pollLatencyMs = pollLatencyMs;
        }
    }

    public static final class ChannelCalibration {
        public final int index;
        public final int min;
        public final int middle;
        public final int max;
        public final boolean reverse;

        public ChannelCalibration(int index, int min, int middle, int max, boolean reverse) {
            this.index = index;
            this.min = min;
            this.middle = middle;
            this.max = max;
            this.reverse = reverse;
        }
    }

    public static final class Diagnostics {
        public final int successfulPolls;
        public final int stalePolls;
        public final int skippedPolls;
        public final int failedPolls;
        public final int slowPolls;
        public final int pendingReads;
        public final int maxPendingReads;
        public final long lastRequestIntervalMs;
        public final long maxRequestIntervalMs;
        public final int requestGapsOverManualInterval;
        public final long lastCallbackIntervalMs;
        public final long maxCallbackIntervalMs;
        public final int callbackGapsOverManualInterval;
        public final int callbackGapsOverDoubleManualInterval;
        public final long lastPollLatencyMs;
        public final long averagePollLatencyMs;
        public final long maxPollLatencyMs;

        public Diagnostics(
            int successfulPolls,
            int stalePolls,
            int skippedPolls,
            int failedPolls,
            int slowPolls,
            int pendingReads,
            int maxPendingReads,
            long lastRequestIntervalMs,
            long maxRequestIntervalMs,
            int requestGapsOverManualInterval,
            long lastCallbackIntervalMs,
            long maxCallbackIntervalMs,
            int callbackGapsOverManualInterval,
            int callbackGapsOverDoubleManualInterval,
            long lastPollLatencyMs,
            long averagePollLatencyMs,
            long maxPollLatencyMs
        ) {
            this.successfulPolls = successfulPolls;
            this.stalePolls = stalePolls;
            this.skippedPolls = skippedPolls;
            this.failedPolls = failedPolls;
            this.slowPolls = slowPolls;
            this.pendingReads = pendingReads;
            this.maxPendingReads = maxPendingReads;
            this.lastRequestIntervalMs = lastRequestIntervalMs;
            this.maxRequestIntervalMs = maxRequestIntervalMs;
            this.requestGapsOverManualInterval = requestGapsOverManualInterval;
            this.lastCallbackIntervalMs = lastCallbackIntervalMs;
            this.maxCallbackIntervalMs = maxCallbackIntervalMs;
            this.callbackGapsOverManualInterval = callbackGapsOverManualInterval;
            this.callbackGapsOverDoubleManualInterval = callbackGapsOverDoubleManualInterval;
            this.lastPollLatencyMs = lastPollLatencyMs;
            this.averagePollLatencyMs = averagePollLatencyMs;
            this.maxPollLatencyMs = maxPollLatencyMs;
        }
    }

    private final class PollThread extends Thread {
        private final long generation;

        private PollThread(long generation) {
            super("Skydroid-G12-SDK");
            this.generation = generation;
        }

        @Override
        public void run() {
            long nextPollAtElapsedMs = SystemClock.elapsedRealtime();
            while (isCurrentGeneration(generation) && !isInterrupted()) {
                long nowMs = SystemClock.elapsedRealtime();
                long waitMs = nextPollAtElapsedMs - nowMs;
                if (waitMs > 0L) {
                    synchronized (this) {
                        try {
                            wait(waitMs);
                        } catch (InterruptedException e) {
                            interrupt();
                        }
                    }
                    continue;
                }
                long readSequence = beginRead(generation);
                if (readSequence > 0L) {
                    long requestedAtElapsedMs = SystemClock.elapsedRealtime();
                    recordRequestStarted(requestedAtElapsedMs);
                    try {
                        KeyManager.INSTANCE.get(
                            RemoteControllerKey.INSTANCE.getKeyChannels(),
                            new ChannelsCallback(generation, readSequence, requestedAtElapsedMs)
                        );
                    } catch (Throwable t) {
                        if (isCurrentGeneration(generation)) {
                            failedPolls.incrementAndGet();
                            finishRead();
                        }
                    }
                }
                long intervalMs = config.pollIntervalMs;
                nextPollAtElapsedMs += intervalMs;
                long afterReadElapsedMs = SystemClock.elapsedRealtime();
                if (nextPollAtElapsedMs <= afterReadElapsedMs) {
                    nextPollAtElapsedMs = afterReadElapsedMs + intervalMs;
                }
            }
        }
    }

    private final class ChannelsCallback implements CompletionCallbackWith<int[]> {
        private final long generation;
        private final long readSequence;
        private final long requestedAtElapsedMs;

        private ChannelsCallback(long generation, long readSequence, long requestedAtElapsedMs) {
            this.generation = generation;
            this.readSequence = readSequence;
            this.requestedAtElapsedMs = requestedAtElapsedMs;
        }

        @Override
        public void onSuccess(int[] channels) {
            if (!isCurrentGeneration(generation)) {
                return;
            }
            long sampledAtElapsedMs = SystemClock.elapsedRealtime();
            long latencyMs = sampledAtElapsedMs - requestedAtElapsedMs;
            recordPollLatency(latencyMs);
            finishRead();
            if (!markDelivered(readSequence)) {
                return;
            }
            recordCallbackDelivered(sampledAtElapsedMs);
            Listener currentListener = listener;
            if (currentListener != null && channels != null) {
                currentListener.onChannels(new ChannelSample(
                    channels.clone(),
                    sampledAtElapsedMs,
                    readSequence,
                    Math.max(0L, latencyMs)
                ));
            }
        }

        @Override
        public void onFailure(SkyException e) {
            if (!isCurrentGeneration(generation)) {
                return;
            }
            failedPolls.incrementAndGet();
            finishRead();
        }
    }
}
