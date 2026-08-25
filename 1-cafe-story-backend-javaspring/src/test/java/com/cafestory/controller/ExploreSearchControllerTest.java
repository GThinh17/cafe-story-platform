package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ExploreSearchResponseDTO;
import com.cafestory.service.serviceInterface.ExploreSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExploreSearchControllerTest {

    @Mock
    private ExploreSearchService exploreSearchService;

    @InjectMocks
    private ExploreSearchController exploreSearchController;

    @Test
    void search_success_delegatesQueryAndSize_TC001() {
        ExploreSearchResponseDTO response = new ExploreSearchResponseDTO();
        when(exploreSearchService.search("cafe", 8)).thenReturn(response);

        ExploreSearchResponseDTO result = exploreSearchController.search("cafe", 8);

        assertThat(result).isSameAs(response);
        verify(exploreSearchService).search("cafe", 8);
    }
}
