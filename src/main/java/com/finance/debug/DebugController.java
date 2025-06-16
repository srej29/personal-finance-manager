package com.finance.debug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private DataSource dataSource;

    @GetMapping("/authstatus")
    public ResponseEntity<String> getAuthStatus() {
        return ResponseEntity.ok("Debug endpoint accessible - " + LocalDateTime.now());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        try {
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("message", "Application is running");

            // Check if application context is available
            if (applicationContext != null) {
                health.put("applicationContext", "Available");
                health.put("beanCount", applicationContext.getBeanDefinitionCount());
            } else {
                health.put("applicationContext", "Not Available");
            }

            // Check if datasource is available
            if (dataSource != null) {
                health.put("dataSource", "Available");
                try {
                    var connection = dataSource.getConnection();
                    health.put("databaseConnection", "Working");
                    connection.close();
                } catch (Exception e) {
                    health.put("databaseConnection", "Failed: " + e.getMessage());
                }
            } else {
                health.put("dataSource", "Not Available");
            }

        } catch (Exception e) {
            health.put("error", e.getMessage());
            health.put("stackTrace", e.getClass().getSimpleName());
        }
        return ResponseEntity.ok(health);
    }

    @GetMapping("/beans")
    public ResponseEntity<Map<String, Object>> getBeans() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (applicationContext != null) {
                String[] beanNames = applicationContext.getBeanDefinitionNames();
                result.put("totalBeans", beanNames.length);

                // Look for our specific beans
                Map<String, Boolean> ourBeans = new HashMap<>();
                ourBeans.put("userRepository", applicationContext.containsBean("userRepository"));
                ourBeans.put("categoryRepository", applicationContext.containsBean("categoryRepository"));
                ourBeans.put("transactionRepository", applicationContext.containsBean("transactionRepository"));
                ourBeans.put("goalRepository", applicationContext.containsBean("goalRepository"));
                ourBeans.put("userService", applicationContext.containsBean("userService"));
                ourBeans.put("transactionService", applicationContext.containsBean("transactionService"));

                result.put("ourBeans", ourBeans);

                // List some bean names
                result.put("sampleBeans", java.util.Arrays.asList(beanNames).subList(0, Math.min(10, beanNames.length)));
            } else {
                result.put("error", "Application context not available");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-json")
    public ResponseEntity<Map<String, Object>> testJson(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", body);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "JSON parsing works");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/database-simple")
    public ResponseEntity<Map<String, Object>> testDatabaseSimple() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (dataSource != null) {
                var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.TABLES");

                if (resultSet.next()) {
                    result.put("tableCount", resultSet.getInt("count"));
                }

                resultSet.close();
                statement.close();
                connection.close();

                result.put("status", "Database query successful");
            } else {
                result.put("error", "DataSource not available");
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        return ResponseEntity.ok(result);
    }
}