import test, { after, before } from "node:test";
import assert from "node:assert/strict";

process.env.NODE_ENV = "test";
const { server, assess } = await import("../server.mjs");
let base;

before(async () => {
  await new Promise(resolve => server.listen(0, "127.0.0.1", resolve));
  base = `http://127.0.0.1:${server.address().port}`;
});
after(() => server.close());

test("health endpoint reports deterministic mode", async () => {
  const response = await fetch(`${base}/health`);
  assert.equal(response.status, 200);
  assert.deepEqual(await response.json(), { status: "ok", service: "raahat-api", mode: "deterministic" });
});

test("signal assessment is deterministic and bounded", () => {
  const result = assess({ reportCount: 5, report: { photo: true, situations: ["Underpass flooding", "Water rising quickly"] } });
  assert.equal(result.severity, "CRITICAL");
  assert.ok(result.score >= 85 && result.score <= 100);
  assert.ok(result.confidence >= 0 && result.confidence <= 100);
});

test("report ingestion validates and returns assessment", async () => {
  const invalid = await fetch(`${base}/api/v1/reports`, { method: "POST", headers: { "content-type": "application/json" }, body: "{}" });
  assert.equal(invalid.status, 400);
  const response = await fetch(`${base}/api/v1/reports`, { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify({ location: "G-10 Underpass", waterLevel: "Knee deep", situations: ["Underpass flooding"] }) });
  const body = await response.json();
  assert.equal(response.status, 201);
  assert.equal(body.report.location, "G-10 Underpass");
  assert.ok(body.assessment.reasoning.length >= 3);
});

test("response execution creates dispatch state", async () => {
  const response = await fetch(`${base}/api/v1/responses/execute`, { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify({ strategy: "B" }) });
  const body = await response.json();
  assert.equal(response.status, 202);
  assert.equal(body.dispatch.team, "D-07");
  assert.equal(body.residentsAlerted, 1240);
});
