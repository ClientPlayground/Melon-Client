package org.apache.commons.collections4.sequence;

public interface CommandVisitor<T> {
  void visitInsertCommand(T paramT);
  
  void visitKeepCommand(T paramT);
  
  void visitDeleteCommand(T paramT);
}
