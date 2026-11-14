package kg.astra.health;


public record Baseline(
        String name,
        double median,
        double mad,
        int samples
) {
    static double MIN_SCALE_FRACTION = 0.02;
    static double MAD_TO_SIGMA = 1.2826;
    public double z(double value){
        ;
        double scale = Math.max(MAD_TO_SIGMA * mad, MIN_SCALE_FRACTION * Math.abs(median));
        if (scale == 0) return 0;
        return (value - median) / scale;
    }
}
