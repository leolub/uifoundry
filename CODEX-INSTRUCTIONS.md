# CODEX-INSTRUCTIONS

## 1. Purpose

This file defines how Codex should collaborate with the user while developing this repository.

The project specification defines **what the product should become**. This file defines **how implementation work should be carried out on the user's local machine and how large each development step should be**.

The repository has already been scaffolded. Do not recreate or replace the project structure unless the user explicitly asks for it.

The user expects substantial implementation assistance. Codex should actively write production-quality code, connect modules, fix implementation issues, add tests, and complete coherent features. The user is not expected to manually write most of the code.

At the same time, the user wants to understand the project, inspect changes, run it locally, make occasional manual edits, and commit/push at their own pace. Codex should therefore work in **feature-sized, verifiable increments**, not generate the entire project in one autonomous pass.

---

## 2. Default Collaboration Model

Use this default workflow:

1. Read the relevant specification and inspect the existing repository.
2. Inspect the current Git working tree so existing user changes are preserved.
3. Identify one coherent feature, module, or integration to implement.
4. Briefly state what will be implemented and any important dependency/environment implications.
5. Implement the feature completely enough to be locally testable.
6. It is acceptable to modify multiple related files across frontend, backend, database, and tests when the feature requires it.
7. Run relevant automated checks and fix ordinary implementation errors within the current feature scope.
8. Stop when the feature reaches a meaningful verification point.
9. Summarize what changed and tell the user how to test it locally.
10. Wait for the user to inspect, test, modify, commit/push, or request the next feature.

The user does **not** need approval prompts for every file edit, helper function, test, or normal project dependency.

The user **does** need a review opportunity before Codex moves into a new major feature, changes architecture, changes public API/database contracts, or modifies the development machine significantly.

---

## 3. Appropriate Size of an Implementation Step

Do not make steps artificially tiny.

A good default unit of work is **one coherent feature or module that can be tested independently**.

Examples of appropriate single steps:

- implement image upload end-to-end, including frontend input, backend endpoint, validation, storage abstraction, and tests;
- implement email/password registration and login with JWT, including backend security configuration and frontend integration;
- implement the initial AI generation endpoint and connect it to the frontend generation flow;
- implement project persistence with create/list/open/update operations;
- integrate Monaco Editor with the generated file model;
- integrate Sandpack preview with generated React files;
- implement website URL input and connect it to the screenshot service;
- implement Figma frame import;
- implement generation history/version restore;
- implement ZIP export.

These may require multiple files and are allowed to be completed as one task.

A step is too large when it combines several largely independent features, for example:

```text
Authentication + AI generation + Figma import + version history + deployment
```

Do not implement an entire multi-feature phase merely because all items appear under the same phase heading in the planning document.

The goal is:

```text
one coherent feature
→ working local state
→ user verification
→ next feature
```

not:

```text
one tiny function
→ ask permission
→ another tiny function
→ ask permission
```

and not:

```text
entire application
→ user sees hundreds of unfamiliar changes at once
```

---

## 4. Codex Should Provide Strong Implementation Assistance

The user may understand code better than they can write it from scratch quickly. Codex should therefore be proactive within the agreed feature scope.

Codex may and should:

- write complete classes, components, services, controllers, hooks, utilities, tests, configuration, and migrations;
- connect frontend and backend pieces required by the feature;
- choose ordinary implementation details when the specification does not require a particular choice;
- create reasonable validation and error handling;
- write boilerplate that would otherwise consume unnecessary user time;
- diagnose compilation/runtime/test errors caused by the current implementation;
- make follow-up fixes necessary to get the current feature working;
- add or update project-local dependencies required for the current feature;
- improve code structure when needed for the feature being implemented;
- explain unfamiliar or important code when reporting the result.

Do not force the user to manually fill in obvious boilerplate just for the sake of participation.

The user may still choose to write or modify some code manually. Preserve those changes and integrate around them.

---

## 5. When to Stop and Let the User Check

Stop for user verification when any of the following is true:

- the requested feature works end-to-end;
- a meaningful UI/module is now runnable locally;
- a database/API integration has reached a testable state;
- a new external service integration is functional;
- the next work would begin a different major feature;
- the next work would substantially expand scope;
- a design decision has become ambiguous enough to affect architecture or product behavior.

At that point, report:

- what was implemented;
- important files changed;
- dependencies added or changed;
- tests/checks run;
- any known limitations;
- exact local verification steps;
- a suggested commit message if useful.

Then wait for the user.

Do not automatically proceed into the next major feature.

---

## 6. User Testing and Manual Editing Are Expected

The user intends to frequently:

- run the frontend/backend locally;
- inspect the UI;
- test individual functions or modules;
- read generated code;
- ask questions about unfamiliar code;
- make occasional manual edits;
- commit and push when a checkpoint feels stable;
- sometimes make extra commits while validating or fixing a feature.

