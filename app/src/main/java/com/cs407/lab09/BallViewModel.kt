package com.cs407.lab09

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BallViewModel : ViewModel() {

    private var ball: Ball? = null
    private var lastTimestamp: Long = 0L

    // Scaling factor to make the ball movement more responsive
    // The gravity sensor values are in m/s^2 (about 9.8 max), which is too small for visible movement
    private val ACCELERATION_SCALE = 50f

    // Expose the ball's position as a StateFlow
    private val _ballPosition = MutableStateFlow(Offset.Zero)
    val ballPosition: StateFlow<Offset> = _ballPosition.asStateFlow()

    /**
     * Called by the UI when the game field's size is known.
     */
    fun initBall(fieldWidth: Float, fieldHeight: Float, ballSizePx: Float) {
        if (ball == null) {
            // Initialize the ball instance
            ball = Ball(
                backgroundWidth = fieldWidth,
                backgroundHeight = fieldHeight,
                ballSize = ballSizePx
            )

            // Update the StateFlow with the initial position
            ball?.let {
                _ballPosition.value = Offset(it.posX, it.posY)
            }
        }
    }

    /**
     * Called by the SensorEventListener in the UI.
     */
    fun onSensorDataChanged(event: SensorEvent) {
        // Ensure ball is initialized
        val currentBall = ball ?: return

        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            if (lastTimestamp != 0L) {
                // Calculate the time difference (dT) in seconds
                val NS2S = 1.0f / 1_000_000_000.0f
                val dT = (event.timestamp - lastTimestamp) * NS2S

                // According to Android documentation:
                // TYPE_GRAVITY reports "Force of gravity along the x/y/z axis" in m/s^2
                //
                // Coordinate mapping:
                // - Sensor X-axis: points right (same as screen)
                // - Sensor Y-axis: points UP (opposite of screen which points DOWN)
                // - Sensor Z-axis: points out of screen
                //
                // When device is flat on table:
                // - gravity pulls down (-Z direction in sensor coords)
                // - sensor reports: values[2] ≈ 9.8 (positive Z is gravity force)
                //
                // When tilting device right (clockwise around Y):
                // - gravity has component in +X direction
                // - ball should roll right (+X on screen)
                // - sensor reports: positive values[0]
                //
                // When tilting device forward (top edge down):
                // - gravity has component in +Y direction (sensor coords, which is UP)
                // - ball should roll toward top of screen (which is -Y in screen coords)
                // - sensor reports: positive values[1]
                //
                // Therefore:
                // - X acceleration: use sensor X directly (positive tilt right = positive X = ball rolls right)
                // - Y acceleration: NEGATE sensor Y (positive sensor Y = UP = should make ball go up screen = negative screen Y)

                val xAcc = event.values[0] * ACCELERATION_SCALE
                val yAcc = -event.values[1] * ACCELERATION_SCALE

                // Update the ball's position and velocity
                currentBall.updatePositionAndVelocity(
                    xAcc = xAcc,
                    yAcc = yAcc,
                    dT = dT
                )

                // Update the StateFlow to notify the UI
                _ballPosition.update {
                    Offset(currentBall.posX, currentBall.posY)
                }
            }

            // Update the lastTimestamp
            lastTimestamp = event.timestamp
        }
    }

    fun reset() {
        // Reset the ball's state
        ball?.reset()

        // Update the StateFlow with the reset position
        ball?.let {
            _ballPosition.value = Offset(it.posX, it.posY)
        }

        // Reset the lastTimestamp so the next sensor event re-inits timing
        lastTimestamp = 0L
    }
}