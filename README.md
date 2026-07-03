# Airport Runway Simulation Dashboard

A full-stack portfolio project for configuring, running, and visualising airport runway traffic simulations. The application combines a Spring Boot simulation API with a React/Vite dashboard so scenario inputs, operational metrics, and runway activity can be explored from one interface.

This is an independent full-stack rebuild inspired by an airport runway simulation specification. The focus is on demonstrating clean domain modelling, deterministic simulation behaviour, API design, automated backend tests, and a polished frontend experience.

## Tech Stack

- Java 21
- Spring Boot 3
- Gradle
- JUnit 5
- React
- Vite
- CSS

## Features

- Configure simulation duration, inbound traffic, outbound traffic, runway setup, disruption thresholds, and deterministic random seeds.
- Model inbound holding patterns, outbound take-off queues, runway allocation, cancellations, diversions, and emergency prioritisation.
- Return summary metrics for queue sizes, waiting times, delays, runway activity, cancellations, and diversions.
- Visualise simulation results through a responsive React dashboard.
- Preserve repeatable simulation runs with seed-driven scenario generation.

## Architecture Overview

The backend exposes a REST API for running simulations from scenario configuration payloads. The simulation domain is organised around aircraft, runways, scenario configuration, mutable simulation state, event processing, and result DTOs.

The simulation engine uses a discrete event-oriented structure. Scheduled aircraft are generated at the start of a run, then simulation events update shared state over time for arrivals, departures, runway usage, cancellations, diversions, and metric collection.

The frontend is a Vite-powered React application. It provides scenario configuration screens, submits requests to the backend API, and renders the resulting operational metrics and runway utilisation views.

## Running the Backend

```bash
cd backend
./gradlew bootRun
```

## Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

## Running Tests

```bash
cd backend
./gradlew clean test
```

## Testing Summary

Backend tests cover deterministic simulation behaviour, queue and runway domain logic, aircraft state transitions, disruption handling, validation of invalid scenarios, and sanity checks for zero-traffic and active-traffic runs.

## CV Summary

- Built a full-stack airport runway simulation dashboard with Spring Boot, Java 21, React, and Vite.
- Designed a deterministic simulation engine for runway allocation, aircraft queues, cancellations, diversions, and emergency prioritisation.
- Exposed simulation workflows through a REST API and visualised operational metrics in a responsive dashboard.
- Added JUnit coverage for domain behaviour, repeatability, validation, and disruption scenarios.
