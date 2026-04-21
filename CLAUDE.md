# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Demo repository for a talk on the [RandomizedTesting](https://github.com/randomizedtesting/randomizedtesting) library. Java 21, Maven build.

## Commands

```bash
# Run all tests
mvn test

# Run tests with a fixed seed (reproducible)
mvn test -Djvm.args="-Dtests.seed=12345"

# Run tests with a specific locale
mvn test -Djvm.args="-Dtests.locale=fr-FR"
```

## Architecture

Single test class: `src/test/java/fr/pilato/talk/randomizedtesting/RandomizedTest.java`

Key design points:
- Maven Surefire is **disabled**; tests run via `com.carrotsearch.randomizedtesting:junit4-maven-plugin`
- The class uses `@RunWith(RandomizedRunner.class)` instead of default JUnit runner
- Locale is randomized per test run by default (`tests.locale=random`); override with `-Dtests.locale=<BCP47-tag>`
- Seeds are randomized by default; fix with `-Dtests.seed=<hex>` or `@Seed` annotation on a test method
- `@ThreadLeakFilters` + `FriendlyZombieFilter` inner class demonstrates how to whitelist known background threads
- `@Repeat(iterations = N)` runs a single test method N times with different seeds

## Test System Properties

| Property | Default | Effect |
|---|---|---|
| `tests.seed` | random | Fix global seed for reproducible run |
| `tests.locale` | `random` | Set JVM default Locale (BCP 47 tag, e.g. `fr-FR`) |
