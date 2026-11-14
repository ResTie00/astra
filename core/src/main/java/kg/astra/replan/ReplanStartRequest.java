package kg.astra.replan;

//POST /api/replan. reason is free text for the log ("storm alert", "blind spot c2"); may be null
public record ReplanStartRequest(String reason) {}
