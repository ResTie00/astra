import { useQuery } from "@tanstack/react-query"
import { getAlerts } from "@/api/alerts"

export const useAlerts = () =>
  useQuery({ queryKey: ["alerts"], queryFn: getAlerts, refetchInterval: 10_000 })