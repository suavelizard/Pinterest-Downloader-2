package nl.juraji.pinterestdownloader.util;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * Created by Juraji on 8-5-2018.
 * Pinterest Downloader
 */
public class ArrayListModel<T> extends AbstractListModel<T> implements List<T> {
    private final List<T> backingList = new ArrayList<>();

    @Override
    public int getSize() {
        return backingList.size();
    }

    @Override
    public T getElementAt(int index) {
        return backingList.get(index);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean isEmpty() {
        return backingList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return backingList.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return backingList.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return backingList.toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        //noinspection SuspiciousToArrayCall
        return backingList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        final boolean b = backingList.add(t);
        if (b) {
            final int s = lastIndex();
            fireIntervalAdded(this, s, s);
        }
        return b;
    }

    @Override
    public boolean remove(Object o) {
        final int i = indexOf(o);
        fireIntervalRemoved(this, i, i);
        return backingList.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return backingList.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        final boolean b = backingList.addAll(c);
        if (b) {
            final int s = lastIndex();
            fireIntervalAdded(this, s - c.size(), s);
        }
        return b;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        final boolean b = backingList.addAll(index, c);
        if (b) {
            fireIntervalAdded(this, index, index + c.size());
        }
        return b;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        final boolean b = backingList.removeAll(c);
        if (b) {
            fireContentsChanged(this, 0, lastIndex());
        }
        return b;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        final boolean b = backingList.retainAll(c);
        if (b) {
            fireContentsChanged(this, 0, lastIndex());
        }
        return b;
    }

    @Override
    public void clear() {
        final int s = lastIndex();
        backingList.clear();
        fireIntervalRemoved(this, 0, s);
    }

    @Override
    public T get(int index) {
        return backingList.get(index);
    }

    @Override
    public T set(int index, T element) {
        final T set = backingList.set(index, element);
        if (set != null) {
            fireContentsChanged(this, index, index);
        }
        return set;
    }

    @Override
    public void add(int index, T element) {
        backingList.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    @Override
    public T remove(int index) {
        final T t = backingList.remove(index);
        if (t != null) {
            fireIntervalRemoved(this, index, index);
        }
        return t;
    }

    @Override
    public int indexOf(Object o) {
        return backingList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return backingList.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return backingList.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return backingList.listIterator(index);
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return backingList.subList(fromIndex, toIndex);
    }

    private int lastIndex() {
        int size = getSize();
        if (size > 0) {
            size = size - 1;
        }
        return size;
    }
}
