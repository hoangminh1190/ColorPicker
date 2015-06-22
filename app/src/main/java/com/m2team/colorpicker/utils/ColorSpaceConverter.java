package com.m2team.colorpicker.utils;


import android.graphics.Color;

/**
 * ColorSpaceConverter
 *
 * @author dvs, hlp
 *         Created Jan 15, 2004
 *         Version 3 posted on ImageJ Mar 12, 2006 by Duane Schwartzwald
 *         vonschwartzwalder at mac.com
 *         Version 4 created Feb. 27, 2007
 *         by Harry Parker, harrylparker at yahoo dot com,
 *         corrects RGB to XYZ (and LAB) conversion.
 */
public class ColorSpaceConverter {
    private static final int FLOAT_POINT_PERCENT = 0;
    private static final int FLOAT_POINT_LAB = 1;
    private static final int FLOAT_POINT_XYZ_xyY = 4;
    /**
     * reference white in XYZ coordinates
     */
    public double[] D65 = {95.0429, 100.0, 108.8900};
    public double[] whitePoint = D65;

    /**
     * reference white in xyY coordinates
     */
    public double[] chromaD65 = {0.3127, 0.3290, 100.0};
    public double[] chromaWhitePoint = chromaD65;

    /**
     * sRGB to XYZ conversion matrix
     */
    public double[][] M = {{0.4124, 0.3576, 0.1805},
            {0.2126, 0.7152, 0.0722},
            {0.0193, 0.1192, 0.9505}};

    /**
     * XYZ to sRGB conversion matrix
     */
    public double[][] Mi = {{3.2406, -1.5372, -0.4986},
            {-0.9689, 1.8758, 0.0415},
            {0.0557, -0.2040, 1.0570}};

    public String rgbToHex(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public int[] hexToRGB(String hex) {
            if (!hex.contains("#")) hex = "#" + hex;
            int color = Color.parseColor(hex);
            int[] rgb = new int[3];
            rgb[0] = Color.red(color);
            rgb[1] = Color.green(color);
            rgb[2] = Color.blue(color);
            return rgb;
    }

    public float[] rgbToCmyk(float[] rgb) {
        float[] cmyk = new float[4];
        float computedC = 1 - (rgb[0] / 255);
        float computedM = 1 - (rgb[1] / 255);
        float computedY = 1 - (rgb[2] / 255);

        float minCMY = Math.min(computedC,
                Math.min(computedM, computedY));
        cmyk[0] = Utils.round((computedC - minCMY) / (1 - minCMY) * 100, FLOAT_POINT_PERCENT);
        cmyk[1] = Utils.round((computedM - minCMY) / (1 - minCMY) * 100, FLOAT_POINT_PERCENT) ;
        cmyk[2] = Utils.round((computedY - minCMY) / (1 - minCMY) * 100, FLOAT_POINT_PERCENT) ;
        cmyk[3] = Utils.round(minCMY * 100, FLOAT_POINT_PERCENT);
        return cmyk;
    }

    public float[] rgbToHSL(float[] rgb) {
        //  Get RGB values in the range 0 - 1
        float r = rgb[0] / 255;
        float g = rgb[1] / 255;
        float b = rgb[2] / 255;

        //	Minimum and Maximum RGB values are used in the HSL calculations

        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        //  Calculate the Hue

        float h = 0;

        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;

        //  Calculate the Luminance

        float l = (max + min) / 2;

        //  Calculate the Saturation

        float s;

        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);

        return new float[]{Utils.round(h, FLOAT_POINT_PERCENT), Utils.round(s * 100, FLOAT_POINT_PERCENT) , Utils.round(l * 100, FLOAT_POINT_PERCENT)};
    }

    /**
     * Convert LAB to RGB.
     *
     * @param L
     * @param a
     * @param b
     * @return RGB values
     */
    public int[] LABtoRGB(double L, double a, double b) {
        return XYZtoRGB(LABtoXYZ(L, a, b));
    }

    /**
     * @param Lab
     * @return RGB values
     */
    public int[] LABtoRGB(double[] Lab) {
        return XYZtoRGB(LABtoXYZ(Lab));
    }

