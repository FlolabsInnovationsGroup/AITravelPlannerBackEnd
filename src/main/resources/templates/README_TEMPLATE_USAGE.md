# Template Usage Documentation

## ACTIVE TEMPLATES (Do NOT modify or move)

### Core Templates (Referenced in Java Code)
These templates are directly referenced in Java files and must remain in the main `/templates/` folder:

1. **DefaultPrompt.txt** - Referenced in `DefaultPromptConfig.java`
2. **ExceptionHandlingPrompt.txt** - Referenced in `ExceptionHandlingConfig.java` 
3. **LightweightPromptSelection.txt** - Referenced in `LightweightPromptSelectionConfig.java`
4. **PromptAndFunctionSuggestionsPrompt.txt** - Referenced in `PromptAndFunctionSuggestionsConfig.java`
5. **SessionTitleGenerationPrompt.txt** - Referenced in `SessionTitleGenerationConfig.java`
6. **SimpleTranscriptionPrompt.txt** - Referenced in `SimpleTranscriptionConfig.java`

### Dynamic Templates (Loaded by PromptsConfig.java)
These templates are in the `/general-instructions-variants/` folder and are loaded dynamically:

- **LocationRequestPrompt.txt** - Main AI location handling template (CRITICAL - recently fixed StringTemplate syntax)
- All other files in `general-instructions-variants/` folder

## ARCHIVED TEMPLATES

The following templates have been moved to `/archive-unused/` folder as they are not referenced in the current codebase:

- AccessibilityPrompt.txt
- BookingConfirmationPrompt.txt  
- BudgetOptimizationPrompt.txt
- EmergencyAssistancePrompt.txt
- EnhancedTravelAssistantPrompt.txt
- GroupTravelPrompt.txt
- ItineraryModificationPrompt.txt
- LocalRegulationsPrompt.txt
- LocationRequestPrompt.txt (duplicate from main folder)
- MultiLanguagePrompt.txt
- OPTIMIZED_ItineraryPlannerPrompt.txt
- OPTIMIZED_SearchSpecialistPrompt.txt
- OPTIMIZED_SimplePrompt.txt
- OPTIMIZED_TravelPlannerPrompt.txt
- TravelDocumentPrompt.txt
- UnifiedPlanningPrompt.txt
- WeatherSafetyPrompt.txt
- WithFunctionsQuestionPrompt.txt

## Important Notes

1. **StringTemplate Syntax**: All templates must use proper StringTemplate syntax with escaped brackets `\[` and `\]`
2. **PromptsConfig.java**: Loads templates from `classpath:/templates/general-instructions-variants/*.*`
3. **Active Template Location**: The system uses the template in `/general-instructions-variants/` folder, NOT the main folder
4. **Backup**: All moved templates are preserved in `/archive-unused/` and can be restored if needed

## Recent Fixes (October 2025)

- Fixed StringTemplate parsing errors in `LocationRequestPrompt.txt` (variants folder)
- Cleaned up duplicate templates to prevent confusion
- Archived unused templates to improve maintainability

## Template Modification Guidelines

1. **Before editing any template**: Verify which folder the application actually loads from
2. **For variants templates**: Edit files in `/general-instructions-variants/` folder
3. **For core templates**: Edit files in main `/templates/` folder
4. **Always test**: Restart backend after template changes to verify no StringTemplate errors
5. **Syntax requirements**: Use `\[` and `\]` for literal brackets, avoid unescaped special characters