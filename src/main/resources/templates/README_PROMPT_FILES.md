# AI Travel Planner - Prompt Files Reference

## Active Prompt Files (6 files)

### 1. **DefaultPrompt.txt** ✅ PRIMARY
**Location**: `/templates/DefaultPrompt.txt` AND `/templates/general-instructions-variants/DefaultPrompt.txt`  
**Used By**: `ChatClientConfig.java`, `AbstractTextAgent.java` (hardcoded as primary prompt)  
**Purpose**: Main system prompt for AI travel assistant with comprehensive travel guidance  
**Status**: **FIXED - StringTemplate syntax errors resolved (removed JSON format examples)**  
**Content**: 
- 300-400 word detailed responses
- JSON response format (message + suggestions)
- Emergency assistance capabilities
- Budget optimization
- Accessibility support
- Location management
- All 15+ AI functions enabled

### 2. **SessionTitleGenerationPrompt.txt** ✅
**Location**: `/templates/SessionTitleGenerationPrompt.txt`  
**Used By**: `AISessionController.java`  
**Purpose**: Generates 3-5 word titles for travel planning sessions  
**Example**: "Budget Backpacking Southeast Asia"

### 3. **ExceptionHandlingPrompt.txt** ✅
**Location**: `/templates/ExceptionHandlingPrompt.txt`  
**Used By**: `GlobalExceptionHandler.java`  
**Purpose**: Converts technical errors into user-friendly travel assistance messages

### 4. **LightweightPromptSelection.txt** ✅
**Location**: `/templates/LightweightPromptSelection.txt`  
**Used By**: `AbstractTextAgent.java` (declared but agent always uses DefaultPrompt.txt)  
**Purpose**: Formerly used for prompt variant selection, now bypassed  
**Note**: Agent selection was simplified to always use DefaultPrompt.txt

### 5. **PromptAndFunctionSuggestionsPrompt.txt** ✅ CREATED
**Location**: `/templates/PromptAndFunctionSuggestionsPrompt.txt`  
**Used By**: `AbstractChatService.java` (field declared but never called)  
**Purpose**: Intelligent prompt and function selector (future enhancement)  
**Status**: Created to prevent missing file errors

### 6. **SimpleTranscriptionPrompt.txt** ✅ CREATED
**Location**: `/templates/SimpleTranscriptionPrompt.txt`  
**Used By**: `AudioTranscriptionService.java`  
**Purpose**: Instructs OpenAI Whisper API for audio transcription  
**Status**: Created for voice message feature

---

## Archived Prompt Files (37 files)

**Location**: `/templates/archive-unused/`

All prompt variants and extensions moved to archive because:
- AbstractTextAgent was simplified to ONLY use DefaultPrompt.txt
- DefaultPrompt.txt is comprehensive enough for all travel queries
- Prompt variant system (Emergency, Budget, Accessibility, etc.) was replaced with function calling

**Archived Files Include**:
- AccessibilityPrompt.txt, AccessibilityExtension.txt, AccessibilityPrompt_OLD.txt
- BudgetOptimizationPrompt.txt, BudgetExtension.txt, BudgetOptimizationPrompt_OLD.txt
- EmergencyAssistancePrompt.txt, EmergencyExtension.txt, EmergencyAssistancePrompt_OLD.txt
- FastQuestionPrompt.txt, GPT4oOptimizedPrompt.txt, UltraFastPrompt.txt
- SingleQuestionPrompt.txt, SingleQuestionPrompt_OPTIMIZED.txt
- PlanningExtension.txt, SpeedExtension.txt
- CoreBasePrompt.txt, DefaultPrompt_OLD.txt
- And 20+ other experimental/obsolete prompts

---

## General Instructions Variants Folder

**Location**: `/templates/general-instructions-variants/`  
**Contents**: Only `DefaultPrompt.txt` (identical to root DefaultPrompt.txt)  
**Purpose**: Historical folder for prompt variants, now contains only the active prompt  
**Loaded By**: `PromptsConfig.java` scans this folder and loads all .txt files into a Map

---

## How Prompt Selection Works (Current Architecture)

