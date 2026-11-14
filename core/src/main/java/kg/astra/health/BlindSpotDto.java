package kg.astra.health;

import java.util.List;


//Gap between how the astronaut rates themselves and what the data shows.

//@param selfRating  is a latest self-rated wellbeing, 1..5
//@param objectiveScore is a wellbeing estimated from data, same 1..5 scale
//@param gap is a selfRating - objectiveScore (positive = feels better than data says)
//@param flagged  is true when the gap is large enough to show the warning

public record BlindSpotDto(int selfRating, double objectiveScore, double gap, boolean flagged,
                           List<FactorDto> drivers) {}
