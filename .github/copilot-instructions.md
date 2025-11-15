# AI Travel Planner Backend - Copilot Instructions

## Project Overview
This is the **Spring Boot 3.4.0 backend** for the AI Travel Planner, featuring **Spring AI integration**, **GPT-4o-mini function calling**, **WebSocket real-time communication**, and comprehensive **travel intelligence APIs**. The system provides 15+ AI functions with enterprise-grade security, rate limiting, and MongoDB persistence.

## Architecture & Tech Stack

### Backend Framework (Spring Boot 3.4.0)
- **Spring AI**: 1.0.0-M5 with OpenAI GPT-4o-mini integration
- **Function Calling**: 15+ AI functions with auto-discovery and registration
- **Rich Location Data**: Enhanced Google Places integration with photos, ratings, hours, contact info
- **Database**: MongoDB with Mongock migrations and chat memory persistence  
- **WebSocket**: STOMP messaging for real-time location synchronization
- **Security**: JWT + OAuth2 (Google, LinkedIn, Facebook) + guest token system
- **Rate Limiting**: Bucket4j with JWT integration and IP-based fallback

### AI & External APIs
- **AI Model**: OpenAI GPT-4o-mini (8192 tokens, 30-message memory)
- **Travel APIs**: Google Places, Amadeus flights, Viator tours, WeatherAPI
- **Geolocation**: PositionStack for address geocoding
- **Currency**: FreeCurrency for exchange rates
- **Validation**: Jakarta Bean Validation with comprehensive JSON schema validation

### Quality & Testing
- **Testing**: 11/11 integration tests passing with comprehensive function validation
- **Memory Management**: Optimized chat memory with automatic cleanup (30-message limit, 8192 tokens)
- **Error Handling**: Robust exception handling with graceful degradation
- **Monitoring**: Comprehensive logging and performance metrics
- **Production Ready**: Clean console logging, performance optimizations, enterprise-grade configuration

## Key Development Patterns

### 1. AI Function Development Architecture
```java
// Standard AI Function Pattern
@Component("emergencyAssistanceFunction")
@Description("Provides immediate emergency assistance for travelers in crisis situations")
public class EmergencyAssistanceFunction implements Function<EmergencyAssistanceRequest, String> {
    
    @Override
    public String apply(EmergencyAssistanceRequest request) {
        // 1. Validate input parameters
        if (request == null || StringUtils.isEmpty(request.getEmergencyType())) {
            return "❌ Please specify the type of emergency you're experiencing.";
        }
        
        // 2. Build comprehensive response
        StringBuilder response = new StringBuilder();
        response.append("🆘 EMERGENCY ASSISTANCE ACTIVATED\n\n");
        
        // 3. Handle different emergency types with specific protocols
        switch (request.getEmergencyType().toLowerCase()) {
            case "medical":
                response.append(handleMedicalEmergency(request));
                break;
            case "safety":
                response.append(handleSafetyEmergency(request));
                break;
            default:
                response.append(handleGeneralEmergency(request));
        }
        
        return response.toString();
    }
}
```

**Convention**: All AI functions use `@Component` with descriptive names, implement `Function<RequestType, String>`, and include detailed `@Description` annotations for GPT function calling.

### Enhanced AI Function Categories - UPDATED

#### **A. Location Management Functions (6 functions)**
- `AddLocationFunction`: Rich Google Places data integration with photos, ratings, hours
- `RemoveLocationFunction`, `RemoveLocationByNameFunction`: Precise location removal
- `ClearLocationsFunction`: Bulk location clearing with WebSocket broadcast
- `ReadLocationsFunction`, `ShowLocationFunction`: Location retrieval and display

#### **B. Travel Intelligence Functions (5+ functions)**
- `EmergencyAssistanceFunction`: Crisis support with medical/safety protocols
- `BudgetOptimizationFunction`: Cost analysis and money-saving recommendations
- `AccessibilityFunction`: Accessibility-focused travel planning
- `FlightSearchFunction`: Amadeus API integration for flight booking
- `PlaceTextSearchFunction`: Google Places advanced search capabilities

