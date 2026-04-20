package org.commonplot.backend;

import java.util.List;

// ============================================================
//  PagedResponse.java — wrapper pentru răspunsuri paginate
//  Returnează datele + metadata de paginare
// ============================================================
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PagedResponse(List<T> content, int page, int pageSize, long totalElements) {
        this.content       = content;
        this.page          = page;
        this.pageSize      = pageSize;
        this.totalElements = totalElements;
        this.totalPages    = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        this.first         = page == 0;
        this.last          = page >= this.totalPages - 1;
    }

    public List<T> getContent()        { return content; }
    public int getPage()               { return page; }
    public int getPageSize()           { return pageSize; }
    public long getTotalElements()     { return totalElements; }
    public int getTotalPages()         { return totalPages; }
    public boolean isFirst()           { return first; }
    public boolean isLast()            { return last; }
}