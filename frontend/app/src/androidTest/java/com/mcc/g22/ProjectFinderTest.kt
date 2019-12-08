package com.mcc.g22

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.database.FirebaseDatabase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class ProjectFinderTest {

    @Before
    fun setUp() {

        // Create test database
        val database = FirebaseDatabase.getInstance().reference

        val groupWorkProject = database.child("projects").child("group work").ref
        groupWorkProject.child("members").child("test user").setValue(true)
        groupWorkProject.child("name").setValue("group work")
        groupWorkProject.child("keywords").child("groupp").setValue(true)
        groupWorkProject.child("keywords").child("work").setValue(true)
        groupWorkProject.child("keywords").child("common").setValue(true)

        val personalProject = database.child("projects").child("foo bar personal project").ref
        personalProject.child("members").child("test user").setValue(true)
        personalProject.child("name").setValue("foo bar personal project")
        personalProject.child("keywords").child("personal").setValue(true)
        personalProject.child("keywords").child("common").setValue(true)

        val notMyProject = database.child("projects").child("not my project").ref
        notMyProject.child("members").child("test user2").setValue(true)
        notMyProject.child("name").setValue("not my project")
        notMyProject.child("keywords").child("personal").setValue(true)
        notMyProject.child("keywords").child("groupp").setValue(true)
        notMyProject.child("keywords").child("my").setValue(true)
    }

    @Test
    fun searchByKeyword() {
        val countDownLatch = CountDownLatch(1)
        var result = false

        ProjectFinder.findProject(null, "groupp", {
            assertEquals(1, it.size)
            assertTrue(it.contains("group work"))
            result = true
            countDownLatch.countDown()
        }, {
            result = false
            countDownLatch.countDown()
        })

        countDownLatch.await()
        assertTrue(result)
    }

    @Test
    fun searchByProjectName() {
        val countDownLatch = CountDownLatch(1)
        var result = false

        ProjectFinder.findProject("bar", null, {
            assertEquals(1, it.size)
            assertTrue(it.contains("foo bar personal project"))
            result = true
            countDownLatch.countDown()
        }, {
            result = false
            countDownLatch.countDown()
        })

        countDownLatch.await()
        assertTrue(result)
    }

    @Test
    fun searchByProjectNameAndKeyword() {
        val countDownLatch = CountDownLatch(1)
        var result = false

        ProjectFinder.findProject("o", "personal", {
            assertEquals(1, it.size)
            assertTrue(it.contains("foo bar personal project"))
            result = true
            countDownLatch.countDown()
        }, {
            result = false
            countDownLatch.countDown()
        })

        countDownLatch.await()
        assertTrue(result)
    }

    @Test
    fun findNothing() {
        val countDownLatch = CountDownLatch(1)
        var result = false

        ProjectFinder.findProject("blah", "mleh", {
            result = it.isEmpty()
            countDownLatch.countDown()
        }, {
            result = false
            countDownLatch.countDown()
        })

        countDownLatch.await()
        assertTrue(result)
    }

    @Test
    fun findMany() {
        var countDownLatch = CountDownLatch(1)
        var result = false

        ProjectFinder.findProject("", "common", {
            assertEquals(2, it.size)
            assertTrue(it.contains("group work"))
            assertTrue(it.contains("foo bar personal project"))
            result = true
            countDownLatch.countDown()
        }, {
            result = false
            countDownLatch.countDown()
        })
        countDownLatch.await()
        assertTrue(result)

        countDownLatch = CountDownLatch(1)
        ProjectFinder.findProject("o", "", {
            assertEquals(2, it.size)
            assertTrue(it.contains("group work"))
            assertTrue(it.contains("foo bar personal project"))
            result = true
            countDownLatch.countDown()
        }, {
            result = false
            countDownLatch.countDown()
        })
        countDownLatch.await()
        assertTrue(result)
    }
}