#### **C. Utility & Support Functions (4+ functions)**
- `CurrentDateTimeFunction`: Temporal context for travel planning
- `PositionStackGeocodingFunction`: Address to coordinate conversion
- `DistanceCalculatorFunction`: Route and distance calculations
- `AutoLocationExtractionFunction`: Intelligent location detection from text

### 2. Request DTO Pattern with JSON Schema Validation
```java
// Standard Request DTO Pattern
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonClassDescription("Request for emergency travel assistance")
public class EmergencyAssistanceRequest {
    
    @JsonPropertyDescription("Type of emergency (medical, safety, documents, transport, financial)")
    @NotNull(message = "Emergency type cannot be null")
    @Size(min = 1, max = 50, message = "Emergency type must be between 1 and 50 characters")
    private String emergencyType;
    
    @JsonPropertyDescription("Current location of the traveler")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String currentLocation;
    
    @JsonPropertyDescription("Detailed description of the emergency situation")
    @Size(max = 1000, message = "Emergency details must not exceed 1000 characters")
    private String emergencyDetails;
    
    // Jackson constructors for Spring AI function calling
    @JsonCreator
    public EmergencyAssistanceRequest(@JsonProperty("emergencyType") String emergencyType) {
        this.emergencyType = emergencyType;
    }
}
```

**Pattern**: Use `@JsonPropertyDescription` for OpenAI function calling, `@JsonCreator` for proper deserialization, and Jakarta Bean Validation annotations for input validation.

### 3. WebSocket Real-Time Communication Pattern
```java
@Service
@Slf4j
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    // Session-based WebSocket messaging pattern
    public void sendLocationUpdate(String sessionId, LocationData location) {
        try {
            String destination = "/topic/locations/" + sessionId;
            
            LocationEvent event = LocationEvent.builder()
                .type("LOCATION_ADDED")
                .payload(location)
                .sessionId(sessionId)
                .timestamp(Instant.now())
                .build();
                
            messagingTemplate.convertAndSend(destination, event);
            log.info("Sent location update to session {}: {}", sessionId, location.getName());
            
        } catch (Exception e) {
            log.error("Failed to send WebSocket update for session {}: {}", sessionId, e.getMessage(), e);
            // Implement retry logic or alternative notification method
        }
    }
    
    // Broadcast map clearing to all session subscribers
    public void sendMapCleared(String sessionId) {
        String destination = "/topic/locations/" + sessionId;
        
        LocationEvent event = LocationEvent.builder()
            .type("MAP_CLEARED")
            .sessionId(sessionId)
            .timestamp(Instant.now())
            .build();
            
        messagingTemplate.convertAndSend(destination, event);
    }
}
```

**Convention**: All WebSocket messages use session-scoped destinations (`/topic/locations/{sessionId}`), include event types, and implement proper error handling with retry logic.

### 4. Spring AI Integration & Function Registration
```java
@Configuration
@Slf4j
public class FunctionConfiguration {
    
    // Auto-discovery pattern for AI functions
    @Bean
    public List<Function<?, ?>> aiFunctions(
            AddLocationFunction addLocationFunction,
            RemoveLocationByNameFunction removeLocationByNameFunction,
            EmergencyAssistanceFunction emergencyAssistanceFunction,
            BudgetOptimizationFunction budgetOptimizationFunction,
            AccessibilityFunction accessibilityFunction) {
        
        List<Function<?, ?>> functions = List.of(
            addLocationFunction,
            removeLocationByNameFunction,
            emergencyAssistanceFunction,
            budgetOptimizationFunction,
            accessibilityFunction
        );
        
        log.info("Registered {} AI functions: {}", 
                functions.size(), 
                functions.stream()
                    .map(f -> f.getClass().getSimpleName())
                    .collect(Collectors.joining(", ")));
        
        return functions;
    }
    
    // Function description extraction for OpenAI
    @Bean
    public List<String> aiFunctionDescriptions() {
        return functionAnnotationUtils.extractBeanNamesAndDescriptions(
            aiFunctions.stream()
                .map(Function::getClass)
                .toArray(Class[]::new)
        );
    }
}
```

