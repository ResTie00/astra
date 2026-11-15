import { useQuery } from "@tanstack/react-query"
import { getCrew } from "@/api/crew"

export const useCrew = () =>
  useQuery({
    queryKey: ["crew"],
    queryFn: getCrew,
    refetchInterval: 10_000,
  })