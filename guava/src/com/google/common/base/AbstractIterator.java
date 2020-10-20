/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.base;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Note this class is a copy of {@link com.google.common.collect.AbstractIterator} (for dependency
 * reasons).
 */
@GwtCompatible
abstract class AbstractIterator<T extends @Nullable Object> implements Iterator<T> {
  private State state = State.NOT_READY;

  protected AbstractIterator() {}

  private enum State {
    READY,
    NOT_READY,
    DONE,
    FAILED,
  }

  private @Nullable T next;

  protected abstract T computeNext();

  @CanIgnoreReturnValue
  protected final T endOfData() {
    state = State.DONE;
    // endOfData's return type is a lie. For discussion, see collect.AbstractIterator.
    return unsafeNull();
  }

  @SuppressWarnings("nullness")
  private static <T extends @Nullable Object> T unsafeNull() {
    return null;
  }

  @Override
  public final boolean hasNext() {
    checkState(state != State.FAILED);
    switch (state) {
      case DONE:
        return false;
      case READY:
        return true;
      default:
    }
    return tryToComputeNext();
  }

  private boolean tryToComputeNext() {
    state = State.FAILED; // temporary pessimism
    next = computeNext();
    if (state != State.DONE) {
      state = State.READY;
      return true;
    }
    return false;
  }

  @Override
  public final T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    state = State.NOT_READY;
    // Guaranteed to be safe by the hasNext() check:
    T result = uncheckedCastNullableTToT(next);
    next = null;
    return result;
  }

  @Override
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("nullness")
  private static <T extends @Nullable Object> T uncheckedCastNullableTToT(@Nullable T next) {
    /*
     * We can't use requireNonNull because `next` might be null. Specifically, it can be null
     * because the iterator might contain a null element to be returned to the user. This is in
     * contrast to the other way for `next` to be null, which is for the iterator not to have a next
     * value computed yet.
     */
    return next;
  }
}