Support this workflow.

Do not assume one commit per project phase.

A phase may contain several commits, and a feature may also receive multiple commits while being validated.

When the user has made manual changes, treat them as intentional unless clearly broken or the user asks to replace them.

---

## 7. Git Safety and Ownership

Before substantial edits, inspect the working tree:

```bash
git status --short
```

Use `git diff` when useful to understand existing uncommitted work.

Preserve existing user changes.

Codex must not automatically:

- commit;
- push;
- force push;
- reset user work;
- rewrite history;
- delete branches;
- discard uncommitted changes.

The user controls commits and pushes.

Codex may suggest a commit message after a completed feature, for example:

```text
feat: add screenshot upload and validation flow
```

Forbidden unless explicitly requested:

```text
git reset --hard
git clean -fd
force push
history rewriting
```

---

## 8. Existing Local Environment Takes Priority

This is an existing Windows development machine with an established Java/Maven/Node development environment.

Do not assume that version numbers in project documentation must be installed exactly.

Treat specification versions as recommended or known-compatible unless a specific version is truly required.

When environment compatibility matters, inspect existing tools first:

```powershell
java -version
javac -version
mvn -version
node -v
npm -v
git --version
docker --version
docker compose version
```

When useful, inspect resolved executables:

```powershell
where.exe java
where.exe javac
where.exe mvn
where.exe node
where.exe npm
where.exe git
where.exe docker
```

If Java 17 is already installed and the chosen Spring Boot/dependency versions support Java 17, use Java 17.

Do not install Java 21 merely because a project document or template mentions Java 21.

Apply the same principle to Maven, Node.js, npm, Git, Docker, and other development tools.

Compatibility is more important than arbitrary version matching.

---

## 9. What Codex May Install Without Asking First

Normal **project-local dependencies** required for the currently requested feature may be added without a separate approval prompt.

Examples:

- Maven dependencies in `pom.xml`;
- npm dependencies in `package.json`;
- test libraries;
- small frontend/backend libraries;
- normal project build artifacts.

Codex may run normal project commands such as:

```text
mvn test
mvn package
npm install
npm run dev
npm run build
npm test
```

when appropriate.

However, dependencies should only be added when they serve a real project requirement. Avoid unnecessary libraries and dependency duplication.

At the end of the task, report newly added dependencies.

---

## 10. What Requires User Review Before Installation or Change

Stop and ask before making **machine-level, global, large, or potentially disruptive changes**, including:

- installing another JDK;
- upgrading/replacing the existing JDK;
- installing or replacing Maven itself;
- installing or replacing Node.js/npm globally;
- installing Docker Desktop;
- enabling/installing WSL;
- installing a local database server;
- changing system-wide PATH variables;
- installing a large browser runtime that is not clearly necessary;
- downloading local AI model weights;
- downloading multi-gigabyte tools/data;
- changing Docker Desktop/WSL storage configuration;
- modifying global Maven/npm configuration;
- any change likely to affect other existing projects on the machine.

When such a change appears necessary:

1. inspect the current environment;
2. explain why the current environment is insufficient;
3. propose the smallest solution;
4. mention approximate disk impact when relevant;
5. stop and wait for approval.

Do not turn ordinary project-local dependency installation into an approval-heavy process.

---

## 11. C: Drive Space Constraint

The Windows C: drive has extremely limited free space.

The project is located on a non-system drive and should remain there.

Avoid intentionally storing large caches, repositories, downloads, temporary data, browser binaries, Docker assets, or model files on C: when a safe project-local or non-system-drive alternative exists.

Prefer locations under the project or another non-system drive, for example:

```text
<project>/
├── .cache/
├── .m2/
├── .npm-cache/
├── temp/
├── storage/
└── logs/
```

Add generated/cache directories to `.gitignore` where appropriate.

Do not silently change global settings just to redirect caches. Prefer project-scoped configuration.

### Maven

The machine already has Maven. Reuse it if compatible.

When practical, project builds may use a repository outside C:, for example:

```powershell
mvn -Dmaven.repo.local=.m2/repository test
```

Do not modify the user's global Maven `settings.xml` without asking.

### npm

Keep packages project-local through `node_modules`.

Avoid global npm installs when a project-local dependency works.

A project-scoped npm cache may be used if useful.

### Playwright / browser binaries

Do not automatically download large local browser binaries if this project is using a remote browser/screenshot service and they are unnecessary.

If local browser binaries are required later, place them outside C: where practical and report the expected download size first if it is substantial.

### Docker

Do not reinstall Docker if it is already installed.

Avoid unnecessary large image pulls.

If Docker/WSL storage on C: becomes a serious issue, report it and let the user decide how to handle it.

---

## 12. Architecture and Specification Discipline

Codex has freedom over normal implementation details, but not over major product architecture.

Do not silently change major decisions such as:

