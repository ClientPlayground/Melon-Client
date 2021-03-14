package org.apache.commons.collections4.sequence;

import java.util.List;
import org.apache.commons.collections4.Equator;
import org.apache.commons.collections4.functors.DefaultEquator;

public class SequencesComparator<T> {
  private final List<T> sequence1;
  
  private final List<T> sequence2;
  
  private final Equator<? super T> equator;
  
  private final int[] vDown;
  
  private final int[] vUp;
  
  public SequencesComparator(List<T> sequence1, List<T> sequence2) {
    this(sequence1, sequence2, (Equator<? super T>)DefaultEquator.defaultEquator());
  }
  
  public SequencesComparator(List<T> sequence1, List<T> sequence2, Equator<? super T> equator) {
    this.sequence1 = sequence1;
    this.sequence2 = sequence2;
    this.equator = equator;
    int size = sequence1.size() + sequence2.size() + 2;
    this.vDown = new int[size];
    this.vUp = new int[size];
  }
  
  public EditScript<T> getScript() {
    EditScript<T> script = new EditScript<T>();
    buildScript(0, this.sequence1.size(), 0, this.sequence2.size(), script);
    return script;
  }
  
  private Snake buildSnake(int start, int diag, int end1, int end2) {
    int end = start;
    while (end - diag < end2 && end < end1 && this.equator.equate(this.sequence1.get(end), this.sequence2.get(end - diag)))
      end++; 
    return new Snake(start, end, diag);
  }
  
  private Snake getMiddleSnake(int start1, int end1, int start2, int end2) {
    int m = end1 - start1;
    int n = end2 - start2;
    if (m == 0 || n == 0)
      return null; 
    int delta = m - n;
    int sum = n + m;
    int offset = ((sum % 2 == 0) ? sum : (sum + 1)) / 2;
    this.vDown[1 + offset] = start1;
    this.vUp[1 + offset] = end1 + 1;
    for (int d = 0; d <= offset; d++) {
      int k;
      for (k = -d; k <= d; k += 2) {
        int i = k + offset;
        if (k == -d || (k != d && this.vDown[i - 1] < this.vDown[i + 1])) {
          this.vDown[i] = this.vDown[i + 1];
        } else {
          this.vDown[i] = this.vDown[i - 1] + 1;
        } 
        int x = this.vDown[i];
        int y = x - start1 + start2 - k;
        while (x < end1 && y < end2 && this.equator.equate(this.sequence1.get(x), this.sequence2.get(y))) {
          this.vDown[i] = ++x;
          y++;
        } 
        if (delta % 2 != 0 && delta - d <= k && k <= delta + d && 
          this.vUp[i - delta] <= this.vDown[i])
          return buildSnake(this.vUp[i - delta], k + start1 - start2, end1, end2); 
      } 
      for (k = delta - d; k <= delta + d; k += 2) {
        int i = k + offset - delta;
        if (k == delta - d || (k != delta + d && this.vUp[i + 1] <= this.vUp[i - 1])) {
          this.vUp[i] = this.vUp[i + 1] - 1;
        } else {
          this.vUp[i] = this.vUp[i - 1];
        } 
        int x = this.vUp[i] - 1;
        int y = x - start1 + start2 - k;
        while (x >= start1 && y >= start2 && this.equator.equate(this.sequence1.get(x), this.sequence2.get(y))) {
          this.vUp[i] = x--;
          y--;
        } 
        if (delta % 2 == 0 && -d <= k && k <= d && 
          this.vUp[i] <= this.vDown[i + delta])
          return buildSnake(this.vUp[i], k + start1 - start2, end1, end2); 
      } 
    } 
    throw new RuntimeException("Internal Error");
  }
  
  private void buildScript(int start1, int end1, int start2, int end2, EditScript<T> script) {
    Snake middle = getMiddleSnake(start1, end1, start2, end2);
    if (middle == null || (middle.getStart() == end1 && middle.getDiag() == end1 - end2) || (middle.getEnd() == start1 && middle.getDiag() == start1 - start2)) {
      int i = start1;
      int j = start2;
      while (i < end1 || j < end2) {
        if (i < end1 && j < end2 && this.equator.equate(this.sequence1.get(i), this.sequence2.get(j))) {
          script.append(new KeepCommand<T>(this.sequence1.get(i)));
          i++;
          j++;
          continue;
        } 
        if (end1 - start1 > end2 - start2) {
          script.append(new DeleteCommand<T>(this.sequence1.get(i)));
          i++;
          continue;
        } 
        script.append(new InsertCommand<T>(this.sequence2.get(j)));
        j++;
      } 
    } else {
      buildScript(start1, middle.getStart(), start2, middle.getStart() - middle.getDiag(), script);
      for (int i = middle.getStart(); i < middle.getEnd(); i++)
        script.append(new KeepCommand<T>(this.sequence1.get(i))); 
      buildScript(middle.getEnd(), end1, middle.getEnd() - middle.getDiag(), end2, script);
    } 
  }
  
  private static class Snake {
    private final int start;
    
    private final int end;
    
    private final int diag;
    
    public Snake(int start, int end, int diag) {
      this.start = start;
      this.end = end;
      this.diag = diag;
    }
    
    public int getStart() {
      return this.start;
    }
    
    public int getEnd() {
      return this.end;
    }
    
    public int getDiag() {
      return this.diag;
    }
  }
}
