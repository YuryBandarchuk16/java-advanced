package ru.ifmo.rain.bandarchuk.set;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

  private final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE;

  private static final int DONT_MOVE = 0;
  private static final int MOVE_LEFT = -1;
  private static final int MOVE_RIGHT = 1;

  private final int size;
  private final List<E> elements;
  private final Comparator<? super E> comparator;
  private NavigableSet<E> descending = null;

  public ArraySet() {
    size = 0;
    elements = Collections.emptyList();
    comparator = null;
    UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = this + " is immutable";
  }

  public ArraySet(Collection<? extends E> collection) {
    this(collection, null, true);
  }

  public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
    this(collection, comparator, true);
  }

  private ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator, boolean needSort) {
    this.comparator = comparator;

    if (needSort) {
      TreeSet<E> sortedSet = new TreeSet<>(comparator);
      sortedSet.addAll(collection);
      this.elements = new ArrayList<>(sortedSet);
    } else {
      this.elements = new ArrayList<>(collection);
    }

    this.size = this.elements.size();
    UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = this + " is immutable";
  }

  private boolean isValidIndex(int index) {
    return index >= 0 && index < size;
  }

  private int targetIndex(E target) {
    return targetIndex(target, DONT_MOVE, DONT_MOVE);
  }

  private int targetIndex(E target, int addFoundEqual, int addNotFoundEqual) {
    int index = Collections.binarySearch(elements, target, comparator);

    return index >= 0 ? index + addFoundEqual : -index - 1 - addNotFoundEqual;
  }

  private E getElementByIndex(int index) {
    return isValidIndex(index) ? elements.get(index) : null;
  }

  @Override
  public E lower(E e) {
    return getElementByIndex(targetIndex(e, MOVE_LEFT, MOVE_RIGHT));
  }

  @Override
  public E floor(E e) {
    return getElementByIndex(targetIndex(e, DONT_MOVE, MOVE_RIGHT));
  }

  @Override
  public E ceiling(E e) {
    return getElementByIndex(targetIndex(e));
  }

  @Override
  public E higher(E e) {
    return getElementByIndex(targetIndex(e, MOVE_RIGHT, DONT_MOVE));
  }

  @Override
  public E pollFirst() {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  @Override
  public E pollLast() {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  @Override
  public boolean contains(Object o) {
    return isValidIndex(targetIndex((E)o));
  }

  @Override
  public Iterator<E> iterator() {
    return Collections.unmodifiableList(elements).iterator();
  }

  @Override
  public NavigableSet<E> descendingSet() {
    if (descending == null) {
      descending = new ArraySet<>(elements, Collections.reverseOrder(comparator));
    }

    return descending;
  }

  @Override
  public Iterator<E> descendingIterator() {
    return descendingSet().iterator();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  @Override
  public boolean add(E e) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
  }

  @Override
  public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
    int fromIndex = targetIndex(fromElement, fromInclusive ? DONT_MOVE : MOVE_RIGHT, DONT_MOVE);
    int toIndex = targetIndex(toElement, toInclusive ? DONT_MOVE : MOVE_LEFT, MOVE_RIGHT) + 1;

    Collection<E> sublist = elements.subList(Math.min(fromIndex, toIndex), toIndex);
    return new ArraySet<>(sublist, comparator, false);
  }

  @Override
  public NavigableSet<E> headSet(E toElement, boolean inclusive) {
    return subSet(getElementByIndex(0), true, toElement, inclusive);
  }

  @Override
  public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
    return subSet(fromElement, inclusive, getElementByIndex(size - 1), true);
  }

  @Override
  public Comparator<? super E> comparator() {
    return comparator;
  }

  @Override
  public SortedSet<E> subSet(E fromElement, E toElement) {
    return subSet(fromElement, true, toElement, false);
  }

  @Override
  public SortedSet<E> headSet(E toElement) {
    return headSet(toElement, false);
  }

  @Override
  public SortedSet<E> tailSet(E fromElement) {
    return tailSet(fromElement, true);
  }

  @Override
  public E first() {
    if (size == 0) {
      throw new NoSuchElementException("No first element in empty " + this);
    }
    return elements.get(0);
  }

  @Override
  public E last() {
    if (size == 0) {
      throw new NoSuchElementException("No last element in empty " + this);
    }
    return elements.get(size - 1);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArraySet{");
    sb.append("elements=").append(elements);
    sb.append(", comparator=").append(comparator);
    sb.append('}');
    return sb.toString();
  }
}