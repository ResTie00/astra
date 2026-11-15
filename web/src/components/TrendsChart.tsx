import { useState } from "react"
import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Chart } from "@/components/Chart"
import { Pill } from "@/components/Pill"
import { useTrends } from "@/hooks/useTrends"
import type { Norm, Trends } from "@/types"
import type { EChartsOption } from "echarts"

type Key = "sleepHours" | "reactionMs" | "hrvMs" | "selfRating"

interface Metric {
  key: Key
  label: string
  unit: string
  norm: (t: Trends) => Norm | null
}

const METRICS: Metric[] = [
  { key: "sleepHours", label: "Sleep", unit: "h", norm: (t) => t.sleepNorm },
  { key: "reactionMs", label: "Reaction", unit: "ms", norm: (t) => t.reactionNorm },
  { key: "hrvMs", label: "HRV", unit: "ms", norm: (t) => t.hrvNorm },
  { key: "selfRating", label: "Self-check", unit: "/ 5", norm: () => null },
]

const RANGES = [
  { days: 7, label: "7d" },
  { days: 14, label: "14d" },
]

const ORANGE = "#f59e0b"
const round1 = (x: number) => Math.round(x * 10) / 10

export function TrendsChart({ crewId }: { crewId: string }) {
  // --- хуки строго до первого return ---
  const [metricKey, setMetricKey] = useState<Key>("hrvMs")
  const [days, setDays] = useState(14)
  const { data, isLoading, isError } = useTrends(crewId)

  const metric = METRICS.find((m) => m.key === metricKey) ?? METRICS[0]

  const header = (
    <div className="flex flex-wrap items-center justify-between gap-3">
      <h3 className="text-2xl font-semibold">Health Trends</h3>
      <div className="flex flex-wrap items-center gap-2">
        {METRICS.map((m) => (
          <Pill key={m.key} active={m.key === metricKey} onClick={() => setMetricKey(m.key)}>
            {m.label}
          </Pill>
        ))}
        <span className="mx-1 h-4 w-px bg-white/20" />
        {RANGES.map((r) => (
          <Pill key={r.days} active={r.days === days} onClick={() => setDays(r.days)}>
            {r.label}
          </Pill>
        ))}
      </div>
    </div>
  )

  if (isLoading) return <Skeleton className="h-80 w-full" />
  if (isError || !data) {
    return <Card className="p-4 text-sm text-red-500">Не удалось загрузить историю</Card>
  }

  const points = data.points.slice(-days)
  const values = points.map((p) => p[metric.key])
  const nums = values.filter((v): v is number => v !== null)
  const norm = metric.norm(data)

  if (nums.length === 0) {
    return (
      <Card className="gap-3 p-5">
        {header}
        <p className="py-16 text-center text-sm text-muted-foreground">
          Недостаточно данных для этого астронавта
        </p>
      </Card>
    )
  }

  // ось включает и значения, и полосу нормы
  const lo = Math.min(...nums, ...(norm ? [norm.low] : []))
  const hi = Math.max(...nums, ...(norm ? [norm.high] : []))
  const pad = (hi - lo) * 0.15 || 1

  const option: EChartsOption = {
    textStyle: { color: "#a1a1aa" },
    grid: { left: 48, right: 16, top: 16, bottom: 28 },
    tooltip: { trigger: "axis" },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: points.map((p) => p.date.slice(5)), // "2026-11-14" → "11-14"
    },
    yAxis: {
      type: "value",
      min: round1(lo - pad),
      max: round1(hi + pad),
      splitLine: { lineStyle: { color: "rgba(255,255,255,0.12)" } },
    },
    series: [
      {
        type: "line",
        smooth: true , 
        connectNulls: true,
        data: values,
        symbol: "none",
        lineStyle: { color: ORANGE, width: 2 },
        areaStyle: { color: "rgba(245,158,11,0.15)" },
        markArea: norm
          ? {
              silent: true,
              itemStyle: { color: "rgba(34,197,94,0.12)" },
              data: [[{ yAxis: norm.low }, { yAxis: norm.high }]],
            }
          : undefined,
        markLine: norm
          ? {
              silent: true,
              symbol: "none",
              lineStyle: { type: "dashed", color: "#22c55e" },
              label: { formatter: "median" },
              data: [{ yAxis: norm.median }],
            }
          : undefined,
      },
    ],
  }

  return (
    <Card className="gap-3 p-5">
      {header}
      <Chart option={option} height={280} />
      <p className="text-xs text-muted-foreground">
        {norm
          ? `Зелёная полоса: личная норма ${norm.low}–${norm.high} ${metric.unit}`
          : `${metric.label}, ${metric.unit}`}
      </p>
    </Card>
  )
}