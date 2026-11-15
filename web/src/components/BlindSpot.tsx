import { Badge } from "@/components/ui/badge"
import { Card } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import type { CrewStatus } from "@/types"

function ScoreRow({ label, value }: { label: string; value: number }) {
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-sm">
        <span>{label}</span>
        <span className="tabular-nums">{value.toFixed(1)} / 5</span>
      </div>
      <Progress value={(value / 5) * 100} />
    </div>
  )
}

export function BlindSpot({ data }: { data: CrewStatus["blindSpot"] }) {
  return (
    <Card className="gap-3 p-4 w-full">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold">Слепое пятно</h3>
        {data.flagged ? (
          <Badge variant="destructive">Расхождение</Badge>
        ) : (
          <Badge variant="secondary">В норме</Badge>
        )}
      </div>

      <ScoreRow label="Самооценка" value={data.selfRating} />
      <ScoreRow label="Объективно" value={data.objectiveScore} />
      <p className="text-sm text-muted-foreground">
        Разрыв: {data.gap > 0 ? "+" : ""}
        {data.gap.toFixed(1)}
      </p>

      {data.drivers.length > 0 && (
        <ul className="list-disc space-y-1 pl-5 text-sm">
          {data.drivers.map((d) => (
            <li key={d.metric}>{d.text}</li>
          ))}
        </ul>
      )}
    </Card>
  )
}