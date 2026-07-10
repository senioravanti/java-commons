package ru.senioravanti.commons.result;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

/**
 * Удобный инструмент для "переброса" проверяемых исключений
 * Не подходит для работы с ресурсами
 */
@RequiredArgsConstructor
public final class Result<T> {
    private final T value;
    private final Throwable exception;

    public static <T> Result<T> empty() {
        return new Result<>(null, null);
    }

    public static <T> Result<T> ofNullable(T value) {
        return value != null ? of(value) : empty();
    }

    public static <T> Result<T> of(T value) {
        return new Result<>(Objects.requireNonNull(value), null);
    }

    public static <T> Result<T> ofNullableException(Throwable exception) {
        return exception != null? new Result<>(null, exception) : empty();
    }

    public static <T> Result<T> ofException(Throwable exception) {
        return new Result<>(null, Objects.requireNonNull(exception));
    }

    public static <T> Result<T> from(CheckedSupplier<T> supplier) {
        try {
            return ofNullable(supplier.get());
        } catch (Throwable t) {
            return new Result<>(null, t);
        }
    }

    public static Result<Void> fromVoid(CheckedRunnable task) {
        try {
            task.run();
            return new Result<>(null, null);
        } catch (Throwable t) {
            return new Result<>(null, t);
        }
    }

    public static <E extends Throwable> Consumer<? super E> swallow() {
        return e -> {};
    }

    public T get() {
        if (value != null) return value;
        if (exception != null) sneakyThrow(exception);
        throw new NoSuchElementException("No value present");
    }

    public T orElse(T other) {
        if (value != null) return value;
        if (exception != null) sneakyThrow(exception);
        return other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        if (value != null) return value;
        if (exception != null) sneakyThrow(exception);
        return other.get();
    }

    public Stream<T> stream() {
        return value == null ? Stream.empty() : Stream.of(value);
    }

    public<U> Result<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (value == null) return new Result<>(null, exception);
        final U u;
        try {
            u = mapper.apply(value);
        } catch (Throwable exc) {
            return new Result<>(null, exc);
        }
        return ofNullable(u);
    }

    public<U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        Objects.requireNonNull(mapper);
        return value != null ? Objects.requireNonNull(mapper.apply(value)) : empty();
    }

    public Result<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (value == null) return this;
        final boolean b;
        try {
            b = predicate.test(value);
        } catch (Throwable t) {
            return ofException(t);
        }
        return b ? this : empty();
    }

    public <X extends Throwable> Result<T> recover(
        Class<? extends X> excType, Function<? super X, T> mapper)
    {
        Objects.requireNonNull(mapper);
        return excType.isInstance(exception) ? ofNullable(mapper.apply(excType.cast(exception))) : this;
    }

    public <X extends Throwable> Result<T> recover(
        Iterable<Class<? extends X>> excTypes, Function<? super X, T> mapper)
    {
        Objects.requireNonNull(mapper);
        for (Class<? extends X> excType : excTypes)
            if (excType.isInstance(exception))
                return ofNullable(mapper.apply(excType.cast(exception)));
        return this;
    }

    public <X extends Throwable> Result<T> flatRecover(
        Class<? extends X> excType, Function<? super X, Result<T>> mapper)
    {
        Objects.requireNonNull(mapper);
        return excType.isInstance(exception) ? Objects.requireNonNull(mapper.apply(excType.cast(exception))) : this;
    }

    public <X extends Throwable> Result<T> flatRecover(
        Iterable<Class<? extends X>> excTypes, Function<? super X, Result<T>> mapper)
    {
        Objects.requireNonNull(mapper);
        for (Class<? extends X> c : excTypes)
            if (c.isInstance(exception))
                return Objects.requireNonNull(mapper.apply(c.cast(exception)));
        return this;
    }

    public <E extends Throwable> Result<T> propagate(Class<E> excType) throws E {
        if (excType.isInstance(exception))
            throw excType.cast(exception);
        return this;
    }

    public <E extends Throwable> Result<T> propagate(Iterable<Class<? extends E>> excTypes) throws E {
        for (Class<? extends E> excType : excTypes)
            if (excType.isInstance(exception))
                throw excType.cast(exception);
        return this;
    }

    public <E extends Throwable, F extends Throwable> Result<T> propagate(
        Class<E> excType, Function<? super E, ? extends F> translator)
        throws F
    {
        if (excType.isInstance(exception))
            throw translator.apply(excType.cast(exception));
        return this;
    }

    public <E extends Throwable, F extends Throwable> Result<T> propagate(
        Iterable<Class<E>> excTypes, Function<? super E, ? extends F> translator)
        throws F
    {
        for (Class<? extends E> excType : excTypes)
            if (excType.isInstance(exception))
                throw translator.apply(excType.cast(exception));
        return this;
    }

    public <E extends Throwable> Result<T> handle(Class<E> excType, Consumer<? super E> action) {
        if (excType.isInstance(exception)) {
            action.accept(excType.cast(exception));
            return empty();
        }
        return this;
    }

    public <E extends Throwable> Result<T> handle(Iterable<Class<E>> excTypes, Consumer<? super E> action) {
        for (Class<? extends E> excType : excTypes)
            if (excType.isInstance(exception)) {
                action.accept(excType.cast(exception));
                return empty();
            }
        return this;
    }

    public <X extends Throwable> T orElseThrow(Function<Throwable, ? extends X> exceptionSupplier) throws X {
        if (value != null) return value;
        else throw exceptionSupplier.apply(exception);
    }

    public boolean isPresent() {
        return value != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
        if (exception != null) sneakyThrow(exception);
    }

    public boolean isException() {
        return exception != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof Result && Objects.equals(value, ((Result)obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
