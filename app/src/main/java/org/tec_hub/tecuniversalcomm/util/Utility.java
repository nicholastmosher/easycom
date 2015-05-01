package org.tec_hub.tecuniversalcomm.util;

/**
 * Created by Nick Mosher on 5/1/15.
 * Provides static methods that are generally useful
 * but do not necessarily require their own class.
 */
public class Utility {

    /**
     * Scales the input from the old range to a new one.
     * @param input The input to be transformed.
     * @param oldMin The old range's minimum.
     * @param oldMax The old range's maximum.
     * @param newMin The new range's minimum.
     * @param newMax The new range's maximum.
     * @return The input as represented on the new range.
     */
    public static double scale(double input, double oldMin, double oldMax, double newMin, double newMax) {
        return (input - oldMin) * (newMax - newMin) / (oldMax - oldMin) + newMin;
    }

    /**
     * Scales the input from the old range to a new one.
     * @param input The input to be transformed.
     * @param oldMin The old range's minimum.
     * @param oldMax The old range's maximum.
     * @param newMin The new range's minimum.
     * @param newMax The new range's maximum.
     * @return The input as represented on the new range.
     */
    public static int scale(int input, int oldMin, int oldMax, int newMin, int newMax) {
        return (int) scale((double) input, (double) oldMin, (double) oldMax,(double) newMin, (double) newMax);
    }

    /**
     * If the input is within the range from [min, max], it is unaltered.
     * Otherwise, min or max is returned depending on the range offense.
     * @param input The input to be trimmed.
     * @param min The minimum value that the output may be.
     * @param max The maximum value that the output may be.
     * @return The input, or the minimum or maximum cap.
     */
    public static double trim(double input, double min, double max) {
        if(input > max) {
            input = max;
        } else if(input < min) {
            input = min;
        }
        return input;
    }

    /**
     * If the input is within the range from [-range, range], it is unaltered.
     * Otherwise, -range or range is returned depending on the range offense.
     * If the range is less than 0, then 0 is always returned.
     * @param input The input to be trimmed.
     * @param range The range centered about 0 to trim the input to.
     * @return The input, or a range cap.
     */
    public static double trim(double input, double range) {
        if(range < 0) {
            return 0.0;
        }

        if(input > range) {
            input = range;
        } else if(input < -range) {
            input = -range;
        }
        return input;
    }

    /**
     * If the input is within the range from [min, max], it is unaltered.
     * Otherwise, min or max is returned depending on the range offense.
     * @param input The input to be trimmed.
     * @param min The minimum value that the output may be.
     * @param max The maximum value that the output may be.
     * @return The input, or the minimum or maximum cap.
     */
    public static int trim(int input, int min, int max) {
        if(input > max) {
            input = max;
        } else if(input < min) {
            input = min;
        }
        return input;
    }

    /**
     * If the input is within the range from [-range, range], it is unaltered.
     * Otherwise, -range or range is returned depending on the range offense.
     * If the range is less than 0, then 0 is always returned.
     * @param input The input to be trimmed.
     * @param range The range centered about 0 to trim the input to.
     * @return The input, or a range cap.
     */
    public static int trim(int input, int range) {
        if(range < 0) {
            return 0;
        }

        if(input > range) {
            input = range;
        } else if(input < -range) {
            input = -range;
        }
        return input;
    }

    /**
     * Creates a deadband from range [min, max].  If the input lands
     * in this deadband, then the value def (default) is returned.
     * @param input The input to check against the deadband.
     * @param min The minimum value of the deadband range.
     * @param max The maximum value of the deadband range.
     * @param def The default value to return if the input is inside the deadband.
     * @return The input if it's outside of the deadband, the default value otherwise.
     */
    public static double deadband(double input, double min, double max, double def) {
        if(input <= max && input >= min) {
            return def;
        } else {
            return input;
        }
    }

    /**
     * Creates a deadband from range [min, max].  If the input lands
     * in this deadband, then a value of 0.0 is returned.
     * @param input The input to check against the deadband.
     * @param min The minimum value of the deadband range.
     * @param max The maximum value of the deadband range.
     * @return The input if it's outside of the deadband, 0.0 otherwise.
     */
    public static double deadband(double input, double min, double max) {
        return deadband(input, min, max, 0.0);
    }

    /**
     * Creates a deadband from range [-range, range].  If the input lands
     * in this deadband, then a value of 0.0 is returned.
     * @param input The input to check against the deadband.
     * @param range The distance about 0.0 to place the deadband.
     * @return The input if it's outside of the deadband, 0.0 otherwise.
     */
    public static double deadband(double input, double range) {
        return deadband(input, -range, range);
    }

    /**
     * Creates a deadband from range [min, max].  If the input lands
     * in this deadband, then the value def (default) is returned.
     * @param input The input to check against the deadband.
     * @param min The minimum value of the deadband range.
     * @param max The maximum value of the deadband range.
     * @param def The default value to return if the input is inside the deadband.
     * @return The input if it's outside of the deadband, the default value otherwise.
     */
    public static int deadband(int input, int min, int max, int def) {
        if(input <= max && input >= min) {
            return def;
        } else {
            return input;
        }
    }

    /**
     * Creates a deadband from range [min, max].  If the input lands
     * in this deadband, then a value of 0 is returned.
     * @param input The input to check against the deadband.
     * @param min The minimum value of the deadband range.
     * @param max The maximum value of the deadband range.
     * @return The input if it's outside of the deadband, 0 otherwise.
     */
    public static int deadband(int input, int min, int max) {
        return deadband(input, min, max, 0);
    }

    /**
     * Creates a deadband from range [-range, range].  If the input lands
     * in this deadband, then a value of 0 is returned.
     * @param input The input to check against the deadband.
     * @param range The distance about 0 to place the deadband.
     * @return The input if it's outside of the deadband, 0 otherwise.
     */
    public static int deadband(int input, int range) {
        return deadband(input, -range, range);
    }
}