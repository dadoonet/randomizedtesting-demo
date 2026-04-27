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

import com.carrotsearch.randomizedtesting.jupiter.*;
import com.carrotsearch.randomizedtesting.jupiter.DetectThreadLeaks.ExcludeThreads;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DetectThreadLeaks
@ExcludeThreads({RandomTest.FriendlyZombieFilter.class, IntelliJThreadsFilter.class})
@Randomized
class RandomTest {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Locale savedLocale = Locale.getDefault();

    @BeforeAll
    static void setLocale(Random rnd) {
        String testLocale = System.getProperty("tests.locale", "random");
        Locale locale = testLocale.equals("random") ? RandomizedTest.randomLocale(rnd) :
                new Locale.Builder().setLanguageTag(testLocale).build();
        Locale.setDefault(locale);
        LOGGER.info("🌍 Starting test suite with Locale [{}]", Locale.getDefault().toLanguageTag());
    }

    @AfterAll
    static void resetLocale() {
        Locale.setDefault(savedLocale);
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo, RandomizedContext ctx) {
        LOGGER.info("🏁 Starting test [{}] with seed [{}]", testInfo.getDisplayName(), ctx.getRootSeed());
    }

    @Test
    void randomInteger02(Random rnd) {
        int num = RandomizedTest.randomInt(rnd);
        LOGGER.info(" ➡️  Num is [{}]", num);
        assertThat(num).isNotEqualTo(-1671230352);
    }

    @Test
    @FixSeed("12345")
    void randomIntegerWithSeed03(Random rnd) {
        int num = RandomizedTest.randomInt(rnd);
        LOGGER.info(" ➡️  Num is [{}]", num);
        assertThat(num).isEqualTo(-1671230352);
    }

    @RepeatedTest(5)
    void randomIntegerWithRange04(Random rnd) {
        int num = RandomizedTest.randomIntInRange(rnd, 0, 10);
        LOGGER.info(" ➡️  Num is [{}]", num);
        assertThat(num).isBetween(0, 10);
    }

    @RepeatedTest(5)
    void repeatMe05(Random rnd) {
        int num = RandomizedTest.randomIntInRange(rnd, 0, 10);
        LOGGER.info(" ➡️  Num is [{}]", num);
        assertThat(num).isBetween(0, 10);
    }

    @RepeatedTest(10)
    void repeatMe(Random rnd) {
        Locale locale = RandomizedTest.randomLocale(rnd);
        DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
        String format = dateTimeFormatter.format(LocalDate.now());
        LOGGER.info(" ➡️  Date is formatted as [{}] with locale [{}}]", format, locale.toLanguageTag());
    }

    @Test
    void randomLocale10() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
        String format = dateTimeFormatter.format(LocalDate.now());
        LOGGER.info(" ➡️  Date is formatted as [{}]", format);
        assertThat(format).isNotEmpty();
    }

    @Test
    void ignoreIfUseless20(Random rnd) {
        boolean b = RandomizedTest.randomBoolean(rnd);
        LOGGER.info(" ➡️  boolean is [{}]", b);
        assumeTrue(b);
        assertThat(b).isTrue();
    }

    @Test
    void ignoreOnWindows21() {
        String osName = System.getProperty("os.name").toLowerCase();
        assumeFalse(osName.contains("win"));
        // Run the specific non windows test here
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void ignoreOnWindows22() {
        // Run the specific non windows test here
    }

    @Test
    void stopOrIdentifyYourThreads30() {
        LOGGER.info(" ➡️  Starting a new Thread");
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

    @Test
    void checkBugGestalt242() {
        Settings settings = loadSettings();
        // This is supposed to fail because of:
        // https://github.com/gestalt-config/gestalt/issues/242
        assertThatNullPointerException()
                .isThrownBy(() -> settings.fileName().toString());
    }

    public static class FriendlyZombieFilter implements Predicate<Thread> {
        public boolean test(Thread t) {
            return "friendly-zombie".equals(t.getName());
        }
    }

    // Util method for demo

    private Settings loadSettings() {
        // Simulate fix of issue https://github.com/gestalt-config/gestalt/issues/242
        return new Settings("foobar");
        // return null;
    }

    public record Settings(String fileName) {

    }

    /*
    @Test
    void testLocaleAz() throws Exception {
        Locale.setDefault(new Locale.Builder().setLanguageTag("az") .build());
        Gestalt gestalt = new GestaltBuilder()
                . addSource(MapConfigSourceBuilder.builder()
                        •addCustomConfig("elasticsearch.index", "foo")
                        •build())
                .build ();
        gestalt.loadConfigs ();
        assertThat(gestalt.getConfigOptional/"elasticsearch", Elasticsearch.class)).isPresent() ;
    }
    @Test
    void testLocaleFr() throws Exception {
        Locale.setDefault(new Locale.Builder().setLanguageTag("fr") .build());
        Gestalt gestalt = new GestaltBuilder()
                . addSource(MapConfigSourceBuilder.builder()
                        •addCustomConfig("elasticsearch.index", "foo")
                        •build())
                .build ();
        gestalt.loadConfigs ();
        assertThat(gestalt.getConfigOptional/"elasticsearch", Elasticsearch.class)).isPresent() ;
    }
    */
}