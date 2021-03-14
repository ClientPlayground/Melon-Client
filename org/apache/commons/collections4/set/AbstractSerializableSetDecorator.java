package org.apache.commons.collections4.set;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

public abstract class AbstractSerializableSetDecorator<E> extends AbstractSetDecorator<E> {
  private static final long serialVersionUID = 1229469966212206107L;
  
  protected AbstractSerializableSetDecorator(Set<E> set) {
    super(set);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(decorated());
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    setCollection((Collection)in.readObject());
  }
}