**Pattern**: Functions are auto-discovered through Spring DI, registered as beans, and descriptions extracted from `@Description` annotations for GPT function calling.

### 5. Chat Memory & Session Management
```java
@Component
@Slf4j
public class CustomDatabaseChatMemory implements ChatMemory {
    
    @Value("${ai.chat.memory.max-messages:30}")
    private int maxMessages;
    
    @Value("${ai.chat.memory.max-tokens:8000}")
    private int maxTokens;
    
    @Override
    public void add(String conversationId, Message message) {
        try {
            Conversation conversation = getOrCreateConversation(conversationId);
            List<Message> messages = conversation.getMessages();
            
            messages.add(message);
            
            // Automatic memory cleanup to prevent bloat
            performMemoryCleanup(messages);
            
            conversationRepository.save(conversation);
            log.debug("Added message to conversation {}. Total messages: {}", 
                     conversationId, messages.size());
            
        } catch (Exception e) {
            log.error("Error adding message to conversation {}: {}", 
                     conversationId, e.getMessage(), e);
            throw new ChatMemoryException("Failed to add message", e);
        }
    }
    
    private void performMemoryCleanup(List<Message> messages) {
        if (messages.size() > maxMessages) {
            int start = messages.size() - maxMessages;
            List<Message> messagesToKeep = new ArrayList<>(
                messages.subList(start, messages.size())
            );
            
            messages.clear();
            messages.addAll(messagesToKeep);
            
            log.info("Performed memory cleanup. Messages retained: {}", messages.size());
        }
    }
}
```

**Convention**: Chat memory uses MongoDB persistence with session-based conversation IDs, automatic cleanup, and configurable limits for production scalability.

## Configuration Management

### Critical Environment Variables
```bash
# AI & OpenAI Configuration
SPRING_AI_OPENAI_API_KEY=your_openai_key
OPENAI_MODEL=gpt-4o-mini
AI_CHAT_MEMORY_MAX_MESSAGES=30
AI_CHAT_MEMORY_MAX_TOKENS=8000

# External API Keys
AMADEUS_API_KEY=your_amadeus_key
GOOGLE_PLACES_API_KEY=your_google_key  
VIATOR_API_KEY=your_viator_key
WEATHER_API_KEY=your_weather_key
POSITION_STACK_API_KEY=your_position_stack_key

# Security & Authentication
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION_MS=86400000

# Database Configuration
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/travel_planner

# Rate Limiting Configuration
APP_RATE_LIMIT_CHAT_REQUESTS_PER_MINUTE=10
APP_RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE=20
APP_RATE_LIMIT_GUEST_REQUESTS_PER_MINUTE=5
```

**Critical**: Use `application.properties` for defaults and environment variables for sensitive configuration. All AI functions require proper API keys for external service integration.

## Development Workflows

### Backend Development Setup
```bash
# Environment validation
mvn validate -Pcheck-env  # Validate all required environment variables

# Local development with MongoDB
docker-compose up -d mongodb  # Start MongoDB container
mvn spring-boot:run          # Start Spring Boot application

# Full integration testing
mvn test -Dtest=PromptSystemIntegrationTest  # Run AI function tests
mvn test -Dspring.profiles.active=test      # Run all tests with test profile
```

