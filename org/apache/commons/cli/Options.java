package org.apache.commons.cli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Options implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private Map shortOpts = new HashMap();
  
  private Map longOpts = new HashMap();
  
  private List requiredOpts = new ArrayList();
  
  private Map optionGroups = new HashMap();
  
  public Options addOptionGroup(OptionGroup group) {
    Iterator options = group.getOptions().iterator();
    if (group.isRequired())
      this.requiredOpts.add(group); 
    while (options.hasNext()) {
      Option option = options.next();
      option.setRequired(false);
      addOption(option);
      this.optionGroups.put(option.getKey(), group);
    } 
    return this;
  }
  
  Collection getOptionGroups() {
    return new HashSet(this.optionGroups.values());
  }
  
  public Options addOption(String opt, boolean hasArg, String description) {
    addOption(opt, null, hasArg, description);
    return this;
  }
  
  public Options addOption(String opt, String longOpt, boolean hasArg, String description) {
    addOption(new Option(opt, longOpt, hasArg, description));
    return this;
  }
  
  public Options addOption(Option opt) {
    String key = opt.getKey();
    if (opt.hasLongOpt())
      this.longOpts.put(opt.getLongOpt(), opt); 
    if (opt.isRequired()) {
      if (this.requiredOpts.contains(key))
        this.requiredOpts.remove(this.requiredOpts.indexOf(key)); 
      this.requiredOpts.add(key);
    } 
    this.shortOpts.put(key, opt);
    return this;
  }
  
  public Collection getOptions() {
    return Collections.unmodifiableCollection(helpOptions());
  }
  
  List helpOptions() {
    return new ArrayList(this.shortOpts.values());
  }
  
  public List getRequiredOptions() {
    return this.requiredOpts;
  }
  
  public Option getOption(String opt) {
    opt = Util.stripLeadingHyphens(opt);
    if (this.shortOpts.containsKey(opt))
      return (Option)this.shortOpts.get(opt); 
    return (Option)this.longOpts.get(opt);
  }
  
  public boolean hasOption(String opt) {
    opt = Util.stripLeadingHyphens(opt);
    return (this.shortOpts.containsKey(opt) || this.longOpts.containsKey(opt));
  }
  
  public OptionGroup getOptionGroup(Option opt) {
    return (OptionGroup)this.optionGroups.get(opt.getKey());
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[ Options: [ short ");
    buf.append(this.shortOpts.toString());
    buf.append(" ] [ long ");
    buf.append(this.longOpts);
    buf.append(" ]");
    return buf.toString();
  }
}
