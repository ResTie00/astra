package kg.astra.health;

import java.time.LocalDate;
import java.util.List;

public record TrendsResponse(String crewId, List<Point> points, Band sleepNorm, Band reactionNorm, Band hrvNorm) {

    //One day (UTC). A field is null when there is no data for that day
    public record Point(LocalDate date, Double sleepHours, Double reactionMs, Double hrvMs, Integer selfRating) {}

    // "Your norm" band: median ± 2 scales, the same scale the status rules use (|z| < 2 = normal)
    public record Band(double median, double low, double high) {}
}
