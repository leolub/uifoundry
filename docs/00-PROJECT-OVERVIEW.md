# 00 — Project Overview

## Product
**UIFoundry** is a full-stack design-to-code workspace that converts screenshots, website URLs, Figma frames, and wireframes into runnable React + TypeScript + Tailwind code.

The differentiator is not simply an LLM call. The application owns the full workflow around the model:
- multi-source input normalization;
- guest trial flow plus account/project persistence;
- constrained AI output;
- executable browser preview;
- editor state;
- iterative refinement;
- immutable version history;
- exportable project artifacts;
- external API and usage-limit management.

## Resume Goal
This project is intended to replace a generic CRUD/blog project and provide concrete project evidence for:
- Java
- Spring Boot
- Spring Security
- REST APIs
- React
- TypeScript
- Vite
- PostgreSQL
- Docker
- GitHub Actions

It also introduces a small number of useful additions:
- Gemini multimodal API
- Monaco Editor
- Sandpack
- Figma REST API
- Cloudflare R2
- hosted browser screenshot API

## Target User
A developer or designer who has a visual reference and wants a runnable React starting point.

## Core User Story
> As a developer, I can provide a screenshot, website URL, Figma frame, or wireframe, generate React/TS/Tailwind code, inspect and edit the result, ask AI for revisions, restore older versions, and download a runnable project.

## Product Principles
1. **Fast completion over feature breadth.**
2. **One code target, many lightweight inputs.**
3. **AI output must be constrained and executable.**
4. **Backend owns application state; AI is one dependency, not the application itself.**
5. **Public deployment must let a recruiter try the real workflow without registration while strictly bounding owner-funded/free-tier API usage.**
6. **UI is a serious tool interface first, styled product second.**
