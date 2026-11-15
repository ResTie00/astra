import { Card } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { cn } from "@/lib/utils"

export function DoseBar({ used, budget }: { used: number; budget: number }) {
  const pct = Math.min(100, (used / budget) * 100)
  const color =
    pct >= 85
      ? "*:data-[slot=progress-indicator]:bg-red-500"
      : pct >= 65
        ? "*:data-[slot=progress-indicator]:bg-yellow-500"
        : ""

  return (
    <Card className="gap-3 p-4 w-full">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold">Доза радиации</h3>
        <span className="text-sm tabular-nums">
          {used.toFixed(1)} / {budget.toFixed(0)} мЗв
        </span>
      </div>
      <Progress value={pct} className={cn(color)} />
      <p className="text-sm text-muted-foreground">Использовано {Math.round(pct)}% бюджета миссии</p>
    </Card>
  )
}