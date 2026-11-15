import { Check, Lightbulb, TriangleAlert, type LucideIcon } from "lucide-react"
import { Card } from "@/components/ui/card"
import { cn } from "@/lib/utils"
import type { CrewStatus, Status } from "@/types"

/* ---------- 1. Подсчёт числа ---------- */

const WEIGHT: Record<Status, number> = { NORMAL: 100, WATCH: 50, ACT: 0 }

function calcScore(areas: CrewStatus["areas"]) {
  const sum = areas.reduce((acc, a) => acc + WEIGHT[a.status], 0)
  return Math.round(sum / areas.length)
}

/* ---------- 2. Геометрия дуги ---------- */

const R = 50
const C = 2 * Math.PI * R // длина всей окружности
const ARC = C * 0.75 // дуга занимает 270° из 360°

function scoreColor(score: number) {
  if (score >= 80) return "stroke-emerald-400"
  if (score >= 50) return "stroke-yellow-400"
  return "stroke-red-500"
}

/* ---------- 3. Стили статусов ---------- */

const STATUS_STYLE: Record<Status, { icon: LucideIcon; color: string; bg: string }> = {
  NORMAL: { icon: Check, color: "text-emerald-400", bg: "bg-emerald-400/15" },
  WATCH: { icon: TriangleAlert, color: "text-yellow-400", bg: "bg-yellow-400/15" },
  ACT: { icon: TriangleAlert, color: "text-red-500", bg: "bg-red-500/15" },
}

/* ---------- 4. Три строки справа ---------- */

interface RowConfig {
  title: string
  status: Status
  labels: Record<Status, string>
}

function buildRows(crew: CrewStatus): RowConfig[] {
  const areaStatus = (name: string): Status =>
    crew.areas.find((a) => a.area === name)?.status ?? "NORMAL"

  // у нас нет области "сон", поэтому смотрим, назван ли сон среди причин расхождения
  const sleepStatus: Status = crew.blindSpot.drivers.some((d) => d.metric === "SLEEP_HOURS")
    ? "ACT"
    : "NORMAL"

  return [
    {
      title: "Vital Signs",
      status: areaStatus("CARDIOVASCULAR"),
      labels: { NORMAL: "Optimal", WATCH: "Reduced", ACT: "Critical" },
    },
    {
      title: "Stress Level",
      status: areaStatus("BEHAVIORAL"),
      labels: { NORMAL: "Low", WATCH: "Elevated", ACT: "High" },
    },
    {
      title: "Sleep Quality",
      status: sleepStatus,
      labels: { NORMAL: "Good", WATCH: "Fair", ACT: "Poor" },
    },
  ]
}

/* ---------- Компонент ---------- */

export function OverallHealth({ crew }: { crew: CrewStatus }) {
  const score = calcScore(crew.areas)
  const rows = buildRows(crew)
  const isOk = crew.overall === "NORMAL"

  return (
    <Card className="gap-2 p-6">
      <h3 className="text-xl font-semibold">Overall Health</h3>

      <div className="flex items-center gap-6">
        {/* дуга */}
        <div className="relative size-48 shrink-0">
          <svg viewBox="0 0 120 120" className="size-full">
            {/* серая дорожка */}
            <circle
              cx="60"
              cy="60"
              r={R}
              fill="none"
              strokeWidth="10"
              strokeLinecap="round"
              className="stroke-white/10"
              strokeDasharray={`${ARC} ${C}`}
              transform="rotate(135 60 60)"
            />
            {/* цветная часть */}
            <circle
              cx="60"
              cy="60"
              r={R}
              fill="none"
              strokeWidth="10"
              strokeLinecap="round"
              className={cn("transition-all duration-700", scoreColor(score))}
              strokeDasharray={`${(score / 100) * ARC} ${C}`}
              transform="rotate(135 60 60)"
            />
          </svg>

          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="text-4xl font-bold tabular-nums">{score}%</span>
            <span className="text-sm text-muted-foreground">Health Score</span>
          </div>
        </div>

        {/* три строки */}
        <ul className="space-y-4">
          {rows.map((r) => {
            const { icon: Icon, color, bg } = STATUS_STYLE[r.status]
            return (
              <li key={r.title} className="flex items-center gap-3">
                <span className={cn("flex size-9 items-center justify-center rounded-full", bg, color)}>
                  <Icon className="size-5" />
                </span>
                <div>
                  <p className="text-sm font-medium">{r.title}</p>
                  <p className={cn("text-sm font-semibold", color)}>{r.labels[r.status]}</p>
                </div>
              </li>
            )
          })}
        </ul>
      </div>

      {/* плашка */}
      <div className="flex items-center gap-3 rounded-xl bg-white/5 p-4">
        <Lightbulb className="size-8 shrink-0 text-yellow-400" />
        <div>
          <p className="font-semibold text-yellow-400">
            {isOk ? "You're doing great!" : "Needs attention"}
          </p>
          <p className="text-sm text-muted-foreground">
            {isOk ? "Keep following your routine." : crew.action.text}
          </p>
        </div>
      </div>
    </Card>
  )
}