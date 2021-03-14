package com.google.common.math;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.math.RoundingMode;

@GwtCompatible(emulated = true)
public final class LongMath {
  @VisibleForTesting
  static final long MAX_POWER_OF_SQRT2_UNSIGNED = -5402926248376769404L;
  
  public static boolean isPowerOfTwo(long x) {
    return ((x > 0L)) & (((x & x - 1L) == 0L));
  }
  
  @VisibleForTesting
  static int lessThanBranchFree(long x, long y) {
    return (int)((x - y ^ 0xFFFFFFFFFFFFFFFFL ^ 0xFFFFFFFFFFFFFFFFL) >>> 63L);
  }
  
  public static int log2(long x, RoundingMode mode) {
    int leadingZeros;
    long cmp;
    int logFloor;
    MathPreconditions.checkPositive("x", x);
    switch (mode) {
      case UNNECESSARY:
        MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
      case DOWN:
      case FLOOR:
        return 63 - Long.numberOfLeadingZeros(x);
      case UP:
      case CEILING:
        return 64 - Long.numberOfLeadingZeros(x - 1L);
      case HALF_DOWN:
      case HALF_UP:
      case HALF_EVEN:
        leadingZeros = Long.numberOfLeadingZeros(x);
        cmp = -5402926248376769404L >>> leadingZeros;
        logFloor = 63 - leadingZeros;
        return logFloor + lessThanBranchFree(cmp, x);
    } 
    throw new AssertionError("impossible");
  }
  
  @GwtIncompatible("TODO")
  public static int log10(long x, RoundingMode mode) {
    MathPreconditions.checkPositive("x", x);
    int logFloor = log10Floor(x);
    long floorPow = powersOf10[logFloor];
    switch (mode) {
      case UNNECESSARY:
        MathPreconditions.checkRoundingUnnecessary((x == floorPow));
      case DOWN:
      case FLOOR:
        return logFloor;
      case UP:
      case CEILING:
        return logFloor + lessThanBranchFree(floorPow, x);
      case HALF_DOWN:
      case HALF_UP:
      case HALF_EVEN:
        return logFloor + lessThanBranchFree(halfPowersOf10[logFloor], x);
    } 
    throw new AssertionError();
  }
  
  @GwtIncompatible("TODO")
  static int log10Floor(long x) {
    int y = maxLog10ForLeadingZeros[Long.numberOfLeadingZeros(x)];
    return y - lessThanBranchFree(x, powersOf10[y]);
  }
  
  @VisibleForTesting
  static final byte[] maxLog10ForLeadingZeros = new byte[] { 
      19, 18, 18, 18, 18, 17, 17, 17, 16, 16, 
      16, 15, 15, 15, 15, 14, 14, 14, 13, 13, 
      13, 12, 12, 12, 12, 11, 11, 11, 10, 10, 
      10, 9, 9, 9, 9, 8, 8, 8, 7, 7, 
      7, 6, 6, 6, 6, 5, 5, 5, 4, 4, 
      4, 3, 3, 3, 3, 2, 2, 2, 1, 1, 
      1, 0, 0, 0 };
  
  @GwtIncompatible("TODO")
  @VisibleForTesting
  static final long[] powersOf10 = new long[] { 
      1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 
      10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L };
  
  @GwtIncompatible("TODO")
  @VisibleForTesting
  static final long[] halfPowersOf10 = new long[] { 
      3L, 31L, 316L, 3162L, 31622L, 316227L, 3162277L, 31622776L, 316227766L, 3162277660L, 
      31622776601L, 316227766016L, 3162277660168L, 31622776601683L, 316227766016837L, 3162277660168379L, 31622776601683793L, 316227766016837933L, 3162277660168379331L };
  
  @VisibleForTesting
  static final long FLOOR_SQRT_MAX_LONG = 3037000499L;
  
  @GwtIncompatible("TODO")
  public static long pow(long b, int k) {
    MathPreconditions.checkNonNegative("exponent", k);
    if (-2L <= b && b <= 2L) {
      switch ((int)b) {
        case 0:
          return (k == 0) ? 1L : 0L;
        case 1:
          return 1L;
        case -1:
          return ((k & 0x1) == 0) ? 1L : -1L;
        case 2:
          return (k < 64) ? (1L << k) : 0L;
        case -2:
          if (k < 64)
            return ((k & 0x1) == 0) ? (1L << k) : -(1L << k); 
          return 0L;
      } 
      throw new AssertionError();
    } 
    for (long accum = 1L;; k >>= 1) {
      switch (k) {
        case 0:
          return accum;
        case 1:
          return accum * b;
      } 
      accum *= ((k & 0x1) == 0) ? 1L : b;
      b *= b;
    } 
  }
  
