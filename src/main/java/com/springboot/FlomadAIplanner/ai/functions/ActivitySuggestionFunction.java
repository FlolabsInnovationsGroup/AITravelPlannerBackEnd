package com.springboot.FlomadAIplanner.ai.functions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Description("Suggests activities for a given location.")
public class ActivitySuggestionFunction implements AIFunction<ActivitySuggestionFunction.ActivitySuggestionRequest, ActivitySuggestionFunction.ActivitySuggestionResponse> {

    @Override
    public ActivitySuggestionResponse apply(ActivitySuggestionRequest request) {
        log.info("Suggesting activities for location: {}", request.getLocation());
        // Mock implementation
        return new ActivitySuggestionResponse(List.of(
                new ActivityDetail("Eiffel Tower", "The Eiffel Tower is a wrought-iron lattice tower on the Champ de Mars in Paris, France. It is named after the engineer Gustave Eiffel, whose company designed and built the tower.", "Champ de Mars, 5 Avenue Anatole France, 75007 Paris, France", "https://www.toureiffel.paris/themes/custom/tour_eiffel/img/picto/logo.png"),
                new ActivityDetail("Louvre Museum", "The Louvre, or the Louvre Museum, is the world's largest art museum and a historic monument in Paris, France. A central landmark of the city, it is located on the Right Bank of the Seine in the city's 1st arrondissement.", "Rue de Rivoli, 75001 Paris, France", "https://www.louvre.fr/sites/default/files/imagecache/940x470/medias/medias_images/images/louvre-940x470.jpg")
        ));
    }

    @Data
    public static class ActivitySuggestionRequest {
        private String location;
        private String date;
    }

    @Data
    public static class ActivitySuggestionResponse {
        private final List<ActivityDetail> activities;
    }

    @Data
    public static class ActivityDetail {
        private final String name;
        private final String description;
        private final String address;
        private final String imageUrl;
    }
}
