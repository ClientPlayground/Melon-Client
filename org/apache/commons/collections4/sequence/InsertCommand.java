package org.apache.commons.collections4.sequence;

public class InsertCommand<T> extends EditCommand<T> {
  public InsertCommand(T object) {
    super(object);
  }
  
  public void accept(CommandVisitor<T> visitor) {
    visitor.visitInsertCommand(getObject());
  }
}
