package com.example.englishreader.domain.story

import org.junit.Assert.*
import org.junit.Test

class StoryParserTest {
    private val parser = StoryParser()

    private val sampleOutput = """
TITLE: The Brave Little Fox
STORY: One day a little fox went to the forest. He saw a big tree. "Hello tree!" said the fox. The tree did not talk back. The fox sat down and waited. Soon a bird came. "Hello bird!" said the fox. The bird sang a song. The fox was happy.
NEW_WORDS: forest, brave, sang
IMAGE_PROMPTS: ["A small orange fox walking into a green forest", "A fox sitting under a big tree talking to a bird"]
    """.trimIndent()

    @Test
    fun `parses title correctly`() {
        val result = parser.parse(sampleOutput)
        assertEquals("The Brave Little Fox", result.title)
    }

    @Test
    fun `parses story content`() {
        val result = parser.parse(sampleOutput)
        assertTrue(result.content.startsWith("One day"))
        assertTrue(result.content.contains("The fox was happy."))
    }

    @Test
    fun `parses new words`() {
        val result = parser.parse(sampleOutput)
        assertEquals(listOf("forest", "brave", "sang"), result.newWords)
    }

    @Test
    fun `parses image prompts`() {
        val result = parser.parse(sampleOutput)
        assertEquals(2, result.imagePrompts.size)
        assertTrue(result.imagePrompts[0].contains("fox"))
    }

    @Test
    fun `extracts unique words from story`() {
        val result = parser.parse(sampleOutput)
        val words = parser.extractWords(result.content)
        assertTrue(words.contains("fox"))
        assertTrue(words.contains("forest"))
        assertFalse(words.contains("\""))
    }

    @Test
    fun `splits story into sentence groups`() {
        val result = parser.parse(sampleOutput)
        val groups = parser.splitIntoGroups(result.content, 4)
        assertTrue(groups.isNotEmpty())
        assertTrue(groups.size >= 2)
    }

    @Test
    fun `handles empty input gracefully`() {
        val result = parser.parse("")
        assertEquals("", result.title)
        assertEquals("", result.content)
        assertTrue(result.newWords.isEmpty())
    }
}
