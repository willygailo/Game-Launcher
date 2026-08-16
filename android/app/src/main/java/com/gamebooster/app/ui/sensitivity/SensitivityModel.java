package com.gamebooster.app.ui.sensitivity;

/**
 * Data model for calculated in-game sensitivity recommendations.
 */
public class SensitivityModel {

    public int freeLook;
    public int noScope3rdPerson; // TPP
    public int noScope1stPerson; // FPP
    public int redDotHolo;
    public int scope2x;
    public int scope3x;
    public int scope4x;
    public int scope6x;
    public int scope8x;

    public int tppFov;
    public int fppFov;
    public int sprintSensitivity;
    public int aimAssistStrength;

    public int gyroNoScope;
    public int gyroRedDot;
    public int gyro2x;
    public int gyro3x;
    public int gyro4x;
    public int gyro6x;
    public int gyro8x;

    public String summary;
}
