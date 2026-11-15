import { Card } from "@/components/ui/card"
import { cn } from "@/lib/utils"

export type Tone = "good" | "warn" | "low" | "bad" | "none"

const TONE: Record<Tone, string> = {
  good: "border-emerald-500/40 bg-emerald-900/50 text-emerald-300",
  warn: "border-yellow-500/40 bg-yellow-900/40 text-yellow-300",
  low: "border-blue-500/40 bg-blue-900/50 text-blue-300",
  bad: "border-red-500/40 bg-red-900/50 text-red-300",
  none: "border-white/10 bg-white/5 text-muted-foreground",
}

export function MetricCard({
  title,
  value,
  unit,
  label,
  tone,
}: {
  title: string
  value: string
  unit: string
  label: string
  tone: Tone
}) {
  return (
    <Card className="items-start gap-3 p-5">
      <p className="text-sm text-muted-foreground">{title}</p>
      <p className="text-3xl font-semibold tabular-nums">
        {value}
        <span className="ml-1 text-sm font-normal text-muted-foreground">{unit}</span>
      </p>
      <span className={cn("rounded-full border px-3 py-0.5 text-xs", TONE[tone])}>{label}</span>
    </Card>
  )
}