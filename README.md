# OpenAPI Security Scanner

Static security analysis tool that audits OpenAPI specifications for authentication gaps and risk assessment.

## What It Does

Paste a URL to any public OpenAPI (Swagger) spec and the scanner fetches it, parses every endpoint, checks whether authentication is defined (globally or per-operation), scores unprotected endpoints by risk level, and returns a structured report with warnings, per-endpoint risk ratings, and an overall security assessment.

## Key Features

- **SSRF-safe fetching** — spec URLs are resolved through a DNS-pinning validator that blocks cloud metadata IPs and disables redirects
- **Authentication detection** — checks global security schemes, operation-level overrides, and common auth header/query patterns (`Authorization`, `X-API-Key`, `access_token`, etc.)
- **Risk scoring engine** — weights endpoints by HTTP method sensitivity (TRACE/DELETE/PUT > POST > GET) and path keywords (`/admin`, `/payment`, `/wallet`, etc.)
- **Spec quality warnings** — flags unused security schemes, HTTP-only servers, and missing global security

## Tech Stack

| Layer    | Technology                                                    |
|----------|---------------------------------------------------------------|
| Backend  | Java 17, Spring Boot 3.5, Swagger Parser 2.1, Lombok          |
| Frontend | React 19, TypeScript 5.9, Vite 7.3                            |
| Infra    | Docker (Eclipse Temurin 21 Alpine), Maven, Spring Actuator    |

## Architecture

```
┌─────────────┐        POST /api/scan/validate         ┌──────────────────┐
│   React UI  │ ─────────────────────────────────▶    │  ScanController  │
│  (Vite dev) │ ◀─────────────────────────────────    │                  │
│  :5173      │        JSON ResponseDto               └────────┬─────────┘
└─────────────┘                                                │
                                                               ▼
                                                        ┌─────────────┐
                                                        │ ScanService │  (orchestrator)
                                                        └──────┬──────┘
                                          ┌─────────────┬──────┴───────┬────────────────┐
                                          ▼             ▼              ▼                ▼
                                   UrlValidator   OpenApiParser  SecurityAnalyzer  ReportBuilder
                                   (SSRF guard)   (fetch+parse)  (auth+risk)      (score+warn)
                                          │             │
                                          └──────┬──────┘
                                                 ▼
                                          Remote OpenAPI
                                          spec (JSON/YAML)
```

**Flow:** User submits a spec URL → `UrlValidator` resolves DNS and blocks private IPs → `OpenApiParser` fetches the spec through the safe connection and parses it with Swagger Parser → `SecurityAnalyzer` walks every endpoint checking global/operation-level auth and scoring risk → `ReportBuilder` aggregates results, sorts by risk, and generates warnings → JSON response returned to the React frontend.
