package com.cs407.lab09

/**
 * Represents a ball that can move. (No Android UI imports!)
 *
 * Constructor parameters:
 * - backgroundWidth: the width of the background, of type Float
 * - backgroundHeight: the height of the background, of type Float
 * - ballSize: the width/height of the ball, of type Float
 */
class Ball(
    private val backgroundWidth: Float,
    private val backgroundHeight: Float,
    private val ballSize: Float
) {
    var posX = 0f
    var posY = 0f
    var velocityX = 0f
    var velocityY = 0f
    private var accX = 0f
    private var accY = 0f

    private var isFirstUpdate = true

    init {
        // Call reset()
        reset()
    }

    /**
     * Updates the ball's position and velocity based on the given acceleration and time step.
     * Uses Equations (1) and (2) from the handout for both x and y axes.
     */
    fun updatePositionAndVelocity(xAcc: Float, yAcc: Float, dT: Float) {
        if (isFirstUpdate) {
            isFirstUpdate = false
            accX = xAcc
            accY = yAcc
            return
        }

        // Previous acceleration (a0)
        val a0x = accX
        val a0y = accY

        // New acceleration (a1)
        val a1x = xAcc
        val a1y = yAcc

        // --- Velocity update (Equation 1) ---
        // v1 = v0 + 1/2 * (a1 + a0) * (t1 - t0)
        val newVX = velocityX + 0.5f * (a1x + a0x) * dT
        val newVY = velocityY + 0.5f * (a1y + a0y) * dT

        // --- Distance / position update (Equation 2) ---
        // l = v0 * dt + 1/6 * dt^2 * (3a0 + a1)
        val dt2 = dT * dT
        val dx = velocityX * dT + (1f / 6f) * dt2 * (3f * a0x + a1x)
        val dy = velocityY * dT + (1f / 6f) * dt2 * (3f * a0y + a1y)

        posX += dx
        posY += dy

        velocityX = newVX
        velocityY = newVY

        // Store the latest acceleration as the previous acceleration for next step
        accX = a1x
        accY = a1y

        // Ensure the ball stays inside the field
        checkBoundaries()
    }

    /**
     * Ensures the ball does not move outside the boundaries.
     * When it collides, velocity and acceleration perpendicular to the
     * boundary should be set to 0.
     */
    fun checkBoundaries() {
        val maxX = backgroundWidth - ballSize
        val maxY = backgroundHeight - ballSize

        // Left wall
        if (posX < 0f) {
            posX = 0f
            velocityX = 0f
            accX = 0f
        }

        // Right wall
        if (posX > maxX) {
            posX = maxX
            velocityX = 0f
            accX = 0f
        }

        // Top wall
        if (posY < 0f) {
            posY = 0f
            velocityY = 0f
            accY = 0f
        }

        // Bottom wall
        if (posY > maxY) {
            posY = maxY
            velocityY = 0f
            accY = 0f
        }
    }

    /**
     * Resets the ball to the center of the screen with zero
     * velocity and acceleration.
     */
    fun reset() {
        posX = (backgroundWidth - ballSize) / 2f
        posY = (backgroundHeight - ballSize) / 2f
        velocityX = 0f
        velocityY = 0f
        accX = 0f
        accY = 0f
        isFirstUpdate = true
    }
}