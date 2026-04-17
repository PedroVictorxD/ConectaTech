package br.com.unp.conectatech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedVagaResponse {
    private List<VagaDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
