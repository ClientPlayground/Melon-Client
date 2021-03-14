package org.apache.commons.cli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Option implements Cloneable, Serializable {
  private static final long serialVersionUID = 1L;
  
  public static final int UNINITIALIZED = -1;
  
  public static final int UNLIMITED_VALUES = -2;
  
  private String opt;
  
  private String longOpt;
  
  private String argName = "arg";
  
  private String description;
  
  private boolean required;
  
  private boolean optionalArg;
  
  private int numberOfArgs = -1;
  
  private Object type;
  
  private List values = new ArrayList();
  
  private char valuesep;
  
  public Option(String opt, String description) throws IllegalArgumentException {
    this(opt, null, false, description);
  }
  
  public Option(String opt, boolean hasArg, String description) throws IllegalArgumentException {
    this(opt, null, hasArg, description);
  }
  
  public Option(String opt, String longOpt, boolean hasArg, String description) throws IllegalArgumentException {
    OptionValidator.validateOption(opt);
    this.opt = opt;
    this.longOpt = longOpt;
    if (hasArg)
      this.numberOfArgs = 1; 
    this.description = description;
  }
  
  public int getId() {
    return getKey().charAt(0);
  }
  
  String getKey() {
    if (this.opt == null)
      return this.longOpt; 
    return this.opt;
  }
  
  public String getOpt() {
    return this.opt;
  }
  
  public Object getType() {
    return this.type;
  }
  
  public void setType(Object type) {
    this.type = type;
  }
  
  public String getLongOpt() {
    return this.longOpt;
  }
  
  public void setLongOpt(String longOpt) {
    this.longOpt = longOpt;
  }
  
  public void setOptionalArg(boolean optionalArg) {
    this.optionalArg = optionalArg;
  }
  
  public boolean hasOptionalArg() {
    return this.optionalArg;
  }
  
  public boolean hasLongOpt() {
    return (this.longOpt != null);
  }
  
  public boolean hasArg() {
    return (this.numberOfArgs > 0 || this.numberOfArgs == -2);
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public boolean isRequired() {
    return this.required;
  }
  
  public void setRequired(boolean required) {
    this.required = required;
  }
  
  public void setArgName(String argName) {
    this.argName = argName;
  }
  
  public String getArgName() {
    return this.argName;
  }
  
  public boolean hasArgName() {
    return (this.argName != null && this.argName.length() > 0);
  }
  
  public boolean hasArgs() {
    return (this.numberOfArgs > 1 || this.numberOfArgs == -2);
  }
  
  public void setArgs(int num) {
    this.numberOfArgs = num;
  }
  
  public void setValueSeparator(char sep) {
    this.valuesep = sep;
  }
  
  public char getValueSeparator() {
    return this.valuesep;
  }
  
  public boolean hasValueSeparator() {
    return (this.valuesep > '\000');
  }
  
  public int getArgs() {
    return this.numberOfArgs;
  }
  
  void addValueForProcessing(String value) {
    switch (this.numberOfArgs) {
      case -1:
        throw new RuntimeException("NO_ARGS_ALLOWED");
    } 
    processValue(value);
  }
  
  private void processValue(String value) {
    if (hasValueSeparator()) {
      char sep = getValueSeparator();
      int index = value.indexOf(sep);
      while (index != -1) {
        if (this.values.size() == this.numberOfArgs - 1)
          break; 
        add(value.substring(0, index));
        value = value.substring(index + 1);
        index = value.indexOf(sep);
      } 
    } 
    add(value);
  }
  
  private void add(String value) {
    if (this.numberOfArgs > 0 && this.values.size() > this.numberOfArgs - 1)
      throw new RuntimeException("Cannot add value, list full."); 
    this.values.add(value);
  }
  
  public String getValue() {
    return hasNoValues() ? null : this.values.get(0);
  }
  
  public String getValue(int index) throws IndexOutOfBoundsException {
    return hasNoValues() ? null : this.values.get(index);
  }
  
  public String getValue(String defaultValue) {
    String value = getValue();
    return (value != null) ? value : defaultValue;
  }
  
  public String[] getValues() {
    return hasNoValues() ? null : (String[])this.values.toArray((Object[])new String[this.values.size()]);
  }
  
  public List getValuesList() {
    return this.values;
  }
  
  public String toString() {
    StringBuffer buf = (new StringBuffer()).append("[ option: ");
    buf.append(this.opt);
    if (this.longOpt != null)
      buf.append(" ").append(this.longOpt); 
    buf.append(" ");
    if (hasArgs()) {
      buf.append("[ARG...]");
    } else if (hasArg()) {
      buf.append(" [ARG]");
    } 
    buf.append(" :: ").append(this.description);
    if (this.type != null)
      buf.append(" :: ").append(this.type); 
    buf.append(" ]");
    return buf.toString();
  }
  
  private boolean hasNoValues() {
    return this.values.isEmpty();
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    Option option = (Option)o;
    if ((this.opt != null) ? !this.opt.equals(option.opt) : (option.opt != null))
      return false; 
    if ((this.longOpt != null) ? !this.longOpt.equals(option.longOpt) : (option.longOpt != null))
      return false; 
    return true;
  }
  
  public int hashCode() {
    int result = (this.opt != null) ? this.opt.hashCode() : 0;
    result = 31 * result + ((this.longOpt != null) ? this.longOpt.hashCode() : 0);
    return result;
  }
  
  public Object clone() {
    try {
      Option option = (Option)super.clone();
      option.values = new ArrayList(this.values);
      return option;
    } catch (CloneNotSupportedException cnse) {
      throw new RuntimeException("A CloneNotSupportedException was thrown: " + cnse.getMessage());
    } 
  }
  
  void clearValues() {
    this.values.clear();
  }
  
  public boolean addValue(String value) {
    throw new UnsupportedOperationException("The addValue method is not intended for client use. Subclasses should use the addValueForProcessing method instead. ");
  }
}