### Simplified Flow (As of December 2025):
1. **User sends message** → `FastAIService` or `EnhancedChatCallService`
2. **Service calls** → `AgentFactory.getAgent(aiMessage)`
3. **Factory returns** → `OpenAiAgent` (default) implementing `AbstractTextAgent`
4. **Agent's getPromptContent() method**:
   ```java
   // ALWAYS returns DefaultPrompt.txt
   String selectedPrompt = "DefaultPrompt.txt";
   log.info("🎯 Using DefaultPrompt.txt for comprehensive travel assistance");
   ```
5. **DefaultPrompt.txt loads** with ALL 15+ AI functions enabled
6. **OpenAI GPT-4o-mini processes** → Returns JSON response

### Why Simplified?
- **DefaultPrompt.txt is comprehensive** - handles all query types
- **Function calling is more effective** than prompt variants
- **Reduces complexity** - no need to predict which prompt variant to use
- **Better performance** - no prompt selection overhead
- **Easier maintenance** - single source of truth

---

## Recent Fixes (December 10-11, 2025)

### Issue 1: StringTemplate Syntax Error ✅ FIXED
**Problem**: DefaultPrompt.txt contained JSON format example with colons:
```
{ "message": "your detailed response here (300-400 words)" }
```
StringTemplate 4 engine interpreted this as template syntax, causing parsing errors.

**Fix**: Removed JSON example, replaced with plain text instruction:
```
Return ONLY the raw JSON object without any markdown formatting, code blocks, or backticks.
```

### Issue 2: Response Timeout ✅ FIXED
**Problem**: MAX_RESPONSE_TIME_MS = 8000ms too aggressive for complex function calling  
**Fix**: Increased to 30000ms in `FastAIService.java`

### Issue 3: Static Response Pattern ✅ FIXED
**Problem**: Regex `.*(help|assist|what can you).*` catching emergency queries  
**Fix**: Restricted pattern to exact match with emergency keyword exclusions

---

## Testing Status

### Prompt Tests: ✅ 5/5 PASSING
1. **Emergency Extension** (11.8s) - Emergency assistance with embassy info
2. **Budget Extension** (27.5s) - $800 Rome itinerary with specific venues
3. **Accessibility Extension** (19.8s) - Wheelchair-accessible London hotels
4. **Speed Extension** (5.1s) - Quick Tokyo restaurant recommendations
5. **Planning Extension** (7ms cached) - 5-day Paris detailed itinerary

### Production Validation: ✅ WORKING
- User query: "Budget backpacking route through Southeast Asia"
- AI generated comprehensive itinerary covering 5 countries
- **32 locations automatically extracted and mapped**
- WebSocket real-time synchronization working
- Session title: "Budget Backpacking Southeast Asia"

---

## Configuration References

### Load Prompts from Folder:
```java
// PromptsConfig.java
@Bean(name = "templatePrompts")
public Map<String, String> loadPrompts() throws Exception {
    Resource[] resources = resolver.getResources(
        "classpath:/templates/general-instructions-variants/*.*"
    );
    // Loads all .txt files into Map<filename, content>
}
```

### Default Prompt Injection:
```java
// ChatClientConfig.java
@Value("classpath:/templates/DefaultPrompt.txt")
private Resource promptResource;
```

### Agent Prompt Selection:
```java
// AbstractTextAgent.java
String selectedPrompt = "DefaultPrompt.txt"; // Hardcoded, no dynamic selection
```

---

## Future Enhancements (Optional)

1. **Restore Prompt Variant System**: If needed, re-enable intelligent prompt selection using `PromptAndFunctionSuggestionsPrompt.txt`
2. **Dynamic Function Filtering**: Enable only relevant functions per query type (currently all 15+ enabled)
3. **Context-Aware Extensions**: Load extension prompts for specialized scenarios
4. **Performance Optimization**: Cache prompt selection decisions

---

## Important Notes

⚠️ **DO NOT modify DefaultPrompt.txt without testing** - it affects ALL chat interactions  
⚠️ **Avoid StringTemplate syntax** - no colons in format examples, no quoted strings  
⚠️ **Test with node test-prompts.js** after any prompt changes  
⚠️ **Maintain 300-400 word response guidance** for consistent quality  
⚠️ **JSON format must be explicitly stated** - no markdown code blocks

---

Last Updated: December 11, 2025  
System Version: Spring Boot 3.4.0, Spring AI 1.0.0-M5, OpenAI GPT-4o-mini