- React + TypeScript frontend;
- Spring Boot backend;
- PostgreSQL persistence;
- the selected AI-provider abstraction;
- Monaco/Sandpack workspace direction;
- authentication model;
- public API contracts already frozen in specification;
- database relationships already frozen in specification;
- deployment architecture;
- supported input-source categories.

If a specification choice creates a real technical problem, explain the issue and propose an alternative before changing it.

Small implementation adjustments that preserve the intended architecture are allowed.

---

## 13. API and Database Changes

Ordinary implementation of already-specified endpoints/tables does not require permission.

However, if Codex wants to materially change an already-established API contract or database model, stop and explain the change first.

Examples that require review:

- renaming/removing public endpoints;
- changing authentication semantics;
- changing ownership/relationship models;
- replacing PostgreSQL with another database;
- introducing a new major persistence system;
- changing generation/version semantics.

Adding internal fields, indexes, constraints, or helper endpoints that are clearly necessary may be done when they preserve the documented behavior; report them afterward.

---

## 14. Error-Fixing Policy

Codex should actively fix errors caused by the current feature instead of stopping at the first compilation/test/runtime failure.

Within the current feature scope, Codex may:

- inspect logs;
- fix compile errors;
- fix type errors;
- adjust imports;
- fix tests;
- correct ordinary configuration mistakes;
- refine API/client integration;
- resolve dependency conflicts at the project level;
- retry the relevant tests/build.

Do not use broad environment upgrades as a generic troubleshooting strategy.

For example, do not respond to a build problem by simultaneously upgrading Java, Maven, Spring Boot, Node, npm, and multiple dependencies.

Diagnose the actual cause and make the smallest appropriate fix.

---

## 15. Code Quality Expectations

Generated code should be understandable enough for the user to review and explain later.

Prefer:

- clear naming;
- straightforward control flow;
- conventional Spring Boot and React patterns;
- focused classes/components;
- comments only when they clarify non-obvious behavior;
- explicit error handling;
- testable boundaries;
- minimal unnecessary abstraction.

Avoid clever or over-engineered patterns that make the project difficult to understand.

Do not introduce microservices, event buses, complex design patterns, or infrastructure solely to make the project look more sophisticated.

This is a portfolio-quality full-stack project, not a demonstration of maximum architectural complexity.

---

## 16. Explain Important Code, Not Every Line

The user wants to understand the project but does not need a tutorial after every edit.

At the end of a feature, briefly explain:

- the main flow;
- important classes/components;
- any new concept that is likely unfamiliar;
- where the user should look first when reading the code.

If the user asks a detailed coding question later, explain it in more depth then.

Do not flood the user with unnecessary line-by-line explanations unless requested.

---

## 17. Local Verification Handoff

After implementing a feature, provide concrete verification steps whenever possible.

Example:

```text
Implemented: Screenshot upload flow

Changed:
- frontend/src/...
- backend/src/...
- pom.xml

Added dependency:
- <dependency>

Checks run:
- mvn test
- npm run build

Local verification:
1. Start backend with ...
2. Start frontend with ...
3. Open ...
4. Upload PNG/JPEG/WebP.
5. Verify preview appears and invalid files show an error.

Known limitation:
- Files are currently stored locally; R2 integration comes later.

Suggested commit:
feat: implement screenshot upload flow
```

Then stop and allow the user to test or ask questions before starting a different feature.

---

## 18. Do Not Automatically Commit or Push

Even when a feature is complete and all tests pass, leave committing and pushing to the user unless explicitly instructed otherwise.

The user may commit frequently during experimentation and verification. That is expected.

Codex should not optimize for a perfectly clean one-commit-per-feature history at the expense of the user's learning/testing workflow.

---

## 19. Existing Project Scaffold

The project skeleton already exists.

Before adding new infrastructure:

- inspect existing `pom.xml`;
- inspect existing `package.json`;
- inspect current source directories;
- inspect current configuration;
- reuse existing setup where reasonable.

Do not regenerate Spring Boot or Vite projects simply because a preferred template differs from the existing scaffold.

Do not replace configuration wholesale when a targeted edit is sufficient.

---

## 20. Decision Priority

When deciding what to do, use this priority order:

1. Preserve user work and machine safety.
2. Follow the frozen product/architecture specification.
3. Reuse compatible existing local tools.
4. Complete the currently requested feature end-to-end.
5. Keep the result understandable and testable.
6. Avoid unnecessary dependencies and complexity.
7. Stop at a meaningful user-verification point before moving into another major feature.

---

## 21. Short Version

If anything in this document is unclear, follow this principle:

> Be a strong implementation partner, not a passive code tutor and not an autonomous whole-project generator.
>
> Write substantial code when needed. Complete one coherent feature at a time. Fix ordinary issues within that feature. Let the user inspect and test before moving to the next major feature. Reuse the user's existing environment whenever compatible, protect the space-constrained C: drive, and ask before making global, large, destructive, or architectural changes.
