import { Wind } from "lucide-react"
import { Card } from "@/components/ui/card"
import { StatusBadge } from "@/components/StatusBadge"
import { parseCo2 } from "@/lib/parse"
import type { Area } from "@/types"

export function EnvironmentCard({ area }: { area?: Area }) {
  if (!area) return null
  const co2 = parseCo2(area.explanation)

  return (
    <Card className="gap-2 p-4">
      <div className="flex items-center justify-between">
        <h3 className="flex items-center gap-2 font-semibold">
          <Wind className="size-4" /> Среда
        </h3>
        <StatusBadge status={area.status} />
      </div>

      {co2 ? (
        <>
          <p className="text-3xl font-bold tabular-nums">
            {co2.ppm.toLocaleString("ru-RU")}{" "}
            <span className="text-base font-normal text-muted-foreground">ppm CO₂</span>
          </p>
          <p className="text-sm text-muted-foreground">Модуль {co2.module}</p>
        </>
      ) : (
        <p className="text-sm">{area.explanation}</p>
      )}
    </Card>
  )
}