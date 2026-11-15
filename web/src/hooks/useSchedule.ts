import { useQuery } from "@tanstack/react-query"
import { getSchedule } from "@/api/schedule"

export const useSchedule = () =>
  useQuery({ queryKey: ["schedule"], queryFn: getSchedule, refetchInterval: 10_000 })