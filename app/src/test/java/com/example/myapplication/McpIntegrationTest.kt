package com.example.myapplication.test

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit-Tests für MCP-Integration gemäß Using-MCP.md Regeln
 */
class McpIntegrationTest {

    /**
     * Simuliert die buildMcpIntegrations() Logik
     */
    private fun buildMcpIntegrations(
        imageGenEnabled: Boolean = false,
        filesystemEnabled: Boolean = false,
        webSearchEnabled: Boolean = false,
        cliMcpServerEnabled: Boolean = false,
        blenderEnabled: Boolean = false,
        datetimeEnabled: Boolean = false,
        manualServers: String = ""
    ): List<String>? {
        val enabledServers = mutableListOf<String>()

        if (imageGenEnabled) enabledServers.add("mcp/image-gen")
        if (filesystemEnabled) enabledServers.add("mcp/filesystem")
        if (webSearchEnabled) enabledServers.add("mcp/web-search")
        if (cliMcpServerEnabled) enabledServers.add("mcp/cli-mcp-server")
        if (blenderEnabled) enabledServers.add("mcp/blender")
        if (datetimeEnabled) enabledServers.add("mcp/datetime")

        val manualServerList = manualServers.split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { if (it.startsWith("mcp/")) it else "mcp/$it" }

        enabledServers.addAll(manualServerList)

        return enabledServers.distinct().ifEmpty { null }
    }

    @Test
    fun `test no servers enabled returns null`() {
        // Gemäß Using-MCP.md: wenn keine Server, dann null
        val result = buildMcpIntegrations()
        assertNull("Wenn keine Server aktiviert sind, soll null zurückgegeben werden", result)
    }

    @Test
    fun `test single toggle server`() {
        // Test: Ein Toggle-Server aktiviert
        val result = buildMcpIntegrations(imageGenEnabled = true)
        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("mcp/image-gen", result?.get(0))
    }

    @Test
    fun `test multiple toggle servers`() {
        // Test: Mehrere Toggle-Server aktiviert
        val result = buildMcpIntegrations(
            imageGenEnabled = true,
            webSearchEnabled = true,
            datetimeEnabled = true
        )
        assertNotNull(result)
        assertEquals(3, result?.size)
        assertTrue(result?.contains("mcp/image-gen") ?: false)
        assertTrue(result?.contains("mcp/web-search") ?: false)
        assertTrue(result?.contains("mcp/datetime") ?: false)
    }

    @Test
    fun `test manual server input`() {
        // Test: Manuelle Server-Eingabe
        val result = buildMcpIntegrations(manualServers = "playwright, custom-server")
        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals("mcp/playwright", result?.get(0))
        assertEquals("mcp/custom-server", result?.get(1))
    }

    @Test
    fun `test manual server with mcp prefix`() {
        // Test: Manuelle Eingabe mit bereits vorhandenem mcp/ Präfix
        val result = buildMcpIntegrations(manualServers = "mcp/playwright")
        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("mcp/playwright", result?.get(0))
    }

    @Test
    fun `test combined toggle and manual servers`() {
        // Test: Kombination aus Toggle und manuellen Servern
        val result = buildMcpIntegrations(
            imageGenEnabled = true,
            webSearchEnabled = true,
            manualServers = "playwright, filesystem"
        )
        assertNotNull(result)
        assertEquals(4, result?.size)
        assertTrue(result?.contains("mcp/image-gen") ?: false)
        assertTrue(result?.contains("mcp/web-search") ?: false)
        assertTrue(result?.contains("mcp/playwright") ?: false)
        assertTrue(result?.contains("mcp/filesystem") ?: false)
    }

    @Test
    fun `test duplicate servers are removed`() {
        // Test: Duplikate werden entfernt
        val result = buildMcpIntegrations(
            filesystemEnabled = true,
            manualServers = "filesystem, playwright, filesystem"
        )
        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals(1, result?.count { it == "mcp/filesystem" })
    }

    @Test
    fun `test empty manual server string`() {
        // Test: Leerer String in manueller Eingabe
        val result = buildMcpIntegrations(
            imageGenEnabled = true,
            manualServers = "  ,  , "
        )
        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("mcp/image-gen", result?.get(0))
    }

    @Test
    fun `test format matches Using-MCP-md example`() {
        // Test: Format entspricht Using-MCP.md Beispiel
        // Using-MCP.md: "integrations": ["mcp/playwright"]
        val result = buildMcpIntegrations(manualServers = "playwright")
        assertNotNull(result)
        assertEquals(listOf("mcp/playwright"), result)
    }

    @Test
    fun `test all servers enabled`() {
        // Test: Alle Server aktiviert
        val result = buildMcpIntegrations(
            imageGenEnabled = true,
            filesystemEnabled = true,
            webSearchEnabled = true,
            cliMcpServerEnabled = true,
            blenderEnabled = true,
            datetimeEnabled = true
        )
        assertNotNull(result)
        assertEquals(6, result?.size)
        assertEquals(
            setOf(
                "mcp/image-gen",
                "mcp/filesystem",
                "mcp/web-search",
                "mcp/cli-mcp-server",
                "mcp/blender",
                "mcp/datetime"
            ),
            result?.toSet()
        )
    }
}

