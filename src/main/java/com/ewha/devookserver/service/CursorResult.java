package com.ewha.devookserver.service;

import java.util.List;

public class CursorResult<T> {
  private List<T> values;
  private Boolean hasNext;

  public CursorResult(List<T> values, Boolean hasNext) {
    this.values = values;
    this.hasNext = hasNext;
  }

  public List<T> getValues() {
    return values;
  }

  public void setValues(List<T> values) {
    this.values = values;
  }

  public Boolean getHasNext() {
    return hasNext;
  }

  public void setHasNext(Boolean hasNext) {
    this.hasNext = hasNext;
  }
}
