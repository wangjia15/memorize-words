package com.memorizewords.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Utility class for entity-related test operations.
 * Provides helper methods for database operations in tests.
 */
@Component
public class EntityTestUtil {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Clears all data from specified entity tables.
     * Useful for test cleanup when @Transactional is not sufficient.
     */
    @Transactional
    public void clearTable(String tableName) {
        entityManager.createNativeQuery("DELETE FROM " + tableName).executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1").executeUpdate();
    }

    /**
     * Counts the number of entities in a table.
     */
    public long countEntities(String entityName) {
        String jpql = "SELECT COUNT(e) FROM " + entityName + " e";
        return (Long) entityManager.createQuery(jpql).getSingleResult();
    }

    /**
     * Finds entities by a property value.
     */
    public <T> List<T> findByProperty(Class<T> entityClass, String propertyName, Object value) {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + propertyName + " = :value";
        return entityManager.createQuery(jpql, entityClass)
                .setParameter("value", value)
                .getResultList();
    }

    /**
     * Executes a native SQL query and returns the result.
     */
    public Object executeNativeQuery(String sql) {
        return entityManager.createNativeQuery(sql).getSingleResult();
    }

    /**
     * Checks if an entity exists by ID.
     */
    public <T> boolean existsById(Class<T> entityClass, Object id) {
        return entityManager.find(entityClass, id) != null;
    }

    /**
     * Flushes the persistence context to ensure data is written to database.
     */
    public void flush() {
        entityManager.flush();
    }

    /**
     * Clears the persistence context to detach all entities.
     */
    public void clear() {
        entityManager.clear();
    }

    /**
     * Refreshes an entity from the database.
     */
    public <T> void refresh(T entity) {
        entityManager.refresh(entity);
    }

    /**
     * Creates a timestamp for testing purposes.
     */
    public static LocalDateTime createTestTimestamp() {
        return LocalDateTime.of(2023, 1, 1, 12, 0, 0);
    }

    /**
     * Creates a future timestamp for testing.
     */
    public static LocalDateTime createFutureTestTimestamp(int days) {
        return createTestTimestamp().plusDays(days);
    }

    /**
     * Creates a past timestamp for testing.
     */
    public static LocalDateTime createPastTestTimestamp(int days) {
        return createTestTimestamp().minusDays(days);
    }
}