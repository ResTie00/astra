export type Section = "overview" | "checkin" | "planning" | "analytics" | "alerts" | "settings"

// в меню четыре пункта, как в дизайне; alerts и settings открываются из других мест
export const NAV: { id: Section; label: string }[] = [
  { id: "overview", label: "Dashboard" },
  { id: "checkin", label: "Check-in" },
  { id: "planning", label: "Planning" },
  { id: "analytics", label: "Analytics" },
  { id:"alerts" , label: "Alerts" },
  { id:"settings" , label: "Settings" }
]