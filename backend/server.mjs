import { createServer } from "node:http";
import { randomUUID } from "node:crypto";

const port = Number(process.env.PORT || 8080);
const reports = [];
const actions = [];

const signals = {
  rainfallMmHr: 36,
  congestionPercent: 89,
  drainageCapacityPercent: 22,
  roadVulnerability: 90
};

function citizenScore(count, report = {}) {
  let score = count === 0 ? 0 : count === 1 ? 20 : count <= 3 ? 50 : count <= 6 ? 75 : 100;
  if (report.photo) score += 10;
  const situations = new Set(report.situations || []);
  if (situations.has("Vehicle stuck")) score += 15;
  if (situations.has("Person at risk")) score += 25;
  if (situations.has("Underpass flooding")) score += 15;
  if (situations.has("Water rising quickly")) score += 15;
  return Math.min(score, 100);
}

export function assess(input = {}) {
  const count = input.reportCount ?? Math.max(reports.length, 5);
  const citizen = citizenScore(count, input.report);
  const rain = input.rainfallMmHr ?? signals.rainfallMmHr;
  const congestion = input.congestionPercent ?? signals.congestionPercent;
  const capacity = input.drainageCapacityPercent ?? signals.drainageCapacityPercent;
  const road = input.roadVulnerability ?? signals.roadVulnerability;
  const weather = rain <= 5 ? 10 : rain <= 15 ? 35 : rain <= 30 ? 70 : 100;
  const traffic = congestion < 20 ? 10 : congestion < 40 ? 40 : congestion < 70 ? 70 : 100;
  const drainage = capacity > 70 ? 10 : capacity > 35 ? 40 : capacity > 10 ? 80 : 100;
  const score = Math.round(citizen * .30 + weather * .25 + traffic * .20 + drainage * .15 + road * .10);
  const severity = score < 30 ? "LOW" : score < 50 ? "MODERATE" : score < 70 ? "HIGH" : score < 85 ? "SEVERE" : "CRITICAL";
  const confidence = Math.min(96, 84 + (count >= 4 ? 10 : 0));
  return {
    score, severity, confidence,
    signals: { citizen, weather, traffic, drainage, road },
    reasoning: [
      `${count} citizen reports detected within 420 metres`,
      `Rainfall intensity reached ${rain} mm/hr`,
      `Traffic congestion reached ${congestion}%`,
      `Drainage capacity is reduced to ${capacity}%`
    ],
    predictedNextZone: { name: "G-10/2 Street 14", probability: 72, estimatedMinutes: 14 }
  };
}

const incident = () => ({
  id: "g10-underpass", name: "G-10 Underpass", exposure: 1240,
  assessment: assess(), signals,
  predictions: [
    { name: "G-10/2 Street 14", probability: 72, eta: "10–16 min" },
    { name: "G-9 Service Road", probability: 48, eta: "22–30 min" }
  ]
});

function json(response, status, body) {
  response.writeHead(status, {
    "content-type": "application/json; charset=utf-8",
    "access-control-allow-origin": "*",
    "access-control-allow-headers": "content-type",
    "access-control-allow-methods": "GET,POST,OPTIONS"
  });
  response.end(JSON.stringify(body));
}

async function bodyOf(request) {
  let raw = "";
  for await (const chunk of request) {
    raw += chunk;
    if (raw.length > 1_000_000) throw new Error("Payload too large");
  }
  return raw ? JSON.parse(raw) : {};
}

export const server = createServer(async (request, response) => {
  try {
    const url = new URL(request.url, `http://${request.headers.host}`);
    if (request.method === "OPTIONS") return json(response, 204, {});
    if (request.method === "GET" && url.pathname === "/health") return json(response, 200, { status: "ok", service: "raahat-api", mode: "deterministic" });
    if (request.method === "GET" && url.pathname === "/api/v1/incidents") return json(response, 200, { incidents: [incident()] });
    if (request.method === "GET" && url.pathname === "/api/v1/incidents/g10-underpass") return json(response, 200, incident());
    if (request.method === "POST" && url.pathname === "/api/v1/reports") {
      const payload = await bodyOf(request);
      if (!payload.location || !payload.waterLevel) return json(response, 400, { error: "location and waterLevel are required" });
      const report = { id: randomUUID(), createdAt: new Date().toISOString(), ...payload };
      reports.push(report);
      return json(response, 201, { report, assessment: assess({ report, reportCount: reports.length }) });
    }
    if (request.method === "POST" && url.pathname === "/api/v1/analyze") return json(response, 200, assess(await bodyOf(request)));
    if (request.method === "POST" && url.pathname === "/api/v1/responses/execute") {
      const payload = await bodyOf(request);
      const execution = { id: `RH-${2048 + actions.length}`, strategy: payload.strategy || "B", status: "EXECUTING", dispatch: { team: "D-07", etaMinutes: 9 }, residentsAlerted: 1240 };
      actions.push(execution);
      return json(response, 202, execution);
    }
    return json(response, 404, { error: "Route not found" });
  } catch (error) {
    return json(response, error instanceof SyntaxError ? 400 : 500, { error: error.message });
  }
});

if (process.env.NODE_ENV !== "test") {
  server.listen(port, "0.0.0.0", () => console.log(`RAAHAT API listening on http://0.0.0.0:${port}`));
}
