
// import ReactEChartsCoreModule from "echarts-for-react/lib/core"

// // достаём компонент из обёртки, если она есть
// const ReactEChartsCore =
//   (ReactEChartsCoreModule as unknown as { default?: typeof ReactEChartsCoreModule }).default ??
//   ReactEChartsCoreModule
// import * as echarts from "echarts/core"
// import { LineChart } from "echarts/charts"
// import { GridComponent, TooltipComponent } from "echarts/components"
// import { CanvasRenderer } from "echarts/renderers"
// import type { EChartsOption } from "echarts"
// import { Card } from "@/components/ui/card"
// import { Skeleton } from "@/components/ui/skeleton"
// import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
// import { useFeed } from "@/hooks/useFeed"
// import { useUi } from "@/store/ui"
// import type { TelemetryPayload } from "@/types"

// echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

// const METRICS = [
//   { key: "HEART_RATE", label: "Пульс", unit: "уд/мин" },
//   { key: "HRV_RMSSD", label: "HRV", unit: "мс" },
//   { key: "SPO2", label: "SpO₂", unit: "%" },
//   { key: "BODY_TEMP", label: "Температура", unit: "°C" },
//   { key: "DOSE_RATE", label: "Мощность дозы", unit: "мкЗв/ч" },
//   { key: "SLEEP_HOURS", label: "Сон", unit: "ч" },
// ]

// export function TelemetryChart() {
//   const crewId = useUi((s) => s.selectedCrewId)
//   const metric = useUi((s) => s.selectedMetric)
//   const setMetric = useUi((s) => s.setMetric)
//   const { data, isLoading } = useFeed<TelemetryPayload>({ type: "TELEMETRY", crewId, metric })

//   const current = METRICS.find((m) => m.key === metric)

//   const option: EChartsOption = {
//     grid: { left: 44, right: 16, top: 16, bottom: 28 },
//     tooltip: { trigger: "axis" },
//     xAxis: { type: "time" },
//     yAxis: { type: "value", scale: true, name: current?.unit },
//     series: [
//       {
//         type: "line",
//         smooth: true,
//         showSymbol: (data?.length ?? 0) < 3,
//         data: (data ?? []).map((d) => [d.timestamp, d.payload.value]),
//       },
//     ],
//   }

//   return (
//     <Card className="gap-4 p-4 m-0 w-6/10">
//       <div className="flex flex-wrap items-center justify-between gap-2">
//         <h3 className="font-semibold">Телеметрия</h3>
//         <ToggleGroup
//           type="single"
//           variant="outline"
//           size="sm"
//           value={metric}
//           onValueChange={(v) => v && setMetric(v)}
//           className="flex-wrap"
//         >
//           {METRICS.map((m) => (
//             <ToggleGroupItem key={m.key} value={m.key}>
//               {m.label}
//             </ToggleGroupItem>
//           ))}
//         </ToggleGroup>
//       </div>

//       {isLoading ? (
//         <Skeleton className="h-72 w-full" />
//       ) : !data?.length ? (
//         <p className="py-16 text-center text-sm text-muted-foreground">
//           Нет данных по этой метрике
//         </p>
//       ) : (
//         <ReactEChartsCore echarts={echarts} option={option} style={{ height: 288 }} notMerge />
//       )}
//     </Card>
//   )
// }