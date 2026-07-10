package ru.senioravanti.commons.result;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;
}
