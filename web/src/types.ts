export type Status = "NORMAL" | "WATCH" | "ACT"

export interface Area {
  area: string
  status: Status
  explanation: string
}

export interface Driver {
  metric: string
  deviation: number
  text: string
}

export interface CrewStatus {
  crewId: string
  name: string
  asOf: string
  overall: Status
  areas: Area[]
  blindSpot: {
    selfRating: number
    objectiveScore: number
    gap: number
    flagged: boolean
    drivers: Driver[]
  }
  action: { type: string; text: string }
  cumulativeDoseMilliSv: number
  doseBudgetMilliSv: number
}

/* ---- payload'ы ---- */

export interface TelemetryPayload {
  value: number
}

export interface AlertPayload {
  id: number
  severity: string
  type: string
  message: string
  createdAt: string
}

export interface SpaceWeatherPayload {
  type: string
  onset: string
  peak: string
  peakProtonFluxPfu: number
  durationHours: number
}

export interface CheckinInput {
  crewId: string
  wellbeing: number
  mood: number
  stress: number
  symptoms: string[]
  reactionMeanMs: number
  reactionLapses: number
}

export interface CheckinPayload extends CheckinInput {
  timestamp: string | null
}

export interface Assignment {
  taskId: string
  taskName: string
  taskType: string
  crewId: string
  crewName: string
  start: string
  end: string
}

export interface CrewRisk {
  crewId: string
  crewName: string
  doseP50MilliSv: number
  doseP95MilliSv: number
  exceedBudgetProbability: number
}

export interface Risk {
  riskScore: number
  crewExceedProbability: number
  impairedCriticalHours: number
  runs: number
  byCrew: CrewRisk[]
}

export interface PlanChange {
  taskId: string
  taskName: string
  type: string
  fromCrewId: string
  toCrewId: string
  fromStart: string
  toStart: string
  reason: string
  explanation: string
}

export interface ReplanOption {
  optionId: string
  strategy: string
  title: string
  plan: { assignments: Assignment[] }
  changes: PlanChange[]
  risk: Risk
  solverStatus: string
}

export interface ReplanResponse {
  runId: string
  horizonStart: string
  horizonHours: number
  currentPlan: { assignments: Assignment[] }
  currentRisk: Risk
  options: ReplanOption[]
}

export type Severity = "INFO" | "WARNING" | "CRITICAL"

export interface AlertItem {
  id: number
  crewId?: string | null
  severity: Severity
  type: string
  message: string
  createdAt: string
}

export interface SpaceWeather {
  type: string
  onset: string
  peak: string
  peakProtonFluxPfu?: number
  durationHours?: number
}


export interface AcceptPlanResponse {
  runId: string
  optionId: string
  schedule: { assignments: Assignment[] }
}

export interface Norm {
  median: number
  low: number
  high: number
}

export interface TrendPoint {
  date: string
  sleepHours: number | null
  reactionMs: number | null
  hrvMs: number | null
  selfRating: number | null
}

export interface Trends {
  crewId: string
  points: TrendPoint[]
  sleepNorm: Norm | null
  reactionNorm: Norm | null
  hrvNorm: Norm | null
}