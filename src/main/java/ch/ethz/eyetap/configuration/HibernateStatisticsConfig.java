package ch.ethz.eyetap.configuration;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@RequiredArgsConstructor
public class HibernateStatisticsConfig {

    private final EntityManagerFactory entityManagerFactory;

    @Bean(name = "hibernateStatistics")
    public Statistics hibernateStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        return stats;
    }
}
