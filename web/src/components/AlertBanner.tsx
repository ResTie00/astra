import { TriangleAlert } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { useAckAlert } from "@/hooks/useAckAlert"
import { useAlerts } from "@/hooks/useAlerts"
import { useSpaceWeather } from "@/hooks/useSpaceWeather"
import { formatDateTime } from "@/lib/format"
import { useAcked } from "@/store/alerts"
import { useUi } from "@/store/ui"

export function AlertBanner() {
  const crewId = useUi((s) => s.selectedCrewId)
  const acked = useAcked((s) => s.ids)
  const { data: alerts } = useAlerts()
  const { data: weather } = useSpaceWeather()
  const ack = useAckAlert()

  const critical = alerts
    ?.filter(
      (a) =>
        a.severity === "CRITICAL" &&
        !acked.includes(a.id) &&
        (!a.crewId || a.crewId === crewId),
    )
    .at(-1)

  if (!critical) return null

  return (
    <Alert variant="destructive">
      <TriangleAlert />
      <AlertTitle>Critical Alert</AlertTitle>
      <AlertDescription>
        <p>{critical.message}</p>
        {weather && (
          <p>
            Начало: {formatDateTime(weather.onset)} · пик: {formatDateTime(weather.peak)}
          </p>
        )}
        <Button
          size="sm"
          variant="outline"
          disabled={ack.isPending}
          onClick={() => ack.mutate(critical.id)}
        >
          {ack.isPending ? "Принимаю…" : "Принять"}
        </Button>
      </AlertDescription>
    </Alert>
  )
}