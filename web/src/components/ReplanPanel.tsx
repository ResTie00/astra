import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { useAcceptPlan } from "@/hooks/useAcceptPlan"
import { useStartReplan } from "@/hooks/useStartReplan"
import { formatDateTime, formatPercent } from "@/lib/format"
import { cn } from "@/lib/utils"
import { useUi } from "@/store/ui"
import type { Risk } from "@/types"

function RiskStats({ risk }: { risk: Risk }) {
  return (
    <div className="space-y-2 text-sm">
      <div className="grid grid-cols-3 gap-2">
        <div>
          <div className="text-xs text-muted-foreground">Риск</div>
          <div className="text-lg font-semibold tabular-nums">{risk.riskScore}</div>
        </div>
        <div>
          <div className="text-xs text-muted-foreground">Превышение дозы</div>
          <div className="text-lg font-semibold tabular-nums">
            {formatPercent(risk.crewExceedProbability)}
          </div>
        </div>
        <div>
          <div className="text-xs text-muted-foreground">Часы с ошибкой</div>
          <div className="text-lg font-semibold tabular-nums">{risk.impairedCriticalHours} ч</div>
        </div>
      </div>
      <ul className="space-y-1 text-muted-foreground">
        {risk.byCrew.map((c) => (
          <li key={c.crewId} className="flex justify-between">
            <span>{c.crewName}</span>
            <span className="tabular-nums">
              {c.doseP50MilliSv}–{c.doseP95MilliSv} мЗв · {formatPercent(c.exceedBudgetProbability)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}

export function ReplanPanel() {
  // --- все хуки строго до первого return ---
  const result = useUi((s) => s.replanResult)
  const selectedId = useUi((s) => s.selectedOptionId)
  const appliedId = useUi((s) => s.appliedOptionId)
  const select = useUi((s) => s.selectOption)
  const apply = useUi((s) => s.applyOption)

  const start = useStartReplan()
  const accept = useAcceptPlan()

  // --- состояние 1: расчёта ещё не было ---
  if (!result) {
    return (
      <Card className="gap-3 p-6">
        <h2 className="text-lg font-semibold">Перепланирование</h2>
        <p className="text-sm text-muted-foreground">
          Бэкенд предложит несколько вариантов плана с оценкой риска.
        </p>
        <div className="flex items-center gap-3">
          <Button onClick={() => start.mutate()} disabled={start.isPending}>
            {start.isPending ? "Считаю…" : "Рассчитать план"}
          </Button>
          {start.isError && (
            <span className="text-sm text-red-500">Не удалось рассчитать, попробуй ещё раз</span>
          )}
        </div>
      </Card>
    )
  }

  // --- состояние 2: результат есть ---
  const selected = result.options.find((o) => o.optionId === selectedId)
  const assignments = [...(selected?.plan.assignments ?? result.currentPlan.assignments)].sort(
    (a, b) => a.start.localeCompare(b.start),
  )

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-lg font-semibold">Перепланирование</h2>
        <div className="flex items-center gap-3">
          <span className="text-sm text-muted-foreground">
            {result.runId} · горизонт {result.horizonHours} ч
          </span>
          <Button
            size="sm"
            variant="outline"
            onClick={() => start.mutate()}
            disabled={start.isPending}
          >
            {start.isPending ? "Считаю…" : "Пересчитать"}
          </Button>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Card className="gap-3 p-4">
          <h3 className="font-semibold">Текущий план</h3>
          <RiskStats risk={result.currentRisk} />
        </Card>

        {result.options.map((o) => {
          const isSelected = o.optionId === selectedId
          const isApplied = o.optionId === appliedId
          return (
            <Card
              key={o.optionId}
              onClick={() => select(o.optionId)}
              className={cn("cursor-pointer gap-3 p-4", isSelected && "ring-2 ring-primary")}
            >
              <div className="flex items-center justify-between">
                <h3 className="font-semibold">{o.title}</h3>
                {isApplied ? <Badge>Применён</Badge> : <Badge variant="outline">{o.strategy}</Badge>}
              </div>
              <RiskStats risk={o.risk} />
              <ul className="list-disc space-y-1 pl-5 text-sm">
                {o.changes.map((c) => (
                  <li key={c.taskId}>{c.explanation}</li>
                ))}
              </ul>
              <Button
                size="sm"
                disabled={isApplied || accept.isPending}
                variant={isSelected ? "default" : "outline"}
                onClick={(e) => {
                  e.stopPropagation()
                  select(o.optionId)
                  accept.mutate(
                    { runId: result.runId, optionId: o.optionId },
                    { onSuccess: () => apply(o.optionId) },
                  )
                }}
              >
                {isApplied ? "Применён" : accept.isPending ? "Применяю…" : "Применить"}
              </Button>
            </Card>
          )
        })}
      </div>

      {accept.isError && (
        <p className="text-sm text-red-500">Не удалось применить план</p>
      )}

      <Card className="gap-2 p-4">
        <h3 className="font-semibold">
          {selected ? `План: ${selected.title}` : "Текущий план задач"}
        </h3>
        <ul className="divide-y text-sm">
          {assignments.map((a) => (
            <li key={a.taskId} className="flex flex-wrap justify-between gap-2 py-2">
              <span>
                {a.taskName} <span className="text-muted-foreground">· {a.crewName}</span>
              </span>
              <span className="tabular-nums text-muted-foreground">
                {formatDateTime(a.start)} → {formatDateTime(a.end)}
              </span>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  )
}