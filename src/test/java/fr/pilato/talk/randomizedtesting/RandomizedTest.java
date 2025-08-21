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

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.ThreadFilter;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakFilters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Random;

import static com.carrotsearch.randomizedtesting.RandomizedTest.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RandomizedRunner.class)
@ThreadLeakFilters(filters = {RandomizedTest.FriendlyZombieFilter.class})
public class RandomizedTest {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Locale savedLocale = Locale.getDefault();

    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void setLocale() {
        String testLocale = System.getProperty("tests.locale", "random");
        Locale locale = testLocale.equals("random") ? com.carrotsearch.randomizedtesting.RandomizedTest.randomLocale() :
                new Locale.Builder().setLanguageTag(testLocale).build();
        Locale.setDefault(locale);
        LOGGER.info("🌍 Starting test suite with Locale [{}]", Locale.getDefault().toLanguageTag());
    }

    @AfterClass
    public static void resetLocale() {
        Locale.setDefault(savedLocale);
    }

    @Test
    public void random00() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        Random generator = new Random();
        int num = generator.nextInt();
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isNotEqualTo(1553932502);
    }

    @Test
    public void randomWithSeed01() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        Random generator = new Random(12345L);
        int num = generator.nextInt();
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isEqualTo(1553932502);
    }

    @Test
    public void randomInteger02() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        int num = randomInt();
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isNotEqualTo(-1671230352);
    }

    @Test
    @Seed("12345")
    public void randomIntegerWithSeed03() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        int num = randomInt();
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isEqualTo(-1671230352);
    }

    @Test
    @Repeat(iterations = 5)
    public void randomIntegerWithRange04() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        int num = randomIntBetween(0, 10);
        LOGGER.info(" ➡️ Num is [{}]", num);
        assertThat(num).isBetween(0, 10);
    }

    @Test
    public void randomLocale10() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
        String format = dateTimeFormatter.format(LocalDate.now());
        LOGGER.info(" ➡️ Date is formatted as [{}]", format);
    }

    @Test
    public void ignoreIfUseless20() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        boolean b = randomBoolean();
        LOGGER.info(" ➡️ boolean is [{}]", b);
        assumeTrue(b);
        assertThat(b).isTrue();
    }

    @Test
    public void stopOrIdentifyYourThreads30() {
        LOGGER.info("🏁 Starting test [{}]", name.getMethodName());
        LOGGER.info(" ➡️ starting a new Thread");
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {
                }
            }
        }, "friendly-zombie").start();
    }

    public static class FriendlyZombieFilter implements ThreadFilter {
        public boolean reject(Thread t) {
            return "friendly-zombie".equals(t.getName());
        }
    }
}