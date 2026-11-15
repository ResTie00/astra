import { useEffect, useState } from "react"
import { ActionCard } from "@/components/ActionCard"
import { AlertBanner } from "@/components/AlertBanner"
import { AlertsList } from "@/components/AlertsList"
import { AreaGrid } from "@/components/AreaGrid"
import { AttentionCard } from "@/components/AttentionCard"
import { BlindSpot } from "@/components/BlindSpot"
import { CheckinForm } from "@/components/CheckinForm"
import { CommDelay } from "@/components/CommDelay"
import { DoseBar } from "@/components/DoseBar"
import { DriversChart } from "@/components/DriversChart"
import { EnvironmentCard } from "@/components/EnvironmentCard"
import { MetricsRow } from "./MetricRow"
import { OverallHealth } from "@/components/OverallHealth"
import { ReplanPanel } from "@/components/ReplanPanel"
import { Schedule } from "@/components/Schedule"
import { SettingsPanel } from "@/components/SettingsPanel"
import { Sidebar } from "@/components/Sidebar"
import { TopBar } from "./Topbar"
import { TrendsChart } from "@/components/TrendsChart"
import { Skeleton } from "@/components/ui/skeleton"
import { useCrew } from "@/hooks/useCrew"
import type { Section } from "@/lib/sections"
import { useUi } from "@/store/ui"

export function Dashboard({ user }: { user: { crewId: string; name: string } }) {
  // --- хуки строго до первого return ---
  const { data: crew, isLoading } = useCrew()
  const setCrew = useUi((s) => s.setCrew)
  const [section, setSection] = useState<Section>("overview")

  // баннер и форма чекина читают выбранного астронавта из стора
  useEffect(() => {
    setCrew(user.crewId)
  }, [user.crewId, setCrew])

  if (isLoading) return <Skeleton className="m-6 h-64" />

  const current = crew?.find((c) => c.crewId === user.crewId)
  if (!current) {
    return <p className="p-6">Не удалось загрузить данные. Проверь VITE_API_URL и бэкенд.</p>
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-370 gap-6 p-4 sm:p-6">
      <Sidebar section={section} onChange={setSection} />

      <main className="min-w-0 flex-1 space-y-4">
        <AlertBanner />
        <TopBar name={user.name} overall={current.overall} onAvatar={() => setSection("settings")} />

        {section === "overview" && (
          <>
            <MetricsRow crew={current} />
            <div className="grid gap-4 xl:grid-cols-[1.7fr_1fr]">
              <TrendsChart crewId={current.crewId} />
              <OverallHealth crew={current} />
            </div>
            <div className="grid gap-4 lg:grid-cols-[1fr_320px]">
              <AttentionCard crew={current} onDetails={() => setSection("alerts")} />
              <CommDelay />
            </div>
          </>
        )}

        {section === "checkin" && <CheckinForm />}

        {section === "planning" && (
          <div className="space-y-6">
            <Schedule />
            <ReplanPanel />
          </div>
        )}

        {section === "analytics" && (
          <section className="grid gap-4 lg:grid-cols-3">
            <div className="space-y-4 lg:col-span-2">
              <AreaGrid areas={current.areas} />
              <DriversChart drivers={current.blindSpot.drivers} />
            </div>
            <div className="space-y-4">
              <BlindSpot data={current.blindSpot} />
              <DoseBar used={current.cumulativeDoseMilliSv} budget={current.doseBudgetMilliSv} />
              <ActionCard action={current.action} />
              <EnvironmentCard area={current.areas.find((a) => a.area === "ENVIRONMENT")} />
            </div>
          </section>
        )}

        {section === "alerts" && <AlertsList />}
        {section === "settings" && <SettingsPanel />}
      </main>
    </div>
  )
}