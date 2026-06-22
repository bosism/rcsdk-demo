# Skydroid G12 SDK Bridge

Small Android/Java helper for reading Skydroid G12 remote-controller channels through the Skydroid RCSDK.

The bridge owns the RCSDK connection lifecycle, polls the G12 channel key at a configured interval, and reports channel samples through a listener. It is intended for host applications that want fresh G12 channel values without duplicating RCSDK polling and diagnostics code.

## Scope

This code is specific to the Skydroid G12 controller.

It does not provide UI, transport, vehicle-control, or application-specific channel mapping. The host application is responsible for deciding how channel values are interpreted and where they are sent.

## Dependency

The host Android application must provide the Skydroid RCSDK AAR on its build classpath.

The bridge currently uses:

- `RCSDKManager` for SDK initialization and RC connection management
- `KeyManager` with `RemoteControllerKey.getKeyChannels()` for channel polling
- optional metadata reads for control mode and channel calibration settings

## Basic Usage

Create one `SkydroidG12SdkBridge`, start it with an Android `Context`, a `Config`, and a `Listener`, then stop it when the host no longer needs G12 input.

```java
SkydroidG12SdkBridge bridge = new SkydroidG12SdkBridge();

bridge.start(
    context,
    SkydroidG12SdkBridge.Config.defaults(),
    new SkydroidG12SdkBridge.Listener() {
        @Override
        public void onRcConnected(String deviceType) {
        }

        @Override
        public void onRcConnectFailed(String error) {
        }

        @Override
        public void onRcDisconnected() {
        }

        @Override
        public void onChannels(SkydroidG12SdkBridge.ChannelSample sample) {
            int[] channels = sample.channels;
        }

        @Override
        public void onControlMode(String controlMode) {
        }

        @Override
        public void onChannelSettings(List<SkydroidG12SdkBridge.ChannelCalibration> channels) {
        }

        @Override
        public void onMetadataReadFailed(String metadata, String error) {
        }
    });

bridge.stop();
```

## Polling Model

The bridge polls `RemoteControllerKey.getKeyChannels()` on a dedicated polling thread.

By default, overlapping SDK reads are allowed and capped. This is useful because a single SDK read may take longer than the requested poll interval. When callbacks return out of order, only the newest read sequence is delivered; older completed reads are counted as stale and discarded.

Important configuration fields:

- `pollIntervalMs`: interval between SDK channel read requests
- `manualIntervalMs`: downstream/manual-control interval used for gap diagnostics
- `preventOverlappingReads`: when true, only one SDK read may be pending
- `maxOverlappingReads`: cap for pending SDK reads when overlapping reads are enabled
- `slowPollLatencyMs`: latency threshold counted by diagnostics
- `mainThreadCallbacks`: passed to `RCSDKManager.setMainThreadCallBack`

Current defaults:

- poll interval: `10ms`
- manual interval: `20ms`
- overlapping reads: enabled
- max overlapping reads: `6`
- slow latency threshold: `200ms`
- main-thread callbacks: enabled

## Diagnostics

`getDiagnostics()` returns counters and timing information intended for live tuning and troubleshooting:

- successful, stale, skipped, failed, and slow polls
- current and maximum pending SDK reads
- last and maximum request intervals
- last and maximum callback intervals
- callback/request gaps compared with `manualIntervalMs`
- last, average, and maximum SDK poll latency

`resetDiagnostics()` clears the counters while preserving the current pending-read baseline.

## Notes

The bridge reports raw channel values from the SDK. It does not normalize axes, apply deadbands, infer buttons, or enforce any vehicle-control rate. Those decisions belong in the host application.
