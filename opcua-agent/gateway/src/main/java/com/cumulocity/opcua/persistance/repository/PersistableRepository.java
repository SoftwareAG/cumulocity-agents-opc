package com.cumulocity.opcua.persistance.repository;

import com.cumulocity.model.idtype.GId;
import com.cumulocity.opcua.gateway.annotations.RunWithinContext;
import com.cumulocity.opcua.gateway.model.core.HasKey;
import com.cumulocity.opcua.gateway.model.gateway.Gateway;
import com.cumulocity.opcua.gateway.repository.core.GatewayRepository;
import com.cumulocity.opcua.gateway.repository.core.Repository;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.FluentIterable.from;

@Slf4j
@RequiredArgsConstructor
public class PersistableRepository<T extends HasKey> implements Repository<T>, GatewayRepository<T> {

    private final Class<T> clazz;

    @Autowired
    private DBStore dbStore;

    @Autowired
    private PersistableTypeMetadataProvider metadataProvider;

    @Autowired
    private AutowireCapableBeanFactory spring;

    public PersistableRepository(Class<T> clazz, DBStore dbStore, PersistableTypeMetadataProvider metadataProvider) {
        this.clazz = clazz;
        this.dbStore = dbStore;
        this.metadataProvider = metadataProvider;
    }

    @Override
    @RunWithinContext
    public Optional<T> get(@NonNull Gateway gateway, @NonNull GId key) {
        return get(key);
    }

    @Override
    @RunWithinContext
    public Collection<T> findAll(@NonNull Gateway gateway) {
        return findAll();
    }

    @Override
    @RunWithinContext
    public boolean exists(@NonNull Gateway gateway, @NonNull GId value) {
        return exists(value);
    }

    @Override
    @RunWithinContext
    public T save(@NonNull Gateway gateway, @NonNull T value) {
        return save(value);
    }

    @Override
    @RunWithinContext
    public T delete(@NonNull Gateway gateway, @NonNull GId value) {
        return delete(value);
    }

    @NonNull
    @Override
    public Optional<T> get(@NonNull final GId key) {
        try {
            return fromNullable(getStore(clazz).get(getKeyAsString(key))).transform(initContext());
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            throw propagate(ex);
        }
    }

    @NonNull
    @Override
    public Collection<T> findAll() {
        try {
            final Map<String, T> store = getStore(clazz);
            return from(store.values()).transform(initContext()).toList();
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            throw propagate(ex);
        }
    }

    @Override
    public boolean exists(@NonNull final GId value) {
        try {
            final String keyAsString = getKeyAsString(value);
            return getStore(clazz).containsKey(keyAsString);
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            throw propagate(ex);
        }
    }

    @Override
    public T save(@NonNull final T value) {
        try {
            final Map<String, T> store = getStore(value.getClass());
            store.put(getKeyAsString(value.getId()), value);
            dbStore.commit();
            return value;
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            dbStore.rollback();
            throw propagate(ex);
        }
    }

    @Override
    public T delete(@NonNull final GId value) {
        try {
            final Map<String, T> store = getStore(clazz);
            final T remove = store.remove(getKeyAsString(value));
            dbStore.commit();
            return remove;
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            dbStore.rollback();
            throw propagate(ex);
        }
    }

    @Override
    public void clear() {
        try {
            getStore(clazz).clear();
        } catch (final Exception ex) {
            log.error(ex.getMessage(), ex);
            dbStore.rollback();
            throw propagate(ex);
        }
    }


    private Function<T, T> initContext() {
        return new Function<T, T>() {
            @Override
            public T apply(final T entity) {
                if (metadataProvider.isAutowire(entity.getClass())) {
                    spring.autowireBean(entity);
                }
                return entity;
            }
        };
    }

    @SneakyThrows
    private Map<String, T> getStore(Class clazz) {
        return dbStore.get(clazz);
    }

    private String getKeyAsString(T key) {
        return getKeyAsString(key.getId());
    }

    private String getKeyAsString(GId key) {
        return GId.asString(key);
    }
}
