# RAAHAT API

Zero-dependency Node.js backend for the RAAHAT AI demo. It provides deterministic incident ingestion, signal fusion, forecast data, and response execution while remaining ready for Firebase/Gemini adapters.

```bash
cd backend
npm test
npm start
```

The API listens on `http://localhost:8080`. Android emulators reach it at `http://10.0.2.2:8080`.

| Method | Route | Purpose |
|---|---|---|
| GET | `/health` | Service readiness |
| GET | `/api/v1/incidents` | Active incidents |
| GET | `/api/v1/incidents/g10-underpass` | Digital Twin detail |
| POST | `/api/v1/reports` | Validate and ingest citizen report |
| POST | `/api/v1/analyze` | Deterministic signal assessment |
| POST | `/api/v1/responses/execute` | Orchestrate response and dispatch |
