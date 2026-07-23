package com.cafestory.integration;

import com.cafestory.entity.User;
import com.cafestory.entity.enums.TrendWindowType;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.BlogFeedRankingService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@EnabledIfEnvironmentVariable(named = "FEED_QUERY_COUNT_TEST", matches = "true")
class FeedQueryCountPostgresIntegrationTest {
    @Autowired
    private BlogFeedRankingService blogFeedRankingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DataSource dataSource;

    private Statistics statistics;

    @BeforeEach
    void requirePostgres() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName())
                    .as("Query-count phải chạy trên PostgreSQL thật")
                    .containsIgnoringCase("PostgreSQL");
        }
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @Test
    void personalizedFeed_queryCountDoesNotGrowWithPageSize_TC001() {
        List<User> activeUsers = userRepository.findByAccountStatusTrue();
        Assumptions.assumeFalse(activeUsers.isEmpty(), "Cần ít nhất một user active trong dữ liệu kiểm thử");
        User user = activeUsers.getFirst();
        UUID regionId = user.getRegion() == null ? null : user.getRegion().getRegionId();

        blogFeedRankingService.rebuildRecommendationCache(user.getUserId(), TrendWindowType.DAY_7, regionId);

        statistics.clear();
        blogFeedRankingService.getPersonalizedFeed(user.getUserId(), TrendWindowType.DAY_7, regionId, 0, 5);
        long queriesAtSizeFive = statistics.getPrepareStatementCount();

        statistics.clear();
        blogFeedRankingService.getPersonalizedFeed(user.getUserId(), TrendWindowType.DAY_7, regionId, 0, 20);
        long queriesAtSizeTwenty = statistics.getPrepareStatementCount();

        assertThat(queriesAtSizeTwenty).isLessThanOrEqualTo(queriesAtSizeFive + 2);
        assertThat(queriesAtSizeTwenty).isLessThanOrEqualTo(12);
    }
}
