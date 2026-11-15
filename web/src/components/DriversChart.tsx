import { Card } from "@/components/ui/card"
import { Chart } from "@/components/Chart"
import type { Driver } from "@/types"
import type { EChartsOption } from "echarts"

// пороги и границы оси: поправь, когда друг подтвердит единицы
const WARN_AT = 2
const CRIT_AT = 4
const AXIS_LIMIT = 6

const LABELS: Record<string, string> = {
  SLEEP_HOURS: "Сон",
  REACTION_TIME: "Реакция",
  HRV_RMSSD: "HRV",
}

const clamp = (v: number) => Math.max(-AXIS_LIMIT, Math.min(AXIS_LIMIT, v))

function barColor(deviation: number) {
  const abs = Math.abs(deviation)
  if (abs >= CRIT_AT) return "#ef4444"
  if (abs >= WARN_AT) return "#eab308"
  return "#22c55e"
}

export function DriversChart({ drivers }: { drivers: Driver[] }) {
  if (drivers.length === 0) {
    return (
      <Card className="gap-2 p-4">
        <h3 className="font-semibold">Отклонения от нормы</h3>
        <p className="text-sm text-muted-foreground">Отклонений нет</p>
      </Card>
    )
  }

  const option: EChartsOption = {
    textStyle: { color: "#a1a1aa" },
    grid: { left: 90, right: 24, top: 8, bottom: 24 },
    xAxis: {
      type: "value",
      min: -AXIS_LIMIT,
      max: AXIS_LIMIT,
      splitLine: { lineStyle: { color: "rgba(255,255,255,0.1)" } },
    },
    yAxis: {
      type: "category",
      inverse: true,
      data: drivers.map((d) => LABELS[d.metric] ?? d.metric),
      axisLine: { onZero: false },
      axisTick: { show: false },
    },
    series: [
      {
        type: "bar",
        barWidth: 18,
        data: drivers.map((d) => ({
          value: clamp(d.deviation),
          itemStyle: { color: barColor(d.deviation), borderRadius: 4 },
        })),
      },
    ],
  }

  return (
    <Card className="gap-3 p-4">
      <h3 className="font-semibold">Отклонения от нормы</h3>
      <Chart option={option} height={drivers.length * 56 + 40} />
      <ul className="space-y-1 text-sm">
        {drivers.map((d) => (
          <li key={d.metric} className="flex justify-between gap-3">
            <span>{d.text}</span>
            <span className="tabular-nums text-muted-foreground">{d.deviation.toFixed(1)}</span>
          </li>
        ))}
      </ul>
      <p className="text-xs text-muted-foreground">
        Влево: ниже нормы · вправо: выше нормы · значения за ±{AXIS_LIMIT} обрезаны на графике
      </p>
    </Card>
  )
}