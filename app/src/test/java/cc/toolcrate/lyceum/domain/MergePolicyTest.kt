package cc.toolcrate.lyceum.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MergePolicyTest {
    private val d1 = LocalDate.of(2026, 1, 1)
    private val d2 = LocalDate.of(2026, 1, 2)
    private fun e(date: LocalDate, body: String, updated: Long, tags: List<String> = emptyList()) =
        Entry(date = date, prompt = "p", body = body, tags = tags, createdAt = 1, updatedAt = updated)

    @Test
    fun newDaysAreAdded() {
        val plan = MergePolicy.plan(listOf(e(d1, "mine", 10)), listOf(e(d2, "theirs", 5)))
        assertEquals(listOf(d2), plan.write.map { it.date })
        assertEquals(1, plan.added)
        assertEquals(0, plan.kept)
    }

    @Test
    fun theNewerPageWinsTagsAndAll() {
        val plan = MergePolicy.plan(listOf(e(d1, "mine", 10, listOf("x"))), listOf(e(d1, "theirs", 20, listOf("y"))))
        assertEquals(listOf("theirs"), plan.write.map { it.body })
        assertEquals(listOf("y"), plan.write.single().tags)
        assertEquals(1, plan.updated)
    }

    @Test
    fun anOlderOrIdenticalPageIsKept() {
        val older = MergePolicy.plan(listOf(e(d1, "mine", 10)), listOf(e(d1, "theirs", 5)))
        val same = MergePolicy.plan(listOf(e(d1, "mine", 10)), listOf(e(d1, "mine", 30)))
        assertEquals(0, older.write.size)
        assertEquals(1, older.kept)
        assertEquals(0, same.write.size)
    }

    @Test
    fun aDayTwiceInTheFileUsesItsNewestCopy() {
        val plan = MergePolicy.plan(emptyList(), listOf(e(d1, "old", 1), e(d1, "new", 2)))
        assertEquals(listOf("new"), plan.write.map { it.body })
        assertEquals(1, plan.added)
    }
}