  @GwtIncompatible("TODO")
  public static long sqrt(long x, RoundingMode mode) {
    long sqrtFloor, halfSquare;
    MathPreconditions.checkNonNegative("x", x);
    if (fitsInInt(x))
      return IntMath.sqrt((int)x, mode); 
    long guess = (long)Math.sqrt(x);
    long guessSquared = guess * guess;
    switch (mode) {
      case UNNECESSARY:
        MathPreconditions.checkRoundingUnnecessary((guessSquared == x));
        return guess;
      case DOWN:
      case FLOOR:
        if (x < guessSquared)
          return guess - 1L; 
        return guess;
      case UP:
      case CEILING:
        if (x > guessSquared)
          return guess + 1L; 
        return guess;
      case HALF_DOWN:
      case HALF_UP:
      case HALF_EVEN:
        sqrtFloor = guess - ((x < guessSquared) ? 1L : 0L);
        halfSquare = sqrtFloor * sqrtFloor + sqrtFloor;
        return sqrtFloor + lessThanBranchFree(halfSquare, x);
    } 
    throw new AssertionError();
  }
  
  @GwtIncompatible("TODO")
  public static long divide(long p, long q, RoundingMode mode) {
    boolean increment;
    long absRem, cmpRemToHalfDivisor;
    Preconditions.checkNotNull(mode);
    long div = p / q;
    long rem = p - q * div;
    if (rem == 0L)
      return div; 
    int signum = 0x1 | (int)((p ^ q) >> 63L);
    switch (mode) {
      case UNNECESSARY:
        MathPreconditions.checkRoundingUnnecessary((rem == 0L));
      case DOWN:
        increment = false;
        break;
      case UP:
        increment = true;
        break;
      case CEILING:
        increment = (signum > 0);
        break;
      case FLOOR:
        increment = (signum < 0);
        break;
      case HALF_DOWN:
      case HALF_UP:
      case HALF_EVEN:
        absRem = Math.abs(rem);
        cmpRemToHalfDivisor = absRem - Math.abs(q) - absRem;
        if (cmpRemToHalfDivisor == 0L) {
          int i = ((mode == RoundingMode.HALF_UP) ? 1 : 0) | ((mode == RoundingMode.HALF_EVEN) ? 1 : 0) & (((div & 0x1L) != 0L) ? 1 : 0);
          break;
        } 
        increment = (cmpRemToHalfDivisor > 0L);
        break;
      default:
        throw new AssertionError();
    } 
    return increment ? (div + signum) : div;
  }
  
  @GwtIncompatible("TODO")
  public static int mod(long x, int m) {
    return (int)mod(x, m);
  }
  
  @GwtIncompatible("TODO")
  public static long mod(long x, long m) {
    if (m <= 0L)
      throw new ArithmeticException("Modulus must be positive"); 
    long result = x % m;
    return (result >= 0L) ? result : (result + m);
  }
  
  public static long gcd(long a, long b) {
    MathPreconditions.checkNonNegative("a", a);
    MathPreconditions.checkNonNegative("b", b);
    if (a == 0L)
      return b; 
    if (b == 0L)
      return a; 
    int aTwos = Long.numberOfTrailingZeros(a);
    a >>= aTwos;
    int bTwos = Long.numberOfTrailingZeros(b);
    b >>= bTwos;
    while (a != b) {
      long delta = a - b;
      long minDeltaOrZero = delta & delta >> 63L;
      a = delta - minDeltaOrZero - minDeltaOrZero;
      b += minDeltaOrZero;
      a >>= Long.numberOfTrailingZeros(a);
    } 
    return a << Math.min(aTwos, bTwos);
  }
  
  @GwtIncompatible("TODO")
  public static long checkedAdd(long a, long b) {
    long result = a + b;
    MathPreconditions.checkNoOverflow((((a ^ b) < 0L)) | (((a ^ result) >= 0L)));
    return result;
  }
  
  @GwtIncompatible("TODO")
  public static long checkedSubtract(long a, long b) {
    long result = a - b;
    MathPreconditions.checkNoOverflow((((a ^ b) >= 0L)) | (((a ^ result) >= 0L)));
    return result;
  }
  