### AI Function Development Workflow
```bash
# 1. Create new function following the established pattern
# 2. Add function to FunctionConfiguration.java
# 3. Test function with integration tests
mvn test -Dtest=*IntegrationTest

# 4. Validate function calling with OpenAI
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Testing Patterns for AI Functions
```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class AIFunctionIntegrationTest {
    
    @Test
    @Order(1)
    void testEmergencyAssistanceFunction() {
        // Test emergency function with various scenarios
        String response = sendTestMessage("I'm stranded at the airport and need help");
        
        assertThat(response)
            .containsIgnoringCase("emergency")
            .containsIgnoringCase("immediate")
            .containsIgnoringCase("contact");
            
        // Validate function was actually called
        verify(emergencyAssistanceFunction, times(1)).apply(any());
    }
    
    @Test
    @Order(2)
    void testBudgetOptimizationFunction() {
        String response = sendTestMessage("Find cheap flights to Asia");
        
        assertThat(response)
            .containsIgnoringCase("budget")
            .containsIgnoringCase("save")
            .hasLineCountGreaterThan(5); // Ensure detailed response
    }
}
```

## Critical Integration Points

### 1. AI Function → WebSocket Integration
```java
// Pattern: AI functions can trigger WebSocket updates
@Component
public class AddLocationFunction implements Function<AddLocationRequest, String> {
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    public String apply(AddLocationRequest request) {
        // 1. Process location addition
        LocationData location = processLocationRequest(request);
        
        // 2. Save to database with session context
        String sessionId = SessionContextHolder.getSessionId();
        locationRepository.save(location.withSessionId(sessionId));
        
        // 3. Broadcast real-time update to frontend
        webSocketService.sendLocationUpdate(sessionId, location);
        
        return "📍 Added " + location.getName() + " to your travel map!";
    }
}
```

### 2. Rate Limiting Integration with JWT
```java
@Component
@Order(1)
@Slf4j
public class RateLimitingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 1. Extract user identifier from JWT or IP
        String userId = extractUserIdentifier(httpRequest);
        
        // 2. Determine rate limit type based on endpoint and user type
        RateLimitType limitType = determineRateLimitType(httpRequest, userId);
        
        // 3. Check rate limit bucket
        if (!rateLimitService.allowRequest(userId, limitType)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.setHeader("X-RateLimit-Type", limitType.toString());
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

### 3. Validation Framework Integration
```java
@Service
@Slf4j
public class AIResponseValidationService {
    
    // Comprehensive validation with context
    public ValidationResult validateWithContext(Object aiResponse, 
                                               String sessionId, 
                                               String context) {
        Set<ConstraintViolation<Object>> violations = validator.validate(aiResponse);
        
        if (violations.isEmpty()) {
            return ValidationResult.success();
        }
        
        List<String> errorMessages = violations.stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.toList());
            
        log.warn("Validation failed for session {} in context {}: {}", 
                sessionId, context, errorMessages);
                
        return ValidationResult.failure(String.join(", ", errorMessages), violations);
    }
}
```

## Production Deployment Considerations

### Performance Optimizations
- **Connection Pooling**: Configure MongoDB and HTTP client connection pools
- **Caching Strategy**: Redis for location searches and frequent AI responses  
- **Async Processing**: Use `@Async` for expensive external API calls
- **Function Batching**: Batch multiple function calls when possible

### Security Best Practices
- **JWT Validation**: All API endpoints validate JWT tokens with proper expiration
- **Rate Limiting**: Different limits for authenticated vs guest users
- **Input Sanitization**: Validate all AI function inputs with Jakarta Bean Validation
- **CORS Configuration**: Proper CORS setup for frontend integration

### Monitoring & Logging
- **Application Metrics**: Micrometer integration for performance monitoring
- **AI Function Metrics**: Track function call success rates and response times
- **Error Tracking**: Comprehensive exception handling with structured logging
- **WebSocket Health**: Monitor WebSocket connection health and message delivery

## Common Debugging Scenarios

### AI Function Not Being Called
1. Check function registration in `FunctionConfiguration.java`
2. Verify `@Description` annotation is present and descriptive
3. Validate request DTO has proper Jackson annotations
4. Test function directly with unit tests

### WebSocket Connection Issues  
1. Verify STOMP configuration in `WebSocketConfig.java`
2. Check session ID consistency between frontend and backend
3. Monitor WebSocket message delivery logs
4. Test with WebSocket debugging tools

### Chat Memory Not Persisting
1. Verify MongoDB connection and database creation
2. Check conversation ID generation and consistency
3. Validate `CustomDatabaseChatMemory` configuration
4. Test memory cleanup thresholds

### Rate Limiting False Positives
1. Check JWT token parsing in `RateLimitingFilter`
2. Verify user type detection (guest vs authenticated)
3. Monitor rate limit bucket creation and cleanup
4. Test with different user scenarios

## AI Function Categories & Patterns

### Location Management Functions (6 functions)
- **Pattern**: Session-scoped location operations with WebSocket broadcasting
- **Examples**: `AddLocationFunction`, `RemoveLocationFunction`, `ClearLocationsFunction`
- **Integration**: Direct map synchronization via WebSocket events

### Travel Intelligence Functions (3 functions)
- **Pattern**: External API integration with comprehensive response formatting  
- **Examples**: `EmergencyAssistanceFunction`, `BudgetOptimizationFunction`, `AccessibilityFunction`
- **Integration**: Rich travel guidance with real-time assistance

### Search & Booking Functions (Advanced)
- **Pattern**: External API integration (Amadeus, Google Places, Viator)
- **Examples**: `FlightSearchFunction`, `HotelsSearchFunction`, `PlaceTextSearchFunction`
- **Integration**: Real-time travel data with booking guidance

## Performance Benchmarks

### Response Time Targets
- **AI Function Calls**: < 2 seconds for single function execution
- **WebSocket Messages**: < 100ms for location updates
- **Database Operations**: < 500ms for conversation retrieval
- **External API Calls**: < 5 seconds with proper timeout handling

### Scalability Metrics
- **Concurrent Sessions**: Support 1000+ active WebSocket connections
- **AI Function Throughput**: 100+ function calls per minute
- **Memory Usage**: < 2GB heap with 30-message chat memory
- **Database Performance**: < 1000ms for complex location queries

## Service Layer Architecture
```java
// Enhanced service classes with retry mechanisms and proper error handling
export class MapService {
  private wsRetryManager = new WebSocketRetryManager(5, 1000);
  private connectionStatus: 'disconnected' | 'connecting' | 'connected' = 'disconnected';
  
  // Production-ready error handling - no console.log pollution
  private logError(context: string, error: any) {
    // Clean logging for production - console cleanup implemented
    if (process.env.NODE_ENV === 'development') {
      console.warn(`MapService[${context}]:`, error);
    }
  }
}
```

**Convention**: Use service classes for external integrations, implement retry mechanisms, clean up console logs for production.

## Console Logging Standards
- **Development**: Use `console.warn()` for non-critical issues that are expected (e.g., missing optional data)
- **Production**: All console logs are cleaned up - use conditional logging based on `NODE_ENV`
- **Testing**: Console output is expected in tests - mock console methods when needed
- **Service Classes**: Implement `logError()` methods with environment checks

## When Working on This Backend

1. **Always validate environment** with required API keys before development
2. **Test AI functions individually** using integration test suite  
3. **Monitor WebSocket connections** when debugging real-time features
4. **Use session context properly** - all operations are session-scoped
5. **Follow the function calling patterns** for new AI capability integration
6. **Implement proper error handling** with graceful degradation
7. **Validate inputs thoroughly** using Jakarta Bean Validation framework
8. **Monitor rate limits** during testing to avoid API blocks

The system is architected for **enterprise-scale AI travel assistance** with **real-time collaboration**, **comprehensive function calling**, and **production-grade security and performance**.