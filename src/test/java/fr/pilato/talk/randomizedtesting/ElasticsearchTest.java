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

import com.carrotsearch.randomizedtesting.jupiter.Randomized;
import com.carrotsearch.randomizedtesting.jupiter.RandomizedTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.util.Random;

@Randomized
class ElasticsearchTest {

    private static final Logger LOGGER = LogManager.getLogger();

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        LOGGER.info("🏁 Starting test [{}]", testInfo.getDisplayName());
    }

    @Test
    void testCreateClusterAndIndex(Random rnd) {
        int numberOfNodes = RandomizedTest.randomIntInRange(rnd, 1, 20);
        int numberOfShards = RandomizedTest.randomIntInRange(rnd, 1, 30);
        int numberOfReplicas = RandomizedTest.randomIntInRange(rnd, 0, numberOfNodes - 1);
        String indexName = RandomizedTest.randomAsciiAlphanumOfLength(
                rnd, RandomizedTest.randomIntInRange(rnd, 5, 20)) ;

        // Create a cluster with the specified number of nodes
        startCluster(numberOfNodes) ;

        // Create an index with the specified number of shards and replicas
        createIndex(indexName, numberOfShards, numberOfReplicas);

        // Run tests here
        // ...
    }

    @Test
    void sometimes(Random rnd) {
        int bulkSize = RandomizedTest.randomIntInRange(rnd, 500, 1000);
        for (int i = 0; i < bulkSize; i++) {
            addDocument("english_" + i, generatePerson(rnd));
            if (RandomizedTest.frequently(rnd)) {
                addDocument("french_" + i, generatePerson(rnd));
            }

            if (RandomizedTest.rarely(rnd)) {
                // Shutdown Node 1
                shutdownNode(1);
            }
        }
    }

    @NightlyTest
    void longRunningTest(Random rnd) {
        int bulkSize = RandomizedTest.randomIntInRange(rnd, 10, 1000);
        LOGGER.debug("Starting test [{}]", bulkSize);
        for (int i = 0; i < bulkSize; i++) {
            try {
                int waitTime = RandomizedTest.randomIntInRange(rnd, 10, 100);
                LOGGER.debug("Round [{}], waiting for test [{}]", i, waitTime);
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    // Fake methods

    private void createIndex(String indexName, int numberOfShards, int numberOfReplicas) {
        LOGGER.debug("Creating index [{}] with [{}] shards and [{}] replicas",
                indexName,  numberOfShards, numberOfReplicas);
    }

    private void startCluster(int number0fNodes) {
        LOGGER.debug("Starting cluster of [{}] nodes.", number0fNodes);
    }

    private Person generatePerson(Random rnd) {
        return new Person(RandomizedTest.randomAsciiAlphanumOfLength(rnd,10), RandomizedTest.randomIntInRange(rnd,18, 80));
    }

    private void addDocument(String id, Person person) {
        LOGGER.debug("Adding document [{}]: [{}]", id, person);
    }

    private void shutdownNode(int node) {
        LOGGER.debug("Shutting down node [{}]", node);
    }
}