  @GwtIncompatible("TODO")
  public static long checkedMultiply(long a, long b) {
    int leadingZeros = Long.numberOfLeadingZeros(a) + Long.numberOfLeadingZeros(a ^ 0xFFFFFFFFFFFFFFFFL) + Long.numberOfLeadingZeros(b) + Long.numberOfLeadingZeros(b ^ 0xFFFFFFFFFFFFFFFFL);
    if (leadingZeros > 65)
      return a * b; 
    MathPreconditions.checkNoOverflow((leadingZeros >= 64));
    MathPreconditions.checkNoOverflow(((a >= 0L)) | ((b != Long.MIN_VALUE)));
    long result = a * b;
    MathPreconditions.checkNoOverflow((a == 0L || result / a == b));
    return result;
  }
  
  @GwtIncompatible("TODO")
  public static long checkedPow(long b, int k) {
    MathPreconditions.checkNonNegative("exponent", k);
    if ((((b >= -2L) ? 1 : 0) & ((b <= 2L) ? 1 : 0)) != 0) {
      switch ((int)b) {
        case 0:
          return (k == 0) ? 1L : 0L;
        case 1:
          return 1L;
        case -1:
          return ((k & 0x1) == 0) ? 1L : -1L;
        case 2:
          MathPreconditions.checkNoOverflow((k < 63));
          return 1L << k;
        case -2:
          MathPreconditions.checkNoOverflow((k < 64));
          return ((k & 0x1) == 0) ? (1L << k) : (-1L << k);
      } 
      throw new AssertionError();
    } 
    long accum = 1L;
    while (true) {
      switch (k) {
        case 0:
          return accum;
        case 1:
          return checkedMultiply(accum, b);
      } 
      if ((k & 0x1) != 0)
        accum = checkedMultiply(accum, b); 
      k >>= 1;
      if (k > 0) {
        MathPreconditions.checkNoOverflow((b <= 3037000499L));
        b *= b;
      } 
    } 
  }
  
  @GwtIncompatible("TODO")
  public static long factorial(int n) {
    MathPreconditions.checkNonNegative("n", n);
    return (n < factorials.length) ? factorials[n] : Long.MAX_VALUE;
  }
  
  static final long[] factorials = new long[] { 
      1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 
      3628800L, 39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L, 
      2432902008176640000L };
  
  public static long binomial(int n, int k) {
    MathPreconditions.checkNonNegative("n", n);
    MathPreconditions.checkNonNegative("k", k);
    Preconditions.checkArgument((k <= n), "k (%s) > n (%s)", new Object[] { Integer.valueOf(k), Integer.valueOf(n) });
    if (k > n >> 1)
      k = n - k; 
    switch (k) {
      case 0:
        return 1L;
      case 1:
        return n;
    } 
    if (n < factorials.length)
      return factorials[n] / factorials[k] * factorials[n - k]; 
    if (k >= biggestBinomials.length || n > biggestBinomials[k])
      return Long.MAX_VALUE; 
    if (k < biggestSimpleBinomials.length && n <= biggestSimpleBinomials[k]) {
      long l = n--;
      for (int j = 2; j <= k; n--, j++) {
        l *= n;
        l /= j;
      } 
      return l;
    } 
    int nBits = log2(n, RoundingMode.CEILING);
    long result = 1L;
    long numerator = n--;
    long denominator = 1L;
    int numeratorBits = nBits;
    for (int i = 2; i <= k; i++, n--) {
      if (numeratorBits + nBits < 63) {
        numerator *= n;
        denominator *= i;
        numeratorBits += nBits;
      } else {
        result = multiplyFraction(result, numerator, denominator);
        numerator = n;
        denominator = i;
        numeratorBits = nBits;
      } 
    } 
    return multiplyFraction(result, numerator, denominator);
  }
  
  static long multiplyFraction(long x, long numerator, long denominator) {
    if (x == 1L)
      return numerator / denominator; 
    long commonDivisor = gcd(x, denominator);
    x /= commonDivisor;
    denominator /= commonDivisor;
    return x * numerator / denominator;
  }
  
  static final int[] biggestBinomials = new int[] { 
      Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 3810779, 121977, 16175, 4337, 1733, 887, 534, 
      361, 265, 206, 169, 143, 125, 111, 101, 94, 88, 
      83, 79, 76, 74, 72, 70, 69, 68, 67, 67, 
      66, 66, 66, 66 };
  
  @VisibleForTesting
  static final int[] biggestSimpleBinomials = new int[] { 
      Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2642246, 86251, 11724, 3218, 1313, 684, 419, 
      287, 214, 169, 139, 119, 105, 95, 87, 81, 76, 
      73, 70, 68, 66, 64, 63, 62, 62, 61, 61, 
      61 };
  
  static boolean fitsInInt(long x) {
    return ((int)x == x);
  }
  
  public static long mean(long x, long y) {
    return (x & y) + ((x ^ y) >> 1L);
  }
}