    /**
     * Convert LAB to XYZ.
     *
     * @param L
     * @param a
     * @param b
     * @return XYZ values
     */
    public double[] LABtoXYZ(double L, double a, double b) {
        double[] result = new double[3];

        double y = (L + 16.0) / 116.0;
        double y3 = Math.pow(y, 3.0);
        double x = (a / 500.0) + y;
        double x3 = Math.pow(x, 3.0);
        double z = y - (b / 200.0);
        double z3 = Math.pow(z, 3.0);

        if (y3 > 0.008856) {
            y = y3;
        } else {
            y = (y - (16.0 / 116.0)) / 7.787;
        }
        if (x3 > 0.008856) {
            x = x3;
        } else {
            x = (x - (16.0 / 116.0)) / 7.787;
        }
        if (z3 > 0.008856) {
            z = z3;
        } else {
            z = (z - (16.0 / 116.0)) / 7.787;
        }

        result[0] = Utils.round(x * whitePoint[0], FLOAT_POINT_XYZ_xyY);
        result[1] = Utils.round(y * whitePoint[1], FLOAT_POINT_XYZ_xyY);
        result[2] = Utils.round(z * whitePoint[2], FLOAT_POINT_XYZ_xyY);

        return result;
    }

    /**
     * Convert LAB to XYZ.
     *
     * @param Lab
     * @return XYZ values
     */
    public double[] LABtoXYZ(double[] Lab) {
        return LABtoXYZ(Lab[0], Lab[1], Lab[2]);
    }

    /**
     * @param R
     * @param G
     * @param B
     * @return Lab values
     */
    public double[] RGBtoLAB(int R, int G, int B) {
        return XYZtoLAB(RGBtoXYZ(R, G, B));
    }

    /**
     * @param RGB
     * @return Lab values
     */
    public double[] RGBtoLAB(int[] RGB) {
        return XYZtoLAB(RGBtoXYZ(RGB));
    }

    /**
     * Convert RGB to XYZ
     *
     * @param R
     * @param G
     * @param B
     * @return XYZ in double array.
     */
    public double[] RGBtoXYZ(int R, int G, int B) {
        double[] result = new double[3];

        // convert 0..255 into 0..1
        double r = R / 255.0;
        double g = G / 255.0;
        double b = B / 255.0;

        // assume sRGB
        if (r <= 0.04045) {
            r = r / 12.92;
        } else {
            r = Math.pow(((r + 0.055) / 1.055), 2.4);
        }
        if (g <= 0.04045) {
            g = g / 12.92;
        } else {
            g = Math.pow(((g + 0.055) / 1.055), 2.4);
        }
        if (b <= 0.04045) {
            b = b / 12.92;
        } else {
            b = Math.pow(((b + 0.055) / 1.055), 2.4);
        }

        r *= 100.0;
        g *= 100.0;
        b *= 100.0;

        // [X Y Z] = [r g b][M]
        result[0] = Utils.round((r * M[0][0]) + (g * M[0][1]) + (b * M[0][2]), FLOAT_POINT_XYZ_xyY);
        result[1] = Utils.round((r * M[1][0]) + (g * M[1][1]) + (b * M[1][2]), FLOAT_POINT_XYZ_xyY);
        result[2] = Utils.round((r * M[2][0]) + (g * M[2][1]) + (b * M[2][2]), FLOAT_POINT_XYZ_xyY);

        return result;
    }

    /**
     * Convert RGB to XYZ
     *
     * @param RGB
     * @return XYZ in double array.
     */
    public double[] RGBtoXYZ(int[] RGB) {
        return RGBtoXYZ(RGB[0], RGB[1], RGB[2]);
    }

    /**
     * @param x
     * @param y
     * @param Y
     * @return XYZ values
     */
    public double[] xyYtoXYZ(double x, double y, double Y) {
        double[] result = new double[3];
        if (y == 0) {
            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
        } else {
            result[0] = (x * Y) / y;
            result[1] = Y;
            result[2] = ((1 - x - y) * Y) / y;
        }
        return result;
    }

    /**
     * @param xyY
     * @return XYZ values
     */
    public double[] xyYtoXYZ(double[] xyY) {
        return xyYtoXYZ(xyY[0], xyY[1], xyY[2]);
    }

