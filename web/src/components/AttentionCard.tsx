import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import type { CrewStatus } from "@/types"

const RANK = { ACT: 0, WATCH: 1, NORMAL: 2 }

export function AttentionCard({
  crew,
  onDetails,
}: {
  crew: CrewStatus
  onDetails: () => void
}) {
  // самая тяжёлая область (сначала ACT, потом WATCH)
  const worst = [...crew.areas].sort((a, b) => RANK[a.status] - RANK[b.status])[0]
  const hasIssue = worst && worst.status !== "NORMAL"

  return (
    <Card className="flex-row items-center justify-between gap-4 p-5">
      <div className="space-y-1">
        <h3 className={hasIssue ? "font-semibold text-orange-400" : "font-semibold text-emerald-400"}>
          {hasIssue ? "Attention Required" : "All clear"}
        </h3>
        <p className="text-sm text-muted-foreground">
          {hasIssue ? worst.explanation : "No action needed. Keep following your routine."}
        </p>
        {hasIssue && <p className="text-sm">{crew.action.text}</p>}
      </div>
      <Button variant="outline" className="shrink-0 rounded-full" onClick={onDetails}>
        View details
      </Button>
    </Card>
  )
}