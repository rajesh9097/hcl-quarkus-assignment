package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductRepository productRepository;

  private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());

  @GET
  public List<Product> get() {
    return productRepository.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @GET
  @Path("filter")
  public PageResult<Product> page(
          @QueryParam("page") @DefaultValue("0") int page,
          @QueryParam("size") @DefaultValue("10") int size,
          @QueryParam("name") String name,
          @QueryParam("minPrice") Double minPrice,
          @QueryParam("maxPrice") Double maxPrice,
          @QueryParam("minStock") Integer minStock,
          @QueryParam("maxStock") Integer maxStock) {

    StringBuilder q = new StringBuilder();
    Map<String, Object> params = new HashMap<>();

    if (name != null && !name.isBlank()) {
      q.append("name like :name");
      params.put("name", "%" + name + "%");
    }
    if (minPrice != null) {
      if (q.length() > 0) q.append(" and ");
      q.append("price >= :minPrice");
      params.put("minPrice", minPrice);
    }
    if (maxPrice != null) {
      if (q.length() > 0) q.append(" and ");
      q.append("price <= :maxPrice");
      params.put("maxPrice", maxPrice);
    }
    if (minStock != null) {
      if (q.length() > 0) q.append(" and ");
      q.append("stock >= :minStock");
      params.put("minStock", minStock);
    }
    if (maxStock != null) {
      if (q.length() > 0) q.append(" and ");
      q.append("stock <= :maxStock");
      params.put("maxStock", maxStock);
    }

    PanacheQuery<Product> pq;
    if (q.length() == 0) {
      pq = productRepository.findAll(Sort.by("name"));
    } else {
      // append order by name for consistent ordering
      String queryWithOrder = q.toString() + " order by name";
      pq = productRepository.find(queryWithOrder, params);
    }

    long total = pq.count();
    // apply pagination using Panache Page
    pq.page(io.quarkus.panache.common.Page.of(page, size));
    java.util.List<Product> items = pq.list();

    int totalPages = size > 0 ? (int) ((total + size - 1) / size) : 0;
    return new PageResult<>(items, total, totalPages, page, size);
  }

  @POST
  @Transactional
  public Response create(Product product) {
    if (product.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    productRepository.persist(product);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Product update(Long id, Product product) {
    if (product.name == null) {
      throw new WebApplicationException("Product Name was not set on request.", 422);
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    productRepository.persist(entity);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    productRepository.delete(entity);
    return Response.status(204).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
