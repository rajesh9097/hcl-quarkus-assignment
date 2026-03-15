package com.fulfilment.application.monolith.products;

import java.util.List;

public class PageResult<T> {
    public List<T> content;
    public long totalElements;
    public int totalPages;
    public int pageNumber;
    public int pageSize;

    public PageResult() {
    }

    public PageResult(List<T> content, long totalElements, int totalPages, int pageNumber, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }
}