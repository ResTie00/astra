import { useQuery } from "@tanstack/react-query";
import { getSpaceWeather } from "@/api/weather";

export const useSpaceWeather = () =>
  useQuery({
    queryKey: ["weather"],
    queryFn: getSpaceWeather,
    refetchInterval: 10_000,
  });
