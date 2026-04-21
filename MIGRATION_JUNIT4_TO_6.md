# Migration from JUnit 4 to JUnit 6

This document captures all changes required to migrate the `randomizedtesting-demo` project
from JUnit 4 + `randomizedtesting-runner` to JUnit 6 + `randomizedtesting-jupiter`.

---

## Maven (`pom.xml`)

### 1. Add the JUnit BOM

Add a `dependencyManagement` section to import the JUnit BOM so that all JUnit artifact
versions are managed centrally:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>6.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. Replace the randomizedtesting dependency

| Before                                                              | After                                                                |
| ------------------------------------------------------------------- | -------------------------------------------------------------------- |
| `com.carrotsearch.randomizedtesting:randomizedtesting-runner:2.8.4` | `com.carrotsearch.randomizedtesting:randomizedtesting-jupiter:0.2.0` |

The JUnit 4-specific runner module is replaced by the new JUnit Jupiter extension module.

### 3. Replace the JUnit dependency

| Before                                  | After                                                |
| --------------------------------------- | ---------------------------------------------------- |
| `junit:junit:4.13.2` (explicit version) | `org.junit.jupiter:junit-jupiter` (version from BOM) |

### 4. Simplify the build plugin configuration

JUnit 4 required disabling the default Surefire execution and wiring a dedicated
`junit4-maven-plugin` instead. With JUnit 6, Surefire natively discovers and runs
JUnit Jupiter tests — no custom plugin configuration is needed.

**Before:**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.5</version>
    <executions>
        <execution>
            <id>default-test</id>
            <phase>none</phase>   <!-- disable Surefire -->
        </execution>
    </executions>
</plugin>

<plugin>
    <groupId>com.carrotsearch.randomizedtesting</groupId>
    <artifactId>junit4-maven-plugin</artifactId>
    <version>2.8.4</version>
    <executions>
        <execution>
            <id>unit-tests</id>
            <phase>test</phase>
            <goals><goal>junit4</goal></goals>
        </execution>
    </executions>
</plugin>
```

**After:**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.5</version>
</plugin>
```

---

## Test class (`RandomizedTest.java`)

### 1. Imports

All JUnit 4 and randomizedtesting JUnit 4 imports are replaced by their JUnit Jupiter
equivalents.

| Before                                                       | After                                                                                    |
| ------------------------------------------------------------ | ---------------------------------------------------------------------------------------- |
| `org.junit.*`                                                | `org.junit.jupiter.api.*`                                                                |
| `org.junit.rules.TestName`                                   | *(removed — see §4)*                                                                     |
| `org.junit.runner.RunWith`                                   | *(removed — see §2)*                                                                     |
| `com.carrotsearch.randomizedtesting.RandomizedRunner`        | *(removed — see §2)*                                                                     |
| `com.carrotsearch.randomizedtesting.ThreadFilter`            | `java.util.function.Predicate`                                                           |
| `com.carrotsearch.randomizedtesting.annotations.*`           | `com.carrotsearch.randomizedtesting.jupiter.*`                                           |
| `static com.carrotsearch.randomizedtesting.RandomizedTest.*` | explicit static imports from `com.carrotsearch.randomizedtesting.jupiter.RandomizedTest` |
| *(absent)*                                                   | `static org.junit.jupiter.api.Assumptions.assumeTrue`                                    |

### 2. Class-level annotations

The JUnit 4 `@RunWith` + `@ThreadLeakFilters` pair is replaced by the new Jupiter
annotations provided by `randomizedtesting-jupiter`.

**Before:**

```java
@RunWith(RandomizedRunner.class)
@ThreadLeakFilters(filters = {RandomizedTest.FriendlyZombieFilter.class})
public class RandomizedTest { ... }
```

**After:**

```java
@DetectThreadLeaks
@DetectThreadLeaks.ExcludeThreads({
        RandomizedTest.FriendlyZombieFilter.class,
        RandomizedTest.IntelliJThreadsFilter.class,
        SystemThreadFilter.class})
@Randomized
class RandomizedTest { ... }
```

Key differences:
- `@Randomized` replaces `@RunWith(RandomizedRunner.class)` as the extension entry point.
- `@DetectThreadLeaks` + `@DetectThreadLeaks.ExcludeThreads` replace `@ThreadLeakFilters`.
  The new annotation also accepts a built-in `SystemThreadFilter` to ignore JVM system threads.
- The class no longer needs to be `public` (JUnit 5/6 convention: package-private is fine).

