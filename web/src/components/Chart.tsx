import ReactEChartsCoreModule from "echarts-for-react/lib/core"
import * as echarts from "echarts/core"
import { BarChart, LineChart } from "echarts/charts"
import {
  GridComponent,
  MarkAreaComponent,
  MarkLineComponent,
  TooltipComponent,
} from "echarts/components"
import { CanvasRenderer } from "echarts/renderers"
import type { EChartsOption } from "echarts"

echarts.use([
  BarChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  MarkAreaComponent,
  MarkLineComponent,
  CanvasRenderer,
])

// пакет echarts-for-react иногда отдаёт обёртку { default: Компонент }
const ReactEChartsCore =
  (ReactEChartsCoreModule as unknown as { default?: typeof ReactEChartsCoreModule }).default ??
  ReactEChartsCoreModule

export function Chart({ option, height }: { option: EChartsOption; height: number }) {
  return <ReactEChartsCore echarts={echarts} option={option} notMerge style={{ height }} />
}