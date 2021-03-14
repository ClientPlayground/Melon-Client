package com.google.common.cache;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;

@Beta
@GwtCompatible
public final class CacheStats {
  private final long hitCount;
  
  private final long missCount;
  
  private final long loadSuccessCount;
  
  private final long loadExceptionCount;
  
  private final long totalLoadTime;
  
  private final long evictionCount;
  
  public CacheStats(long hitCount, long missCount, long loadSuccessCount, long loadExceptionCount, long totalLoadTime, long evictionCount) {
    Preconditions.checkArgument((hitCount >= 0L));
    Preconditions.checkArgument((missCount >= 0L));
    Preconditions.checkArgument((loadSuccessCount >= 0L));
    Preconditions.checkArgument((loadExceptionCount >= 0L));
    Preconditions.checkArgument((totalLoadTime >= 0L));
    Preconditions.checkArgument((evictionCount >= 0L));
    this.hitCount = hitCount;
    this.missCount = missCount;
    this.loadSuccessCount = loadSuccessCount;
    this.loadExceptionCount = loadExceptionCount;
    this.totalLoadTime = totalLoadTime;
    this.evictionCount = evictionCount;
  }
  
  public long requestCount() {
    return this.hitCount + this.missCount;
  }
  
  public long hitCount() {
    return this.hitCount;
  }
  
  public double hitRate() {
    long requestCount = requestCount();
    return (requestCount == 0L) ? 1.0D : (this.hitCount / requestCount);
  }
  
  public long missCount() {
    return this.missCount;
  }
  
  public double missRate() {
    long requestCount = requestCount();
    return (requestCount == 0L) ? 0.0D : (this.missCount / requestCount);
  }
  
  public long loadCount() {
    return this.loadSuccessCount + this.loadExceptionCount;
  }
  
  public long loadSuccessCount() {
    return this.loadSuccessCount;
  }
  
  public long loadExceptionCount() {
    return this.loadExceptionCount;
  }
  
  public double loadExceptionRate() {
    long totalLoadCount = this.loadSuccessCount + this.loadExceptionCount;
    return (totalLoadCount == 0L) ? 0.0D : (this.loadExceptionCount / totalLoadCount);
  }
  
  public long totalLoadTime() {
    return this.totalLoadTime;
  }
  
  public double averageLoadPenalty() {
    long totalLoadCount = this.loadSuccessCount + this.loadExceptionCount;
    return (totalLoadCount == 0L) ? 0.0D : (this.totalLoadTime / totalLoadCount);
  }
  
  public long evictionCount() {
    return this.evictionCount;
  }
  
  public CacheStats minus(CacheStats other) {
    return new CacheStats(Math.max(0L, this.hitCount - other.hitCount), Math.max(0L, this.missCount - other.missCount), Math.max(0L, this.loadSuccessCount - other.loadSuccessCount), Math.max(0L, this.loadExceptionCount - other.loadExceptionCount), Math.max(0L, this.totalLoadTime - other.totalLoadTime), Math.max(0L, this.evictionCount - other.evictionCount));
  }
  
  public CacheStats plus(CacheStats other) {
    return new CacheStats(this.hitCount + other.hitCount, this.missCount + other.missCount, this.loadSuccessCount + other.loadSuccessCount, this.loadExceptionCount + other.loadExceptionCount, this.totalLoadTime + other.totalLoadTime, this.evictionCount + other.evictionCount);
  }
  
  public int hashCode() {
    return Objects.hashCode(new Object[] { Long.valueOf(this.hitCount), Long.valueOf(this.missCount), Long.valueOf(this.loadSuccessCount), Long.valueOf(this.loadExceptionCount), Long.valueOf(this.totalLoadTime), Long.valueOf(this.evictionCount) });
  }
  
  public boolean equals(@Nullable Object object) {
    if (object instanceof CacheStats) {
      CacheStats other = (CacheStats)object;
      return (this.hitCount == other.hitCount && this.missCount == other.missCount && this.loadSuccessCount == other.loadSuccessCount && this.loadExceptionCount == other.loadExceptionCount && this.totalLoadTime == other.totalLoadTime && this.evictionCount == other.evictionCount);
    } 
    return false;
  }
  
  public String toString() {
    return Objects.toStringHelper(this).add("hitCount", this.hitCount).add("missCount", this.missCount).add("loadSuccessCount", this.loadSuccessCount).add("loadExceptionCount", this.loadExceptionCount).add("totalLoadTime", this.totalLoadTime).add("evictionCount", this.evictionCount).toString();
  }
}
