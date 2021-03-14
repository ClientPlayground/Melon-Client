package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.util.AbstractReferenceCounted;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.ResourceLeakDetector;
import com.github.steveice10.netty.util.ResourceLeakDetectorFactory;
import com.github.steveice10.netty.util.ResourceLeakTracker;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDnsMessage extends AbstractReferenceCounted implements DnsMessage {
  private static final ResourceLeakDetector<DnsMessage> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(DnsMessage.class);
  
  private static final int SECTION_QUESTION = DnsSection.QUESTION.ordinal();
  
  private static final int SECTION_COUNT = 4;
  
  private final ResourceLeakTracker<DnsMessage> leak = leakDetector.track(this);
  
  private short id;
  
  private DnsOpCode opCode;
  
  private boolean recursionDesired;
  
  private byte z;
  
  private Object questions;
  
  private Object answers;
  
  private Object authorities;
  
  private Object additionals;
  
  protected AbstractDnsMessage(int id) {
    this(id, DnsOpCode.QUERY);
  }
  
  protected AbstractDnsMessage(int id, DnsOpCode opCode) {
    setId(id);
    setOpCode(opCode);
  }
  
  public int id() {
    return this.id & 0xFFFF;
  }
  
  public DnsMessage setId(int id) {
    this.id = (short)id;
    return this;
  }
  
  public DnsOpCode opCode() {
    return this.opCode;
  }
  
  public DnsMessage setOpCode(DnsOpCode opCode) {
    this.opCode = (DnsOpCode)ObjectUtil.checkNotNull(opCode, "opCode");
    return this;
  }
  
  public boolean isRecursionDesired() {
    return this.recursionDesired;
  }
  
  public DnsMessage setRecursionDesired(boolean recursionDesired) {
    this.recursionDesired = recursionDesired;
    return this;
  }
  
  public int z() {
    return this.z;
  }
  
  public DnsMessage setZ(int z) {
    this.z = (byte)(z & 0x7);
    return this;
  }
  
  public int count(DnsSection section) {
    return count(sectionOrdinal(section));
  }
  
  private int count(int section) {
    Object records = sectionAt(section);
    if (records == null)
      return 0; 
    if (records instanceof DnsRecord)
      return 1; 
    List<DnsRecord> recordList = (List<DnsRecord>)records;
    return recordList.size();
  }
  
  public int count() {
    int count = 0;
    for (int i = 0; i < 4; i++)
      count += count(i); 
    return count;
  }
  
  public <T extends DnsRecord> T recordAt(DnsSection section) {
    return recordAt(sectionOrdinal(section));
  }
  
  private <T extends DnsRecord> T recordAt(int section) {
    Object records = sectionAt(section);
    if (records == null)
      return null; 
    if (records instanceof DnsRecord)
      return castRecord(records); 
    List<DnsRecord> recordList = (List<DnsRecord>)records;
    if (recordList.isEmpty())
      return null; 
    return castRecord(recordList.get(0));
  }
  
  public <T extends DnsRecord> T recordAt(DnsSection section, int index) {
    return recordAt(sectionOrdinal(section), index);
  }
  
  private <T extends DnsRecord> T recordAt(int section, int index) {
    Object records = sectionAt(section);
    if (records == null)
      throw new IndexOutOfBoundsException("index: " + index + " (expected: none)"); 
    if (records instanceof DnsRecord) {
      if (index == 0)
        return castRecord(records); 
      throw new IndexOutOfBoundsException("index: " + index + "' (expected: 0)");
    } 
    List<DnsRecord> recordList = (List<DnsRecord>)records;
    return castRecord(recordList.get(index));
  }
  
  public DnsMessage setRecord(DnsSection section, DnsRecord record) {
    setRecord(sectionOrdinal(section), record);
    return this;
  }
  
  private void setRecord(int section, DnsRecord record) {
    clear(section);
    setSection(section, checkQuestion(section, record));
  }
  
  public <T extends DnsRecord> T setRecord(DnsSection section, int index, DnsRecord record) {
    return setRecord(sectionOrdinal(section), index, record);
  }
  
  private <T extends DnsRecord> T setRecord(int section, int index, DnsRecord record) {
    checkQuestion(section, record);
    Object records = sectionAt(section);
    if (records == null)
      throw new IndexOutOfBoundsException("index: " + index + " (expected: none)"); 
    if (records instanceof DnsRecord) {
      if (index == 0) {
        setSection(section, record);
        return castRecord(records);
      } 
      throw new IndexOutOfBoundsException("index: " + index + " (expected: 0)");
    } 
    List<DnsRecord> recordList = (List<DnsRecord>)records;
    return castRecord(recordList.set(index, record));
  }
  
  public DnsMessage addRecord(DnsSection section, DnsRecord record) {
    addRecord(sectionOrdinal(section), record);
    return this;
  }
  
  private void addRecord(int section, DnsRecord record) {
    checkQuestion(section, record);
    Object records = sectionAt(section);
    if (records == null) {
      setSection(section, record);
      return;
    } 
    if (records instanceof DnsRecord) {
      List<DnsRecord> list = newRecordList();
      list.add(castRecord(records));
      list.add(record);
      setSection(section, list);
      return;
    } 
    List<DnsRecord> recordList = (List<DnsRecord>)records;
    recordList.add(record);
  }
  
  public DnsMessage addRecord(DnsSection section, int index, DnsRecord record) {
    addRecord(sectionOrdinal(section), index, record);
    return this;
  }
  
  private void addRecord(int section, int index, DnsRecord record) {
    checkQuestion(section, record);
    Object records = sectionAt(section);
    if (records == null) {
      if (index != 0)
        throw new IndexOutOfBoundsException("index: " + index + " (expected: 0)"); 
      setSection(section, record);
      return;
    } 
    if (records instanceof DnsRecord) {
      List<DnsRecord> list;
      if (index == 0) {
        list = newRecordList();
        list.add(record);
        list.add(castRecord(records));
      } else if (index == 1) {
        list = newRecordList();
        list.add(castRecord(records));
        list.add(record);
      } else {
        throw new IndexOutOfBoundsException("index: " + index + " (expected: 0 or 1)");
      } 
      setSection(section, list);
      return;
    } 
    List<DnsRecord> recordList = (List<DnsRecord>)records;
    recordList.add(index, record);
  }
  
  public <T extends DnsRecord> T removeRecord(DnsSection section, int index) {
    return removeRecord(sectionOrdinal(section), index);
  }
  
  private <T extends DnsRecord> T removeRecord(int section, int index) {
    Object records = sectionAt(section);
    if (records == null)
      throw new IndexOutOfBoundsException("index: " + index + " (expected: none)"); 
    if (records instanceof DnsRecord) {
      if (index != 0)
        throw new IndexOutOfBoundsException("index: " + index + " (expected: 0)"); 
      T record = castRecord(records);
      setSection(section, null);
      return record;
    } 
    List<DnsRecord> recordList = (List<DnsRecord>)records;
    return castRecord(recordList.remove(index));
  }
  
  public DnsMessage clear(DnsSection section) {
    clear(sectionOrdinal(section));
    return this;
  }
  
  public DnsMessage clear() {
    for (int i = 0; i < 4; i++)
      clear(i); 
    return this;
  }
  
  private void clear(int section) {
    Object recordOrList = sectionAt(section);
    setSection(section, null);
    if (recordOrList instanceof ReferenceCounted) {
      ((ReferenceCounted)recordOrList).release();
    } else if (recordOrList instanceof List) {
      List<DnsRecord> list = (List<DnsRecord>)recordOrList;
      if (!list.isEmpty())
        for (DnsRecord r : list)
          ReferenceCountUtil.release(r);  
    } 
  }
  
  public DnsMessage touch() {
    return (DnsMessage)super.touch();
  }
  
  public DnsMessage touch(Object hint) {
    if (this.leak != null)
      this.leak.record(hint); 
    return this;
  }
  
  public DnsMessage retain() {
    return (DnsMessage)super.retain();
  }
  
  public DnsMessage retain(int increment) {
    return (DnsMessage)super.retain(increment);
  }
  
  protected void deallocate() {
    clear();
    ResourceLeakTracker<DnsMessage> leak = this.leak;
    if (leak != null) {
      boolean closed = leak.close(this);
      assert closed;
    } 
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof DnsMessage))
      return false; 
    DnsMessage that = (DnsMessage)obj;
    if (id() != that.id())
      return false; 
    if (this instanceof DnsQuery) {
      if (!(that instanceof DnsQuery))
        return false; 
    } else if (that instanceof DnsQuery) {
      return false;
    } 
    return true;
  }
  
  public int hashCode() {
    return id() * 31 + ((this instanceof DnsQuery) ? 0 : 1);
  }
  
  private Object sectionAt(int section) {
    switch (section) {
      case 0:
        return this.questions;
      case 1:
        return this.answers;
      case 2:
        return this.authorities;
      case 3:
        return this.additionals;
    } 
    throw new Error();
  }
  
  private void setSection(int section, Object value) {
    switch (section) {
      case 0:
        this.questions = value;
        return;
      case 1:
        this.answers = value;
        return;
      case 2:
        this.authorities = value;
        return;
      case 3:
        this.additionals = value;
        return;
    } 
    throw new Error();
  }
  
  private static int sectionOrdinal(DnsSection section) {
    return ((DnsSection)ObjectUtil.checkNotNull(section, "section")).ordinal();
  }
  
  private static DnsRecord checkQuestion(int section, DnsRecord record) {
    if (section == SECTION_QUESTION && !(ObjectUtil.checkNotNull(record, "record") instanceof DnsQuestion))
      throw new IllegalArgumentException("record: " + record + " (expected: " + 
          StringUtil.simpleClassName(DnsQuestion.class) + ')'); 
    return record;
  }
  
  private static <T extends DnsRecord> T castRecord(Object record) {
    return (T)record;
  }
  
  private static ArrayList<DnsRecord> newRecordList() {
    return new ArrayList<DnsRecord>(2);
  }
}
