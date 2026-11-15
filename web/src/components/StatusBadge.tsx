import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"
import type { Status } from "@/types"

const COLORS: Record<Status, string> = {
  NORMAL: "bg-green-600 text-white",
  WATCH: "bg-yellow-500 text-black",
  ACT: "bg-red-600 text-white",
}

export function StatusBadge({ status }: { status: Status }) {
  return <Badge className={cn("border-transparent", COLORS[status])}>{status}</Badge>
}