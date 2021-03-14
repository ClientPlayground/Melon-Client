package org.apache.commons.collections4.sequence;

import java.util.ArrayList;
import java.util.List;

public class ReplacementsFinder<T> implements CommandVisitor<T> {
  private final List<T> pendingInsertions;
  
  private final List<T> pendingDeletions;
  
  private int skipped;
  
  private final ReplacementsHandler<T> handler;
  
  public ReplacementsFinder(ReplacementsHandler<T> handler) {
    this.pendingInsertions = new ArrayList<T>();
    this.pendingDeletions = new ArrayList<T>();
    this.skipped = 0;
    this.handler = handler;
  }
  
  public void visitInsertCommand(T object) {
    this.pendingInsertions.add(object);
  }
  
  public void visitKeepCommand(T object) {
    if (this.pendingDeletions.isEmpty() && this.pendingInsertions.isEmpty()) {
      this.skipped++;
    } else {
      this.handler.handleReplacement(this.skipped, this.pendingDeletions, this.pendingInsertions);
      this.pendingDeletions.clear();
      this.pendingInsertions.clear();
      this.skipped = 1;
    } 
  }
  
  public void visitDeleteCommand(T object) {
    this.pendingDeletions.add(object);
  }
}
