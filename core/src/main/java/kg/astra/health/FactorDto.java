package kg.astra.health;

//One metric that drives a deviation.
//deviation  is  signed deviation from the personal norm, in robust z-score units
 //text  is ready-to-show explanation, e.g. "Sleep 1.5 h below your norm for 3 days"
public record FactorDto(String metric, double deviation, String text) {}
