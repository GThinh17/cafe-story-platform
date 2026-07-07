package com.cafestory.service;

import com.cafestory.dto.responseDTO.RagTopReviewerResponseDTO;
import com.cafestory.dto.responseDTO.RagTrendingCafeResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.User;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.service.serviceImplement.RagToolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagToolServiceImplTest {

    @Mock
    private CafePageRepository cafePageRepository;
    @Mock
    private ReviewerRepository reviewerRepository;

    private RagToolServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RagToolServiceImpl(cafePageRepository, reviewerRepository);
    }

    @Test
    void trendingCafesNormalizesBlankProvinceToNull() {
        when(cafePageRepository.findRagTrendingCafePages(any(), any(Pageable.class)))
                .thenReturn(List.of());

        service.getTrendingCafes("   ", 5);

        verify(cafePageRepository).findRagTrendingCafePages(eq(null), any(Pageable.class));
    }

    @Test
    void trendingCafesMapsPublicFields() {
        CafePage page = new CafePage();
        page.setId(UUID.randomUUID());
        page.setName("The Hidden Garden");
        page.setAddress("12 Nguyen Hue");
        page.setFollowerCount(120);
        page.setLikeCount(300);
        when(cafePageRepository.findRagTrendingCafePages(any(), any(Pageable.class)))
                .thenReturn(List.of(page));

        List<RagTrendingCafeResponseDTO> result = service.getTrendingCafes("TP.HCM", 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("The Hidden Garden");
        assertThat(result.get(0).getFollowerCount()).isEqualTo(120);
    }

    @Test
    void limitIsClampedToMax() {
        when(reviewerRepository.findRagTopReviewers(any(Pageable.class))).thenReturn(List.of());

        service.getTopReviewers(9999);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewerRepository).findRagTopReviewers(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void topReviewersExcludePrivateFields() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("minh");
        user.setUserFullName("Minh Tran");
        user.setUserFollower(99);
        Reviewer reviewer = new Reviewer();
        reviewer.setReviewerId(UUID.randomUUID());
        reviewer.setUser(user);
        when(reviewerRepository.findRagTopReviewers(any(Pageable.class))).thenReturn(List.of(reviewer));

        List<RagTopReviewerResponseDTO> result = service.getTopReviewers(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("minh");
        assertThat(result.get(0).getFollowerCount()).isEqualTo(99);
    }
}
