import { MetricCard, type Tone } from "@/components/MetricCard"
import { useTrends } from "@/hooks/useTrends"
import type { CrewStatus, Norm, TrendPoint } from "@/types"

type Where = "below" | "within" | "above" | "unknown"

function where(v: number | null, n: Norm | null | undefined): Where {
  if (v === null || !n) return "unknown"
  if (v < n.low) return "below"
  if (v > n.high) return "above"
  return "within"
}

// последнее непустое значение из истории
function lastValue(points: TrendPoint[], key: "sleepHours" | "reactionMs" | "hrvMs") {
  for (let i = points.length - 1; i >= 0; i--) {
    const v = points[i][key]
    if (v !== null) return v
  }
  return null
}

const fmt = (v: number | null) => (v === null ? "—" : String(v))

export function MetricsRow({ crew }: { crew: CrewStatus }) {
  const { data } = useTrends(crew.crewId)
  const points = data?.points ?? []

  const sleep = lastValue(points, "sleepHours")
  const reaction = lastValue(points, "reactionMs")
  const hrv = lastValue(points, "hrvMs")

  const sleepW = where(sleep, data?.sleepNorm)
  const reactionW = where(reaction, data?.reactionNorm)
  const hrvW = where(hrv, data?.hrvNorm)

  const pct = (crew.cumulativeDoseMilliSv / crew.doseBudgetMilliSv) * 100
  const doseLabel: [string, Tone] =
    pct >= 85 ? ["High", "bad"] : pct >= 65 ? ["Attention", "warn"] : ["Normal", "good"]

  return (
    <div className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-5">
      <MetricCard
        title="Sleep"
        value={fmt(sleep)}
        unit="h"
        label={sleepW === "below" ? "Low" : sleepW === "unknown" ? "No data" : "Normal"}
        tone={sleepW === "below" ? "low" : sleepW === "unknown" ? "none" : "good"}
      />
      <MetricCard
        title="Reaction"
        value={fmt(reaction)}
        unit="ms"
        label={reactionW === "above" ? "Attention" : reactionW === "unknown" ? "No data" : "Normal"}
        tone={reactionW === "above" ? "warn" : reactionW === "unknown" ? "none" : "good"}
      />
      <MetricCard
        title="HRV"
        value={fmt(hrv)}
        unit="ms"
        label={hrvW === "below" ? "Attention" : hrvW === "unknown" ? "No data" : "Normal"}
        tone={hrvW === "below" ? "warn" : hrvW === "unknown" ? "none" : "good"}
      />
      <MetricCard
        title="Dose"
        value={crew.cumulativeDoseMilliSv.toFixed(1)}
        unit={`/ ${crew.doseBudgetMilliSv} mSv`}
        label={doseLabel[0]}
        tone={doseLabel[1]}
      />
      <MetricCard
        title="Self-check"
        value={String(crew.blindSpot.selfRating)}
        unit="/ 5"
        label={crew.blindSpot.flagged ? "Gap" : "Aligned"}
        tone={crew.blindSpot.flagged ? "warn" : "good"}
      />
    </div>
  )
}