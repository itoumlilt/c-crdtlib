/*
* MIT License
*
* Copyright © 2022, Concordant and contributors.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
* associated documentation files (the "Software"), to deal in the Software without restriction,
* including without limitation the rights to use, copy, modify, merge, publish, distribute,
* sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
* NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package crdtlib.utils

import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.kotest.matchers.booleans.*
import io.kotest.matchers.comparables.*

/**
 * Represents a test suite for SimpleEnvironment.
 */
class SimpleEnvironmentTest : StringSpec({

    /**
     * This test evaluates that the first timestamp generated by an empty environment has the good
     * client unique id (the one associated with the environment) and a count equals to 1.
     */
    "empty client gets new timestamp" {
        val uid = ClientUId("clientid")
        val client = SimpleEnvironment(uid)

        val ts = client.tick()

        ts.shouldBe(Timestamp(uid, Timestamp.CNT_MIN_VALUE + 1))
    }

    /**
     * This test evaluates that the current state associated with an empty environment is an empty
     * version vector.
     */
    "empty client gets current state" {
        val uid = ClientUId("clientid")
        val client = SimpleEnvironment(uid)
        val emptyVV = VersionVector()

        val vv = client.getState()

        vv.isSmallerOrEquals(emptyVV).shouldBeTrue()
        emptyVV.isSmallerOrEquals(vv).shouldBeTrue()
    }

    /**
     * This test evaluates that after updating the environment with a local timestamp, the next
     * generated timestamp has a client unique id equals to the one associated with the
     * environment and a count equals to the update timestamp count plus 1.
     */
    "update with local timestamp then generate new timestamp" {
        val uid = ClientUId("clientid")
        val client = SimpleEnvironment(uid)

        client.update(Timestamp(uid, 7))
        val ts = client.tick()

        ts.shouldBe(Timestamp(uid, 8))
    }

    /**
     * This test evaluates that after updating the environment with a foreign timestamp, the next
     * generated timestamp has a client unique id equals to the one associated with the
     * environment and a count equals to the update timestamp count plus 1.
     */
    "update with foreign timestamp then generate new timestamp" {
        val uid1 = ClientUId("clientid1")
        val uid2 = ClientUId("clientid2")
        val client = SimpleEnvironment(uid1)

        client.update(Timestamp(uid2, 4))
        val ts = client.tick()

        ts.shouldBe(Timestamp(uid1, 5))
    }

    /**
     * This test evaluates that after updating the environment with a local timestamp having a
     * MAX_VALUE counter, the next generation of timestamp throws an exception.
     */
    "update with local timestamp then generate new timestamp and overflow" {
        val uid = ClientUId("clientid")
        val client = SimpleEnvironment(uid)

        client.update(Timestamp(uid, Int.MAX_VALUE))

        shouldThrow<RuntimeException> {
            client.tick()
        }
    }

    /**
     * This test evaluates that after updating the environment with a foreign timestamp having a
     * MAX_VALUE counter, the next generation of timestamp throws an exception.
     */
    "update with foreign timestamp then generate new timestamp and overflow" {
        val uid1 = ClientUId("clientid1")
        val uid2 = ClientUId("clientid2")
        val client = SimpleEnvironment(uid1)

        client.update(Timestamp(uid2, Int.MAX_VALUE))

        shouldThrow<RuntimeException> {
            client.tick()
        }
    }

    /**
     * This test evaluates that after updating the environment with a foreign and a local timestamp,
     * the next generated timestamp has a client unique id equals to the one associated with the
     * environment and a count equals to the max of update timestamp counts plus 1.
     */
    "update with local and foreign timestamp then generate timestamp" {
        val uid1 = ClientUId("clientid1")
        val uid2 = ClientUId("clientid2")
        val client = SimpleEnvironment(uid1)

        client.update(Timestamp(uid2, 6))
        client.update(Timestamp(uid1, 5))
        val ts = client.tick()

        ts.shouldBe(Timestamp(uid1, 7))
    }

    /**
     * This test evaluates that after updating the environment with a local timestamp, the current
     * state associated with the environment is a version vector containing the update timestamp.
     */
    "update with local timestamp then get current state" {
        val uid = ClientUId("clientid")
        val client = SimpleEnvironment(uid)
        val ts = Timestamp(uid, 7)
        val cmpVV = VersionVector()
        cmpVV.update(ts)

        client.update(ts)
        val vv = client.getState()

        vv.isSmallerOrEquals(cmpVV).shouldBeTrue()
        cmpVV.isSmallerOrEquals(vv).shouldBeTrue()
    }

    /**
     * This test evaluates that after updating the environment with a foreign timestamp, the current
     * state associated with the environment is a version vector containing the update timestamp.
     */
    "update with foreign timestamp then get current state" {
        val uid1 = ClientUId("clientid1")
        val uid2 = ClientUId("clientid2")
        val client = SimpleEnvironment(uid1)
        val ts = Timestamp(uid2, 5)
        val cmpVV = VersionVector()
        cmpVV.update(ts)

        client.update(ts)
        val vv = client.getState()

        vv.isSmallerOrEquals(cmpVV).shouldBeTrue()
        cmpVV.isSmallerOrEquals(vv).shouldBeTrue()
    }

    /**
     * This test evaluates that after updating the environment with a foreign and a local timestamp,
     * the current state associated with the environment is a version vector containing the update
     * timestamps.
     */
    "update with local and foreign timestamp then get current state" {
        val uid1 = ClientUId("clientid1")
        val uid2 = ClientUId("clientid2")
        val client = SimpleEnvironment(uid1)
        val ts1 = Timestamp(uid1, 7)
        val ts2 = Timestamp(uid2, 6)
        val cmpVV = VersionVector()
        cmpVV.update(ts1)
        cmpVV.update(ts2)

        client.update(ts1)
        client.update(ts2)
        val vv = client.getState()

        vv.isSmallerOrEquals(cmpVV).shouldBeTrue()
        cmpVV.isSmallerOrEquals(vv).shouldBeTrue()
    }

    /**
     * This test evaluates that after updating the environment with a version vector, the next
     * generated timestamp has a client unique id equals to the one associated with the
     * environment and a count equals to the max value in the update version vector plus 1.
     */
    "update with version vector then generate timestamp" {
        val uid1 = ClientUId("clientid1")
        val uid2 = ClientUId("clientid2")
        val client = SimpleEnvironment(uid1)
        val ts1 = Timestamp(uid1, 6)
        val ts2 = Timestamp(uid2, 7)
        val vv = VersionVector()
        vv.update(ts1)
        vv.update(ts2)

        client.update(vv)
        val ts3 = client.tick()

        ts3.shouldBe(Timestamp(uid1, 8))
    }

    /**
     * This test evaluates that after updating the environment with a version vector, the current
     * state associated to the environment equals to the update version vector.
     */
    "update with version vector then get current state" {
        val uid1 = ClientUId("clientid1")
        val uid2 = ClientUId("clientid2")
        val client = SimpleEnvironment(uid1)
        val ts1 = Timestamp(uid1, 6)
        val ts2 = Timestamp(uid2, 5)
        val vv1 = VersionVector()
        vv1.update(ts1)
        vv1.update(ts2)

        client.update(vv1)
        val vv2 = client.getState()

        vv1.isSmallerOrEquals(vv2).shouldBeTrue()
        vv2.isSmallerOrEquals(vv1).shouldBeTrue()
    }
})
