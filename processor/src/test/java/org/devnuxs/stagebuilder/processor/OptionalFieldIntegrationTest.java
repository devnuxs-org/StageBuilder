package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Integration test demonstrating the full functionality of the optional field feature.
 */
public class OptionalFieldIntegrationTest {

    @Test
    public void testRealWorldExample() {
        // Test a real-world example with mixed required and optional fields
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.UserProfile", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class UserProfile {
                        private final String username;
                        private final String email;
                        private final String displayName;
                        private final String bio;
                        private final String avatarUrl;
                        private final boolean isPublic;
                        
                        public UserProfile(
                            String username, 
                            String email, 
                            @StageBuilder.Optional String displayName,
                            @StageBuilder.Optional String bio,
                            @StageBuilder.Optional String avatarUrl,
                            @StageBuilder.Optional boolean isPublic
                        ) {
                            this.username = username;
                            this.email = email;
                            this.displayName = displayName;
                            this.bio = bio;
                            this.avatarUrl = avatarUrl;
                            this.isPublic = isPublic;
                        }
                        
                        public String getUsername() { return username; }
                        public String getEmail() { return email; }
                        public String getDisplayName() { return displayName; }
                        public String getBio() { return bio; }
                        public String getAvatarUrl() { return avatarUrl; }
                        public boolean isPublic() { return isPublic; }
                    }
                    """),
                JavaFileObjects.forSourceString("test.UserProfileService", """
                    package test;
                    
                    public class UserProfileService {
                        
                        // Minimal user profile - only required fields
                        public static UserProfile createMinimalProfile(String username, String email) {
                            return UserProfileStageBuilder.builder()
                                .username(username)
                                .email(email)
                                .build();
                        }
                        
                        // User profile with display name
                        public static UserProfile createProfileWithDisplayName(String username, String email, String displayName) {
                            return UserProfileStageBuilder.builder()
                                .username(username)
                                .email(email)
                                .displayName(displayName)
                                .build();
                        }
                        
                        // Full user profile with all fields
                        public static UserProfile createFullProfile(
                            String username, 
                            String email, 
                            String displayName, 
                            String bio, 
                            String avatarUrl, 
                            boolean isPublic
                        ) {
                            return UserProfileStageBuilder.builder()
                                .username(username)
                                .email(email)
                                .displayName(displayName)
                                .bio(bio)
                                .avatarUrl(avatarUrl)
                                .isPublic(isPublic)
                                .build();
                        }
                        
                        // Profile with optional fields in different order
                        public static UserProfile createProfileCustomOrder(
                            String username, 
                            String email, 
                            String avatarUrl, 
                            boolean isPublic
                        ) {
                            return UserProfileStageBuilder.builder()
                                .username(username)
                                .email(email)
                                .isPublic(isPublic)
                                .avatarUrl(avatarUrl)
                                .build();
                        }
                        
                        // Profile with some optional fields
                        public static UserProfile createProfilePartial(
                            String username, 
                            String email, 
                            String bio
                        ) {
                            return UserProfileStageBuilder.builder()
                                .username(username)
                                .email(email)
                                .bio(bio)
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testRecordExample() {
        // Test optional fields with records
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Product", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record Product(
                        String id, 
                        String name, 
                        double price,
                        @StageBuilder.Optional String description,
                        @StageBuilder.Optional String category,
                        @StageBuilder.Optional String imageUrl,
                        @StageBuilder.Optional boolean inStock
                    ) {}
                    """),
                JavaFileObjects.forSourceString("test.ProductService", """
                    package test;
                    
                    public class ProductService {
                        
                        // Create basic product
                        public static Product createBasicProduct(String id, String name, double price) {
                            return ProductStageBuilder.builder()
                                .id(id)
                                .name(name)
                                .price(price)
                                .build();
                        }
                        
                        // Create product with description
                        public static Product createProductWithDescription(String id, String name, double price, String description) {
                            return ProductStageBuilder.builder()
                                .id(id)
                                .name(name)
                                .price(price)
                                .description(description)
                                .build();
                        }
                        
                        // Create full product
                        public static Product createFullProduct(
                            String id, 
                            String name, 
                            double price, 
                            String description, 
                            String category, 
                            String imageUrl, 
                            boolean inStock
                        ) {
                            return ProductStageBuilder.builder()
                                .id(id)
                                .name(name)
                                .price(price)
                                .description(description)
                                .category(category)
                                .imageUrl(imageUrl)
                                .inStock(inStock)
                                .build();
                        }
                        
                        // Create product with mixed optional fields
                        public static Product createProductMixed(
                            String id, 
                            String name, 
                            double price, 
                            String category, 
                            boolean inStock
                        ) {
                            return ProductStageBuilder.builder()
                                .id(id)
                                .name(name)
                                .price(price)
                                .category(category)
                                .inStock(inStock)
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testMixedRequiredAndOptionalFieldsInComplexScenario() {
        // Test complex scenario with multiple required and optional fields
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.DatabaseConnection", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class DatabaseConnection {
                        private final String host;
                        private final int port;
                        private final String database;
                        private final String username;
                        private final String password;
                        private final int connectionTimeout;
                        private final int socketTimeout;
                        private final boolean useSSL;
                        private final int maxRetries;
                        
                        public DatabaseConnection(
                            String host,
                            int port,
                            String database,
                            String username,
                            String password,
                            @StageBuilder.Optional int connectionTimeout,
                            @StageBuilder.Optional int socketTimeout,
                            @StageBuilder.Optional boolean useSSL,
                            @StageBuilder.Optional int maxRetries
                        ) {
                            this.host = host;
                            this.port = port;
                            this.database = database;
                            this.username = username;
                            this.password = password;
                            this.connectionTimeout = connectionTimeout;
                            this.socketTimeout = socketTimeout;
                            this.useSSL = useSSL;
                            this.maxRetries = maxRetries;
                        }
                        
                        public String getHost() { return host; }
                        public int getPort() { return port; }
                        public String getDatabase() { return database; }
                        public String getUsername() { return username; }
                        public String getPassword() { return password; }
                        public int getConnectionTimeout() { return connectionTimeout; }
                        public int getSocketTimeout() { return socketTimeout; }
                        public boolean isUseSSL() { return useSSL; }
                        public int getMaxRetries() { return maxRetries; }
                    }
                    """),
                JavaFileObjects.forSourceString("test.DatabaseManager", """
                    package test;
                    
                    public class DatabaseManager {
                        
                        // Basic connection
                        public static DatabaseConnection createBasicConnection(
                            String host, int port, String database, String username, String password
                        ) {
                            return DatabaseConnectionStageBuilder.builder()
                                .host(host)
                                .port(port)
                                .database(database)
                                .username(username)
                                .password(password)
                                .build();
                        }
                        
                        // Production connection with SSL
                        public static DatabaseConnection createProductionConnection(
                            String host, int port, String database, String username, String password
                        ) {
                            return DatabaseConnectionStageBuilder.builder()
                                .host(host)
                                .port(port)
                                .database(database)
                                .username(username)
                                .password(password)
                                .useSSL(true)
                                .connectionTimeout(30000)
                                .socketTimeout(60000)
                                .maxRetries(3)
                                .build();
                        }
                        
                        // Development connection with custom timeout
                        public static DatabaseConnection createDevelopmentConnection(
                            String host, int port, String database, String username, String password, int timeout
                        ) {
                            return DatabaseConnectionStageBuilder.builder()
                                .host(host)
                                .port(port)
                                .database(database)
                                .username(username)
                                .password(password)
                                .connectionTimeout(timeout)
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }
}