export function parseCo2(text: string) {
  const m = text.match(/CO2\s+([\d,]+)\s*ppm in module\s+(\S+)/)
  if (!m) return null
  return { ppm: Number(m[1].replace(/,/g, "")), module: m[2] }
}