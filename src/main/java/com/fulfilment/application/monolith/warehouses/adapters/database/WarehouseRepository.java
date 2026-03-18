package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    getEntityManager().createQuery(
      "UPDATE DbWarehouse w SET w.location = :loc, w.capacity = :cap, " +
      "w.stock = :stock, w.archivedAt = :archived WHERE w.businessUnitCode = :code")
      .setParameter("loc", warehouse.location)
      .setParameter("cap", warehouse.capacity)
      .setParameter("stock", warehouse.stock)
      .setParameter("archived", warehouse.archivedAt)
      .setParameter("code", warehouse.businessUnitCode)
      .executeUpdate();

    // Clear persistence context to see updates in subsequent queries
    getEntityManager().flush();
    getEntityManager().clear();
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
      DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
      if (dbWarehouse != null) {
          delete(dbWarehouse);
      } else {
          throw new UnsupportedOperationException("Unimplemented method 'remove'");
      }

  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  public List<Warehouse> searchWarehouses(
          String location,
          Integer minCapacity,
          Integer maxCapacity,
          String sortField,
          String sortDirection,
          int pageNumber,
          int pageSize) {

      // Build the query dynamically
      StringBuilder queryBuilder = new StringBuilder("1=1");
      Map<String, Object> parameters = new HashMap<>();

      if (location != null && !location.isEmpty()) {
          queryBuilder.append(" AND location LIKE :location");
          parameters.put("location", "%" + location + "%");
      }
      if (minCapacity != null) {
          queryBuilder.append(" AND capacity >= :minCapacity");
          parameters.put("minCapacity", minCapacity);
      }
      if (maxCapacity != null) {
          queryBuilder.append(" AND capacity <= :maxCapacity");
          parameters.put("maxCapacity", maxCapacity);
      }

      // Create the query
      PanacheQuery<DbWarehouse> query = find(queryBuilder.toString(), parameters);

    // Apply sorting
    if (sortField != null && sortDirection != null) {
      queryBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection);
    }

      // Apply pagination
      query = query.page(Page.of(pageNumber, pageSize));

      // Map results to domain model
      return query.list().stream()
              .map(DbWarehouse::toWarehouse)
              .toList();
  }

}
