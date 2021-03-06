package com.tiro.entities;

import com.tiro.schema.BaseColumns;
import com.tiro.schema.DbEntity;

import javax.annotation.Nonnull;
import javax.persistence.*;

/** Base entity with tracking of version. */
@MappedSuperclass
@EntityListeners(BaseEntity.Timestamps.class)
@SuppressWarnings({"unused"})
public abstract class BaseEntity implements BaseColumns, DbEntity {
  /** Abstract class serialization ID. */
  private static final long serialVersionUID = -2514165106826894512L;

  /** Version of the Entity, on each modification version automatically increased. */
  @Version
  @Column(name = VERSION) private long version;
  /** Timestamp, when instance was created. Nanos. */
  @Column(name = C_TIME) private long createdAt;
  /** Timestamp of the last update operation. Nanos. */
  @Column(name = U_TIME) private long updatedAt;
  /** Timestamp of the deleteAt operation. Nanos. */
  @Column(name = D_TIME) private long deletedAt;

  /* package */ BaseEntity() {
    this.createdAt = System.nanoTime();
  }

  public long getVersion() {
    return this.version;
  }

  public long getTimeCreated() {
    return createdAt;
  }

  protected void setTimeCreated(final long timeCreated) {
    this.createdAt = timeCreated;
  }

  public long getTimeUpdated() {
    return updatedAt;
  }

  protected void setTimeUpdated(final long timeUpdated) {
    this.updatedAt = timeUpdated;
  }

  public long getTimeDeleted() {
    return deletedAt;
  }

  protected void setTimeDeleted(final long timeDeleted) {
    this.deletedAt = timeDeleted;
  }

  @Override
  public String toString() {
    return ", i: { version=" + version
        + ", createdAt=" + createdAt
        + ", updatedAt=" + updatedAt
        + ", deletedAt=" + deletedAt
        + " }";
  }

  /** Utility class that updates timestamps of the entities during the Update/Delete operations. */
  public static class Timestamps {

    @PrePersist
    protected void onPrePersist(@Nonnull final BaseEntity entity) {
      newAt(entity);
    }

    @PreUpdate
    protected void onPreUpdate(@Nonnull final BaseEntity entity) {
      touchAt(entity);
    }

    @PreRemove
    protected void onPreRemove(@Nonnull final BaseEntity entity) {
      deleteAt(entity);
    }

    /** refresh the {@link #createdAt} of the entity. */
    @Nonnull
    public static <T extends BaseEntity> T newAt(@Nonnull final T entity) {
      entity.setTimeCreated(System.nanoTime());
      return entity;
    }

    /** refresh the {@link #updatedAt} of the entity. */
    @Nonnull
    public static <T extends BaseEntity> T touchAt(@Nonnull final T entity) {
      entity.setTimeUpdated(System.nanoTime());
      return entity;
    }

    /** refresh the {@link #deletedAt} of the entity. */
    @Nonnull
    public static <T extends BaseEntity> T deleteAt(@Nonnull final T entity) {
      entity.setTimeDeleted(System.nanoTime());
      return entity;
    }
  }
}
