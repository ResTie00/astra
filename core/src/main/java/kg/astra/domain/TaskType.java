package kg.astra.domain;

//Kind of task. Drives dose (where the person physically is) and solver rules
public enum TaskType {
    //Extravehicular activity: least shielding, most dose
    EVA,
    //Work inside the habitat. Also the default for a slot with no assignment.
    INTERNAL,
    // Mandatory exercise (bone and muscle protection)
    EXERCISE,
    // Sleep in crew quarters (best shielded place)
    SLEEP
}
