import { useQuery } from "@tanstack/react-query"
import { getTrends } from "@/api/trends"

export const useTrends = (crewId: string) =>
  useQuery({
    queryKey: ["trends", crewId],
    queryFn: () => getTrends(crewId),
    refetchInterval: 60_000,
  })