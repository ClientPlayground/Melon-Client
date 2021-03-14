package org.apache.commons.collections4.trie;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.trie.analyzer.StringKeyAnalyzer;

public class PatriciaTrie<E> extends AbstractPatriciaTrie<String, E> {
  private static final long serialVersionUID = 4446367780901817838L;
  
  public PatriciaTrie() {
    super((KeyAnalyzer<? super String>)new StringKeyAnalyzer());
  }
  
  public PatriciaTrie(Map<? extends String, ? extends E> m) {
    super((KeyAnalyzer<? super String>)new StringKeyAnalyzer(), m);
  }
}
