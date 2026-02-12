package ch.ethz.eyetap.service;

import org.hibernate.stat.Statistics;

import java.util.Arrays;

public class HibernateStatisticsPrinter {
    public static void print(Statistics stats) {
        System.out.println("==== Hibernate Statistics ====");
        System.out.printf("Query count: %d\n", stats.getQueryExecutionCount());
        System.out.printf("Entity fetch count: %d\n", stats.getEntityFetchCount());
        System.out.printf("Second level cache hit count: %d\n", stats.getSecondLevelCacheHitCount());
        System.out.printf("Preparation count: %d\n", stats.getPrepareStatementCount());
        System.out.println("Queries and execution counts:");

        Arrays.stream(stats.getQueries())
                .forEach(q -> System.out.printf("  %s => %d executions\n", q, stats.getQueryExecutionCount()));

        System.out.println("==== End Hibernate Stats ====");
    }
}