    /**
     * Convert XYZ to LAB.
     *
     * @param X
     * @param Y
     * @param Z
     * @return Lab values
     */
    public double[] XYZtoLAB(double X, double Y, double Z) {

        double x = X / whitePoint[0];
        double y = Y / whitePoint[1];
        double z = Z / whitePoint[2];

        if (x > 0.008856) {
            x = Math.pow(x, 1.0 / 3.0);
        } else {
            x = (7.787 * x) + (16.0 / 116.0);
        }
        if (y > 0.008856) {
            y = Math.pow(y, 1.0 / 3.0);
        } else {
            y = (7.787 * y) + (16.0 / 116.0);
        }
        if (z > 0.008856) {
            z = Math.pow(z, 1.0 / 3.0);
        } else {
            z = (7.787 * z) + (16.0 / 116.0);
        }

        double[] result = new double[3];

        result[0] = Utils.round((116.0 * y) - 16.0, FLOAT_POINT_LAB);
        result[1] = Utils.round(500.0 * (x - y), FLOAT_POINT_LAB);
        result[2] = Utils.round(200.0 * (y - z), FLOAT_POINT_LAB);

        return result;
    }

    /**
     * Convert XYZ to LAB.
     *
     * @param XYZ
     * @return Lab values
     */
    public double[] XYZtoLAB(double[] XYZ) {
        return XYZtoLAB(XYZ[0], XYZ[1], XYZ[2]);
    }

    /**
     * Convert XYZ to RGB.
     *
     * @param X
     * @param Y
     * @param Z
     * @return RGB in int array.
     */
    public int[] XYZtoRGB(double X, double Y, double Z) {
        int[] result = new int[3];

        double x = X / 100.0;
        double y = Y / 100.0;
        double z = Z / 100.0;

        // [r g b] = [X Y Z][Mi]
        double r = (x * Mi[0][0]) + (y * Mi[0][1]) + (z * Mi[0][2]);
        double g = (x * Mi[1][0]) + (y * Mi[1][1]) + (z * Mi[1][2]);
        double b = (x * Mi[2][0]) + (y * Mi[2][1]) + (z * Mi[2][2]);

        // assume sRGB
        if (r > 0.0031308) {
            r = ((1.055 * Math.pow(r, 1.0 / 2.4)) - 0.055);
        } else {
            r = (r * 12.92);
        }
        if (g > 0.0031308) {
            g = ((1.055 * Math.pow(g, 1.0 / 2.4)) - 0.055);
        } else {
            g = (g * 12.92);
        }
        if (b > 0.0031308) {
            b = ((1.055 * Math.pow(b, 1.0 / 2.4)) - 0.055);
        } else {
            b = (b * 12.92);
        }

        r = (r < 0) ? 0 : r;
        g = (g < 0) ? 0 : g;
        b = (b < 0) ? 0 : b;

        // convert 0..1 into 0..255
        result[0] = (int) Math.round(r * 255);
        result[1] = (int) Math.round(g * 255);
        result[2] = (int) Math.round(b * 255);

        return result;
    }

    /**
     * Convert XYZ to RGB
     *
     * @param XYZ in a double array.
     * @return RGB in int array.
     */
    public int[] XYZtoRGB(double[] XYZ) {
        return XYZtoRGB(XYZ[0], XYZ[1], XYZ[2]);
    }

    /**
     * @param X
     * @param Y
     * @param Z
     * @return xyY values
     */
    public double[] XYZtoxyY(double X, double Y, double Z) {
        double[] result = new double[3];
        if ((X + Y + Z) == 0) {
            result[0] = chromaWhitePoint[0];
            result[1] = chromaWhitePoint[1];
            result[2] = chromaWhitePoint[2];
        } else {
            result[0] = Utils.round(X / (X + Y + Z), FLOAT_POINT_XYZ_xyY);
            result[1] = Utils.round(Y / (X + Y + Z), FLOAT_POINT_XYZ_xyY);
            result[2] = Utils.round(Y, FLOAT_POINT_XYZ_xyY);
        }
        return result;
    }
}