### 3. Lifecycle annotations

| JUnit 4        | JUnit 5/6     |
| -------------- | ------------- |
| `@BeforeClass` | `@BeforeAll`  |
| `@AfterClass`  | `@AfterAll`   |
| `@Before`      | `@BeforeEach` |
| `@After`       | `@AfterEach`  |

Methods no longer need to be `public`.

### 4. Test name: `@Rule` / `TestName` replaced by `@BeforeEach` with `TestInfo`

JUnit 4 required a `@Rule` field to expose the current test name:

```java
@Rule
public TestName name = new TestName();

@Test
public void someTest() {
    LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
    // ...
}
```

In JUnit 5/6, `TestInfo` is injected directly as a lifecycle-method parameter.
A single `@BeforeEach` method now logs the test name for every test:

```java
@BeforeEach
void beforeEach(TestInfo testInfo) {
    LOGGER.info("🏁 Starting test [{}]", testInfo.getDisplayName());
}
```

The per-test logging lines are removed from all test methods.

### 5. Random injection: explicit `Random` parameter

The JUnit 4 integration provided random helpers as static no-arg methods inherited
from a base class. The Jupiter integration instead injects a `Random` instance as a
method parameter, making the seed traceable per invocation.

| Before                    | After                          |
| ------------------------- | ------------------------------ |
| `randomInt()`             | `randomInt(rnd)`               |
| `randomIntBetween(0, 10)` | `randomIntInRange(rnd, 0, 10)` |
| `randomBoolean()`         | `randomBoolean(rnd)`           |
| `randomLocale()`          | `randomLocale(rnd)`            |

The `Random rnd` parameter can be declared on any `@Test`, `@BeforeAll`, or
`@BeforeEach` method that needs randomness.

### 6. Seed annotation

| Before           | After               |
| ---------------- | ------------------- |
| `@Seed("12345")` | `@FixSeed("12345")` |

### 7. Repeated test

JUnit 4 used a randomizedtesting-specific `@Repeat` annotation combined with `@Test`.
JUnit 5/6 provides a built-in `@RepeatedTest` annotation.

**Before:**

```java
@Test
@Repeat(iterations = 5)
public void randomIntegerWithRange04() { ... }
```

**After:**

```java
@RepeatedTest(5)
void randomIntegerWithRange04(Random rnd) { ... }
```

### 8. Thread-leak filter interface

The `ThreadFilter` interface is replaced by the standard Java `Predicate<Thread>`.
The contract is also **inverted**: `ThreadFilter.reject(t)` returned `true` to _reject_
(i.e. ignore) a thread, while `Predicate.test(t)` returns `true` to _accept_ (i.e.
exclude from leak detection). The method name changes accordingly.

| Before                            | After                           |
| --------------------------------- | ------------------------------- |
| `implements ThreadFilter`         | `implements Predicate<Thread>`  |
| `public boolean reject(Thread t)` | `public boolean test(Thread t)` |

### 9. New `IntelliJThreadsFilter`

A second filter class was added to suppress spurious thread-leak warnings when running
tests from IntelliJ IDEA (threads named `JMX server` and `RMI TCP Connection`). This is
referenced in `@DetectThreadLeaks.ExcludeThreads` alongside `FriendlyZombieFilter`.

---

## Summary table

| Concern                 | JUnit 4 approach                                       | JUnit 6 approach                                           |
| ----------------------- | ------------------------------------------------------ | ---------------------------------------------------------- |
| Test runner             | `@RunWith(RandomizedRunner.class)`                     | `@Randomized`                                              |
| Thread leak detection   | `@ThreadLeakFilters(filters = {...})`                  | `@DetectThreadLeaks` + `@DetectThreadLeaks.ExcludeThreads` |
| Thread filter contract  | `ThreadFilter.reject()` returns `true` to ignore       | `Predicate<Thread>.test()` returns `true` to exclude       |
| Maven test execution    | Custom `junit4-maven-plugin`                           | Standard Maven Surefire                                    |
| Randomized helpers      | No-arg static methods from `RandomizedTest` base class | Static methods with explicit `Random rnd` parameter        |
| Seeded test             | `@Seed("value")`                                       | `@FixSeed("value")`                                        |
| Repeated test           | `@Test` + `@Repeat(iterations = N)`                    | `@RepeatedTest(N)`                                         |
| Test name access        | `@Rule TestName` field                                 | `TestInfo` parameter in `@BeforeEach`                      |
| Class/method visibility | `public` required                                      | Package-private allowed                                    |
