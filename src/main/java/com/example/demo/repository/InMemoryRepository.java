package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/** Базовый репозиторий-хранилище в памяти (коллекция). Id выдаётся сервером. */
public abstract class InMemoryRepository<T> {

    private final Map<Long, T> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    /** Возвращает копию сущности с указанным id. */
    protected abstract T withId(T entity, Long id);

    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    /** Сохраняет сущность: если id == null — выдаёт новый, иначе заменяет существующую. */
    public T save(T entity, Long id) {
        Long realId = id != null ? id : sequence.incrementAndGet();
        T saved = withId(entity, realId);
        storage.put(realId, saved);
        return saved;
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    protected List<T> findBy(Predicate<T> predicate) {
        return storage.values().stream().filter(predicate).toList();
    }
}
