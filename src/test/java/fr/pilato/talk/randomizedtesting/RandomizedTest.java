/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.pilato.talk.randomizedtesting;

import com.carrotsearch.randomizedtesting.jupiter.DetectThreadLeaks;
import com.carrotsearch.randomizedtesting.jupiter.FixSeed;import com.carrotsearch.randomizedtesting.jupiter.Randomized;
import com.carrotsearch.randomizedtesting.jupiter.SystemThreadFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;

import static com.carrotsearch.randomizedtesting.jupiter.RandomizedTest.randomBoolean;
import static com.carrotsearch.randomizedtesting.jupiter.RandomizedTest.randomInt;
import static com.carrotsearch.randomizedtesting.jupiter.RandomizedTest.randomIntInRange;
import static com.carrotsearch.randomizedtesting.jupiter.RandomizedTest.randomLocale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DetectThreadLeaks
@DetectThreadLeaks.ExcludeThreads({
        RandomizedTest.FriendlyZombieFilter.class,
        RandomizedTest.IntelliJThreadsFilter.class,
        SystemThreadFilter.class})
@Randomized
class RandomizedTest {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Locale savedLocale = Locale.getDefault();

    @BeforeAll
    static void setLocale(Random rnd) {
        String testLocale = System.getProperty("tests.locale", "random");
        Locale locale = testLocale.equals("random") ? randomLocale(rnd) :
                new Locale.Builder().setLanguageTag(testLocale).build();
        Locale.setDefault(locale);
        LOGGER.info("🌍 Starting test suite with Locale [{}]", Locale.getDefault().toLanguageTag());
    }

    @AfterAll
    static void resetLocale() {
        Locale.setDefault(savedLocale);
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        LOGGER.info("🏁 Starting test [{}]", testInfo.getDisplayName());
    }

    @Test
    void random00() {
        Random generator = new Random();
        int num = generator.nextInt();
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isNotEqualTo(1553932502);
    }

    @Test
    void randomWithSeed01() {
        Random generator = new Random(12345L);
        int num = generator.nextInt();
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isEqualTo(1553932502);
    }

    @Test
    void randomInteger02(Random rnd) {
        int num = randomInt(rnd);
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isNotEqualTo(-1671230352);
    }

    @Test
    @FixSeed("12345")
    void randomIntegerWithSeed03(Random rnd) {
        int num = randomInt(rnd);
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isEqualTo(-1671230352);
    }

    @RepeatedTest(5)
    void randomIntegerWithRange04(Random rnd) {
        int num = randomIntInRange(rnd, 0, 10);
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isBetween(0, 10);
    }

    @Test
    void randomLocale10() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
        String format = dateTimeFormatter.format(LocalDate.now());
        LOGGER.info(" ➡️ Date is formatted as [{}]", format);
        assertThat(format).isNotEmpty();
    }

    @Test
    void ignoreIfUseless20(Random rnd) {
        boolean b = randomBoolean(rnd);
        LOGGER.info(" ➡️ boolean is [{}]", b);
        assumeTrue(b);
        assertThat(b).isTrue();
    }

    @Test
    void stopOrIdentifyYourThreads30() {
        LOGGER.info(" ➡️ starting a new Thread");
        boolean b = true;
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {
                }
            }
        }, "friendly-zombie").start();
        assertThat(b).isTrue();
    }

    public static class FriendlyZombieFilter implements Predicate<Thread> {
        public boolean test(Thread t) {
            return "friendly-zombie".equals(t.getName());
        }
    }

    /**
     * This filter is only needed when running the tests from IntelliJ
     */
    public static class IntelliJThreadsFilter implements Predicate<Thread> {
        public boolean test(Thread t) {
            boolean intellijThreads = t.getName().startsWith("JMX server") || t.getName().startsWith("RMI TCP Connection");
            if (intellijThreads) {
                LOGGER.warn("Detected IntelliJ threads [{}], if you are running the tests from IntelliJ, " +
                        "you can ignore this warning or add [{}] to the thread leak filters",
                        t.getName(), IntelliJThreadsFilter.class.getSimpleName());
            }
            return intellijThreads;
        }
    }
}