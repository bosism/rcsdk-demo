package com.skydroid.rcsdkdemo

import kotlin.math.roundToInt

data class ManualControlAxes(
    val targetSystem: Int,
    val x: Int,
    val y: Int,
    val z: Int,
    val r: Int,
    val buttons: Int = 0
)

class MavlinkV1Encoder(
    private val systemId: Int,
    private val componentId: Int,
) {
    private var sequence = 0

    fun encodeHeartbeatGcs(): ByteArray {
        val payload = ByteArray(HEARTBEAT_PAYLOAD_LENGTH)
        putUint32(payload, 0, 0)
        payload[4] = MAV_TYPE_GCS.toByte()
        payload[5] = MAV_AUTOPILOT_INVALID.toByte()
        payload[6] = MAV_MODE_FLAG_MANUAL_INPUT_ENABLED.toByte()
        payload[7] = MAV_STATE_ACTIVE.toByte()
        payload[8] = MAVLINK_VERSION.toByte()
        return buildFrame(HEARTBEAT_MESSAGE_ID, HEARTBEAT_CRC_EXTRA, payload)
    }

    fun encodeManualControl(axes: ManualControlAxes): ByteArray {
        val payload = ByteArray(MANUAL_CONTROL_PAYLOAD_LENGTH)
        putInt16(payload, 0, axes.x)
        putInt16(payload, 2, axes.y)
        putInt16(payload, 4, axes.z)
        putInt16(payload, 6, axes.r)
        putUint16(payload, 8, axes.buttons)
        payload[10] = axes.targetSystem.toByte()
        return buildFrame(MANUAL_CONTROL_MESSAGE_ID, MANUAL_CONTROL_CRC_EXTRA, payload)
    }

    private fun nextSequence(): Int {
        val current = sequence
        sequence = (sequence + 1) and 0xFF
        return current
    }

    private fun putInt16(buffer: ByteArray, offset: Int, value: Int) {
        val shortValue = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        buffer[offset] = (shortValue and 0xFF).toByte()
        buffer[offset + 1] = ((shortValue ushr 8) and 0xFF).toByte()
    }

    private fun putUint16(buffer: ByteArray, offset: Int, value: Int) {
        val intValue = value.coerceIn(0, 0xFFFF)
        buffer[offset] = (intValue and 0xFF).toByte()
        buffer[offset + 1] = ((intValue ushr 8) and 0xFF).toByte()
    }

    private fun putUint32(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        buffer[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        buffer[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    private fun buildFrame(messageId: Int, crcExtra: Int, payload: ByteArray): ByteArray {
        val frame = ByteArray(payload.size + MAVLINK_V1_OVERHEAD)
        frame[0] = MAVLINK_V1_MAGIC.toByte()
        frame[1] = payload.size.toByte()
        frame[2] = nextSequence().toByte()
        frame[3] = systemId.toByte()
        frame[4] = componentId.toByte()
        frame[5] = messageId.toByte()
        payload.copyInto(frame, destinationOffset = MAVLINK_V1_HEADER_LENGTH)

        val crc = mavlinkCrc(frame, 1, payload.size + MAVLINK_V1_HEADER_LENGTH - 1)
        val finalCrc = mavlinkAccumulate(crcExtra, crc)
        frame[frame.size - 2] = (finalCrc and 0xFF).toByte()
        frame[frame.size - 1] = ((finalCrc ushr 8) and 0xFF).toByte()
        return frame
    }

    private fun mavlinkCrc(buffer: ByteArray, offset: Int, length: Int): Int {
        var crc = 0xFFFF
        for (index in offset until (offset + length)) {
            crc = mavlinkAccumulate(buffer[index].toInt() and 0xFF, crc)
        }
        return crc
    }

    private fun mavlinkAccumulate(value: Int, crc: Int): Int {
        var tmp = value xor (crc and 0xFF)
        tmp = tmp xor (tmp shl 4 and 0xFF)
        val next = (crc ushr 8) xor (tmp shl 8) xor (tmp shl 3) xor (tmp ushr 4)
        return next and 0xFFFF
    }

    companion object {
        private const val MAVLINK_V1_MAGIC = 0xFE
        private const val MAVLINK_V1_HEADER_LENGTH = 6
        private const val MAVLINK_V1_OVERHEAD = 8
        private const val HEARTBEAT_MESSAGE_ID = 0
        private const val HEARTBEAT_PAYLOAD_LENGTH = 9
        private const val HEARTBEAT_CRC_EXTRA = 50
        private const val MANUAL_CONTROL_MESSAGE_ID = 69
        private const val MANUAL_CONTROL_PAYLOAD_LENGTH = 11
        private const val MANUAL_CONTROL_CRC_EXTRA = 243
        private const val MAV_TYPE_GCS = 6
        private const val MAV_AUTOPILOT_INVALID = 8
        private const val MAV_MODE_FLAG_MANUAL_INPUT_ENABLED = 64
        private const val MAV_STATE_ACTIVE = 4
        private const val MAVLINK_VERSION = 3
    }
}

data class AxisCalibration(
    val min: Int = 1000,
    val middle: Int = 1500,
    val max: Int = 2000,
    val reverse: Boolean = false,
)

fun normalizeCenteredAxis(rawValue: Int, calibration: AxisCalibration): Int {
    val clamped = rawValue.coerceIn(calibration.min, calibration.max)
    val normalized = if (clamped >= calibration.middle) {
        val upperSpan = (calibration.max - calibration.middle).coerceAtLeast(1)
        ((clamped - calibration.middle).toDouble() / upperSpan * 1000.0).roundToInt()
    } else {
        val lowerSpan = (calibration.middle - calibration.min).coerceAtLeast(1)
        (-(calibration.middle - clamped).toDouble() / lowerSpan * 1000.0).roundToInt()
    }
    return if (calibration.reverse) -normalized else normalized
}

fun normalizePositiveThrottle(rawValue: Int, calibration: AxisCalibration): Int {
    val clamped = rawValue.coerceIn(calibration.min, calibration.max)
    val span = (calibration.max - calibration.min).coerceAtLeast(1)
    var normalized = ((clamped - calibration.min).toDouble() / span * 1000.0).roundToInt()
    if (calibration.reverse) {
        normalized = 1000 - normalized
    }
    return normalized.coerceIn(0, 1000)
}
