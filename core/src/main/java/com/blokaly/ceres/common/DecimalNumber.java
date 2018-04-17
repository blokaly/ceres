package com.blokaly.ceres.common;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalNumber implements Comparable<DecimalNumber> {

    public static DecimalNumber ZERO = new DecimalNumber(0L, 0);
    public static DecimalNumber ONE = new DecimalNumber(1L, 0);
    public static DecimalNumber MAX = new DecimalNumber(Long.MAX_VALUE, 0);
    private static int maxLongValue = String.valueOf(Long.MAX_VALUE).length();

    private long longValue;
    private int scale;

    private static long thresholds[][] = {
            { Long.MAX_VALUE, 1L },
            { Long.MAX_VALUE / 10L, 10L },
            { Long.MAX_VALUE / 100L, 100L },
            { Long.MAX_VALUE / 1000L, 1000L },
            { Long.MAX_VALUE / 10000L, 10000L },
            { Long.MAX_VALUE / 100000L, 100000L },
            { Long.MAX_VALUE / 1000000L, 1000000L },
            { Long.MAX_VALUE / 10000000L, 10000000L },
            { Long.MAX_VALUE / 100000000L, 100000000L },
            { Long.MAX_VALUE / 1000000000L, 1000000000L },
            { Long.MAX_VALUE / 10000000000L, 10000000000L },
            { Long.MAX_VALUE / 100000000000L, 100000000000L },
            { Long.MAX_VALUE / 1000000000000L, 1000000000000L },
            { Long.MAX_VALUE / 10000000000000L, 10000000000000L },
            { Long.MAX_VALUE / 100000000000000L, 100000000000000L },
            { Long.MAX_VALUE / 1000000000000000L, 1000000000000000L },
            { Long.MAX_VALUE / 10000000000000000L, 10000000000000000L },
            { Long.MAX_VALUE / 100000000000000000L, 100000000000000000L },
            { Long.MAX_VALUE / 1000000000000000000L, 1000000000000000000L },

    };

    public static long longTenToThe(long value, int n) {
        if (n >= 0 && n < thresholds.length
                && Math.abs(value) <= thresholds[n][0]) {
            return value * thresholds[n][1];
        }
        return Long.MAX_VALUE;
    }

    private DecimalNumber(long longValue, int scale) {
        this.longValue = longValue;
        this.scale = scale;
    }

    public long asLong() {
        if (scale == 0) {
            return longValue;
        } else if (scale < 0) {
            return longTenToThe(longValue, -scale);
        } else {
            return scale < thresholds.length ? longValue / thresholds[scale][1]
                    : 0L;
        }
    }

    public int compareTo(DecimalNumber that) {
        long value;
        if (scale == that.scale) {
            value = longValue - that.longValue;
        } else {
            int sigNum = Long.signum(longValue);
            int sigDiff = sigNum - Long.signum(that.longValue);
            if (sigDiff != 0) {
                return (sigDiff > 0 ? 1 : -1);
            } else if (sigNum == 0) {
                return 0;
            }

            if (scale < that.scale) {
                long scaledVal = longTenToThe(longValue, that.scale - scale);
                if (scaledVal == Long.MAX_VALUE) {
                    return longValue > 0 ? 1 : -1;
                }
                value = scaledVal - that.longValue;
            } else {
                long scaledVal = longTenToThe(that.longValue, scale
                        - that.scale);
                if (scaledVal == Long.MAX_VALUE) {
                    return that.longValue > 0 ? -1 : 1;
                }
                value = longValue - scaledVal;
            }
        }

        return value > 0 ? 1 : (value < 0) ? -1 : 0;
    }

    public DecimalNumber halve() {
        if ((longValue & 1) == 0) {
            return new DecimalNumber(longValue / 2, scale);
        } else {
            if (longValue < Long.MAX_VALUE / 5) {
                return new DecimalNumber(longValue * 5, scale + 1);
            } else {
                throw new ArithmeticException("Overflow. Tried do divide "
                        + this + " by 2");
            }
        }
    }

    public DecimalNumber negate() {
        return new DecimalNumber(-longValue, scale);
    }

    public double asDbl() {
        if (scale == 0) {
            return longValue;
        } else if (scale > 0) {
            return scale < thresholds.length ? (double) longValue
                    / thresholds[scale][1] : longValue / Math.pow(10, scale);
        } else {
            return scale > -thresholds.length ? (double) longValue
                    * thresholds[-scale][1] : longValue * Math.pow(10, -scale);
        }
    }

    public BigDecimal asBD() {
        return BigDecimal.valueOf(longValue, scale);
    }

    public int getScale() {
        return scale;
    }

    public int signum() {
        return Long.signum(longValue);
    }

    public DecimalNumber setScale(int scale, RoundingMode mode) {
        if (this.scale == scale) {
            return this;
        }

        int signum = Long.signum(longValue);
        if (signum == 0) {
            return new DecimalNumber(0, scale);
        }

        if (scale > this.scale) {
            long scaledVal = longTenToThe(longValue, scale - this.scale);
            if (scaledVal == Long.MAX_VALUE) {
                throw new ArithmeticException(
                        "Overflow. Tried to set scale of (" + this.longValue
                                + "," + this.getScale() + ") to "
                                + (scale - this.scale));
            } else {
                return new DecimalNumber(scaledVal, scale);
            }
        } else {
            int scaledPlaces = this.scale - scale;
            if (scaledPlaces >= thresholds.length) {
                if (mode == RoundingMode.UNNECESSARY) {
                    throw new ArithmeticException("Rounding necessary");
                }

                if (mode == RoundingMode.UP) {
                    return signum > 0 ? new DecimalNumber(1, scale)
                            : new DecimalNumber(-1, scale);
                } else if (mode == RoundingMode.DOWN) {
                    return ZERO;
                } else if (mode == RoundingMode.CEILING) {
                    return signum > 0 ? new DecimalNumber(1, scale) : ZERO;
                } else if (mode == RoundingMode.FLOOR) {
                    return signum < 0 ? new DecimalNumber(-1, scale) : ZERO;
                }

                if (scaledPlaces > thresholds.length) {
                    return ZERO;
                }

                long threshold = thresholds[thresholds.length - 1][1] / 2;
                if (signum > 0) {
                    if (longValue > threshold) {
                        return new DecimalNumber(1, scale);
                    } else if (longValue < threshold) {
                        return ZERO;
                    } else {
                        return mode == RoundingMode.HALF_UP ? new DecimalNumber(
                                1, scale)
                                : ZERO;
                    }
                } else {
                    if (longValue < -threshold) {
                        return new DecimalNumber(-1, scale);
                    } else if (longValue > -threshold) {
                        return ZERO;
                    } else {
                        return mode == RoundingMode.HALF_UP ? new DecimalNumber(
                                -1, scale)
                                : ZERO;
                    }
                }
            } else {
                return new DecimalNumber(divide(longValue,
                        thresholds[scaledPlaces][1], mode), scale);
            }
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof DecimalNumber) {
            final DecimalNumber that = (DecimalNumber) o;
            return scale == that.scale && longValue == that.longValue;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 29 * (int) (longValue ^ (longValue >>> 32)) + scale;
    }

    private String getStrRep(boolean commas) {
        if (longValue == 0 && scale <= 0)
            return "0";
        boolean negative = longValue < 0;
        long posLongVal = negative ? -longValue : longValue;
        char[] longChars = Long.toString(posLongVal).toCharArray();
        int numChars = longChars.length;
        StringBuilder sb = new StringBuilder(numChars + 8);
        if (negative) {
            sb.append('-');
        }
        int numBeforePoint = numChars - scale;
        if (commas && numBeforePoint > 3) {
            int nextComma = numBeforePoint % 3;
            if (nextComma == 0)
                nextComma = 3;
            int initialCharCount = scale >= 0 ? numBeforePoint
                    : longChars.length;
            for (int i = 0; i < initialCharCount; i++) {
                if (nextComma == 0) {
                    sb.append(',');
                    nextComma = 2;
                } else {
                    nextComma--;
                }
                sb.append(longChars[i]);
            }
            if (scale > 0) {
                sb.append('.');
                sb.append(longChars, numBeforePoint, scale);
            } else {
                for (int i = scale; i < 0; i++) {
                    if (nextComma == 0) {
                        sb.append(',');
                        nextComma = 2;
                    } else {
                        nextComma--;
                    }
                    sb.append('0');
                }
            }
        } else {
            if (scale > 0) {
                if (numBeforePoint >= 1) {
                    sb.append(longChars, 0, numBeforePoint);
                    sb.append('.');
                    sb.append(longChars, numBeforePoint, scale);
                } else {
                    sb.append('0').append('.');
                    for (; numBeforePoint < 0; numBeforePoint++) {
                        sb.append('0');
                    }
                    sb.append(longChars);
                }
            } else {
                sb.append(longChars);
                for (int i = scale; i < 0; i++) {
                    sb.append('0');
                }
            }
        }
        return sb.toString();
    }

    public String toCommaString() {
        return getStrRep(true);
    }

    @Override
    public String toString() {
        return getStrRep(false);
    }

    public DecimalNumber plus(DecimalNumber dn) {
        return add(dn, true);
    }

    public DecimalNumber minus(DecimalNumber dn) {
        return add(dn, false);
    }

    private DecimalNumber add(DecimalNumber augend, boolean add) {
        long augendLongValue = add ? augend.longValue : -augend.longValue;
        int signum = Long.signum(longValue);
        if (signum == 0) {
            return add ? augend : new DecimalNumber(augendLongValue,
                    augend.scale);
        }
        int augendSignum = Long.signum(augendLongValue);
        if (augendSignum == 0) {
            return this;
        }

        int scaleDiff = scale - augend.scale;
        int newScale;
        long newVal;
        if (scaleDiff == 0) {
            newVal = longValue + augendLongValue;
            newScale = scale;
        } else if (scaleDiff > 0) {
            long scaledVal = longTenToThe(augendLongValue, scaleDiff);
            if (scaledVal == Long.MAX_VALUE) {
                throw new ArithmeticException(
                        "Overflow. Tried to set scale of " + augendLongValue
                                + " to " + scaleDiff);
            }
            newVal = longValue + scaledVal;
            newScale = scale;
        } else {
            long scaledVal = longTenToThe(longValue, -scaleDiff);
            if (scaledVal == Long.MAX_VALUE) {
                throw new ArithmeticException(
                        "Overflow. Tried to set scale of " + this + " to "
                                + -scaleDiff);
            }
            newVal = scaledVal + augendLongValue;
            newScale = augend.scale;
        }

        if (signum > 0) {
            if (augendSignum > 0 && Long.signum(newVal) < 0) {
                throw new ArithmeticException("Overflow error in plus [n1="
                        + this + ", n2=" + augend + "]");
            }
        } else {
            if (augendSignum < 0 && Long.signum(newVal) > 0) {
                throw new ArithmeticException("Underflow erro in plus [n1="
                        + this + ", n2=" + augend + "]");
            }
        }
        return new DecimalNumber(newVal, newScale);
    }

    public static DecimalNumber fromBD(BigDecimal bd) {
        if (bd.precision() > maxLongValue) {
            throw new ArithmeticException("Overflow. Number too big : "
                    + bd.toString());
        } else {
            return new DecimalNumber(bd.unscaledValue().longValue(), bd.scale());
        }
    }

    public static DecimalNumber fromStr(String bd) {
        return fromStr(bd, false);
    }

    public static DecimalNumber fromStr(String bd, boolean keepTrailingZero) {
        int mode = 0;
        int scale = 0;
        int expSign = -1;
        int expScale = 0;
        int trailingZero = 0;
        boolean dpEncountered = false;
        boolean nonZero = false;

        char[] str = bd.toCharArray();
        final int len = str.length;
        boolean negative = str[0] == '-';
        int start = negative ? 1 : 0;
        long unscaled = 0;

        for (int i = start; i < len; i++) {
            char curr = str[i];
            if (mode == 0) {
                if (curr >= '0' && curr <= '9') {
                    if (dpEncountered) {
                        scale++;
                    }
                    if (curr == '0') {
                        trailingZero++;
                    } else {
                        nonZero = true;
                        for (int k = 0; k < trailingZero; k++) {
                            unscaled *= 10;
                        }
                        unscaled = unscaled * 10 + (curr - '0');
                        trailingZero = 0;
                    }
                } else if (curr == '.') {
                    dpEncountered = true;
                } else if (curr == 'E' || curr == 'e') {
                    mode = 1;
                } else {
                    throw new IllegalArgumentException("Illegal character at ["
                            + i + 1 + "] for [" + bd + "]");
                }
            }

            if (mode == 1) {
                if (curr == '+') {
                    expSign = -1;
                } else if (curr == '-') {
                    expSign = 1;
                } else if (curr >= '0' && curr <= '9') {
                    expScale = expScale * 10 + (curr - '0');
                } else if (curr == 'E' || curr == 'e') {

                } else {
                    throw new IllegalArgumentException("Illegal character at ["
                            + i + 1 + "] for [" + bd + "]");
                }
            }
        }

        expScale = expSign * expScale;
        scale += expScale;

        if (nonZero && trailingZero > 0) {
            if (keepTrailingZero) {
                unscaled = longTenToThe(unscaled, trailingZero);
            } else {
                scale -= trailingZero;
            }
        }

        if (negative) {
            unscaled = -unscaled;
        }

        return DecimalNumber.fromLongAndScale(unscaled, scale);
    }

    public static DecimalNumber fromDbl(double val) {
        return fromStr(Double.toString(val));
    }

    public static DecimalNumber fromLong(long l) {
        return new DecimalNumber(l, 0);
    }

    public static DecimalNumber fromLongAndScale(long l, int scale) {
        return new DecimalNumber(l, scale);
    }

    public DecimalNumber abs() {
        return longValue < 0 ? new DecimalNumber(-longValue, scale) : this;
    }

    public DecimalNumber multiply(DecimalNumber multiplicand) {
        long product = longValue * multiplicand.longValue;
        if (longValue != 0L && product / longValue != multiplicand.longValue) {
            throw new ArithmeticException("Overflow. Tried to multiply " + this
                    + " by " + multiplicand);
        }
        long newScale = scale + multiplicand.scale;
        if (newScale != (int) newScale) {
            throw new ArithmeticException("Overflow. Tried to multiply " + this
                    + " by " + multiplicand);
        }
        return new DecimalNumber(product, (int) newScale);
    }

    public DecimalNumber multiply(long multiplicand) {
        long product = longValue * multiplicand;
        if (longValue != 0L && product / longValue != multiplicand) {
            throw new ArithmeticException("Overflow. Tried to multiply " + this
                    + " by " + multiplicand);
        }

        return new DecimalNumber(product, scale);
    }

    public DecimalNumber divide(DecimalNumber divisor, int newScale,
                                RoundingMode mode) {
        DecimalNumber dividend = this;
        long tmpScale = (long) newScale + divisor.scale;
        if (tmpScale != (int) tmpScale) {
            divisor = divisor.stripTrailingZeros();
            tmpScale = (long) newScale + divisor.scale;
            if (tmpScale != (int) tmpScale) {
                throw new ArithmeticException("Overflow. Tried to divide ("
                        + dividend.longValue + "," + dividend.getScale()
                        + ") by (" + divisor.longValue + ","
                        + divisor.getScale() + ") with scaling of " + newScale);
            }
        }
        if (tmpScale > dividend.scale) {
            try {
                dividend = dividend.setScale((int) tmpScale, mode);
            } catch (ArithmeticException ae) {
                divisor = divisor.stripTrailingZeros();
                dividend = dividend.stripTrailingZeros();
                tmpScale = (long) newScale + divisor.scale;
                dividend = dividend.setScale((int) tmpScale, mode);
            }
        } else if (tmpScale < dividend.scale) {
            tmpScale = (long) dividend.scale - newScale;
            if (tmpScale != (int) tmpScale) {
                throw new ArithmeticException("Overflow. Tried to divide ("
                        + dividend.longValue + "," + dividend.getScale()
                        + ") by (" + divisor.longValue + ","
                        + divisor.getScale() + ") with scaling of " + newScale);
            }
            divisor = divisor.setScale((int) tmpScale, mode);
        }

        return new DecimalNumber(divide(dividend.longValue, divisor.longValue,
                mode), newScale);
    }

    public DecimalNumber stripTrailingZeros() {
        if (this.longValue == 0) {
            return DecimalNumber.ZERO;
        }
        long longValue = this.longValue;
        int scale = this.scale;
        while (longValue % 10 == 0) {
            longValue /= 10;
            --scale;
        }
        return scale == this.scale ? this : new DecimalNumber(longValue, scale);
    }

    private static long divide(long value, long divBy, RoundingMode mode) {
        long div = value / divBy;
        long rem = value % divBy;

        if (rem == 0) {
            return div;
        }
        if (mode == RoundingMode.UNNECESSARY) {
            throw new ArithmeticException("Rounding necessary");
        }
        int signum = Long.signum(value) * Long.signum(divBy);
        boolean increment;
        if (mode == RoundingMode.UP) {
            increment = true;
        } else if (mode == RoundingMode.DOWN) {
            increment = false;
        } else if (mode == RoundingMode.CEILING) {
            increment = (signum > 0);
        } else if (mode == RoundingMode.FLOOR) {
            increment = (signum < 0);
        } else {
            long cmpFracHalf = Math.abs(rem * 2) - Math.abs(divBy);
            if (cmpFracHalf > 0) {
                increment = true;
            } else if (cmpFracHalf < 0) {
                increment = false;
            } else {
                if (mode == RoundingMode.HALF_UP) {
                    increment = false;
                } else {
                    if (mode == RoundingMode.HALF_DOWN) {
                        increment = false;
                    } else {
                        increment = (div & 1L) != 0L;
                    }
                }
            }
        }
        if (increment) {
            div += signum;
        }
        return div;
    }

    public DecimalNumber movePointRight(int n) {
        return new DecimalNumber(longValue, scale - n);
    }

    public DecimalNumber movePointLeft(int n) {
        return new DecimalNumber(longValue, scale + n);
    }

    public long unscaledValue() {
        return longValue;
    }

    public boolean isZero() {
        return longValue==0;
    }
}
