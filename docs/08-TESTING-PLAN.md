# 08 — Testing Plan

## Philosophy
Test business-critical seams; do not chase artificial coverage percentage.

## Backend Unit Tests
Priority:
- auth service credential handling;
- registered and guest project ownership checks;
- input type validation;
- Figma URL parser;
- website URL SSRF validator;
- generated file validator;
- version creation/restore logic;
- guest/user/IP/global quota calculation;
- guest project expiration/cleanup selection;
- ZIP path safety.

Frameworks:
- JUnit 5
- Mockito

## Backend Integration Tests
Use PostgreSQL-compatible integration testing. Testcontainers is recommended for a small number of critical repository/API tests if local/CI setup is stable.

Required scenarios:
- register/login flow;
- guest project creation without auth;
- guest cookie ownership isolation;
- project create/read ownership;
- version uniqueness/order;
- draft update persistence.

Do not call real Gemini/Figma/Cloudflare APIs in automated CI tests.
Use mocked provider clients.

## Frontend Tests
Prioritize:
- auth form validation;
- input mode selection;
- project workspace file switching;
- AI refine form state;
- quota/BYOK state;
- version restore confirmation;
- API error display.

Use a lightweight React test setup. Exact runner can be chosen during scaffold and then frozen.

## Manual Acceptance Test Matrix
Before release test:
- PNG upload
- clipboard screenshot paste
- invalid file
- large file
- website URL success
- invalid/private URL rejection
- Figma valid frame
- Figma invalid token
- initial generation
- generation failure
- BYOK generation
- guest can generate without login
- guest fourth demo AI request is rejected
- registered-user demo quota exceeded
- expired guest project access rejected
- Monaco edit/autosave
- refinement
- manual version
- rollback
- ZIP install/run
- anonymous example access
- guest ZIP download without login
- unauthorized project access rejection

## External API Smoke Tests
Maintain manual scripts or Postman collection for:
- Gemini image -> structured code JSON
- Figma frame -> image URL
- Cloudflare Browser Run URL -> screenshot
- R2 upload/download

Never put real secrets in committed Postman environment files.
