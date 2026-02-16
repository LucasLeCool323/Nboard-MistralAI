package com.nboard.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LayoutPackManagerTest {
    @Test
    fun defaultPackIdForLegacyMode_mapsAllLegacyModes() {
        assertEquals(
            LayoutPackManager.BUILTIN_AZERTY_CLASSIC_ID,
            LayoutPackManager.defaultPackIdForLegacyMode(KeyboardLayoutMode.AZERTY)
        )
        assertEquals(
            LayoutPackManager.BUILTIN_QWERTY_CLASSIC_ID,
            LayoutPackManager.defaultPackIdForLegacyMode(KeyboardLayoutMode.QWERTY)
        )
        assertEquals(
            LayoutPackManager.BUILTIN_AZERTY_GBOARD_ID,
            LayoutPackManager.defaultPackIdForLegacyMode(KeyboardLayoutMode.GBOARD_AZERTY)
        )
        assertEquals(
            LayoutPackManager.BUILTIN_QWERTY_GBOARD_ID,
            LayoutPackManager.defaultPackIdForLegacyMode(KeyboardLayoutMode.GBOARD_QWERTY)
        )
    }

    @Test
    fun parseLayoutPackXml_validPack_parsesRowsAndMetadata() {
        val xml = """
            <layout-pack id="community.azerty.demo" name="Community AZERTY" bottomStyle="gboard" qwertyLike="false">
                <row1>a z e r t y u i o p</row1>
                <row2>q s d f g h j k l m</row2>
                <row3>w x c v b n ' .</row3>
            </layout-pack>
        """.trimIndent()

        val pack = LayoutPackManager.parseLayoutPackXmlForTest(xml)

        assertEquals("community.azerty.demo", pack.id)
        assertEquals("Community AZERTY", pack.displayName)
        assertEquals(LayoutBottomStyle.GBOARD, pack.bottomStyle)
        assertFalse(pack.isQwertyLike)
        assertEquals(10, pack.row1.size)
        assertEquals(10, pack.row2.size)
        assertEquals(8, pack.row3.size)
        assertEquals("a", pack.row1.first())
        assertEquals(".", pack.row3.last())
    }

    @Test
    fun parseLayoutPackXml_withoutQwertyLike_infersFromFirstKey() {
        val xml = """
            <layout-pack id="community.qwerty.demo" name="Community QWERTY" bottomStyle="classic">
                <row1>q w e r t y u i o p</row1>
                <row2>a s d f g h j k l</row2>
                <row3>z x c v b n m , '</row3>
            </layout-pack>
        """.trimIndent()

        val pack = LayoutPackManager.parseLayoutPackXmlForTest(xml)

        assertTrue(pack.isQwertyLike)
        assertEquals(LayoutBottomStyle.CLASSIC, pack.bottomStyle)
    }

    @Test
    fun parseLayoutPackXml_withoutName_fallsBackToPackId() {
        val xml = """
            <layout-pack id="community.no.name">
                <row1>a z e r t y u i o p</row1>
                <row2>q s d f g h j k l m</row2>
                <row3>w x c v b n ' .</row3>
            </layout-pack>
        """.trimIndent()

        val pack = LayoutPackManager.parseLayoutPackXmlForTest(xml)

        assertEquals("community.no.name", pack.displayName)
    }

    @Test
    fun parseLayoutPackXml_unknownBottomStyle_defaultsToClassic() {
        val xml = """
            <layout-pack id="community.unknown.bottom" name="Unknown bottom" bottomStyle="experimental">
                <row1>a z e r t y u i o p</row1>
                <row2>q s d f g h j k l m</row2>
                <row3>w x c v b n ' .</row3>
            </layout-pack>
        """.trimIndent()

        val pack = LayoutPackManager.parseLayoutPackXmlForTest(xml)

        assertEquals(LayoutBottomStyle.CLASSIC, pack.bottomStyle)
    }

    @Test
    fun parseLayoutPackXml_explicitQwertyLike_overridesInference() {
        val xml = """
            <layout-pack id="community.override.qwerty" name="Override qwertyLike" qwertyLike="false">
                <row1>q w e r t y u i o p</row1>
                <row2>a s d f g h j k l</row2>
                <row3>z x c v b n m , '</row3>
            </layout-pack>
        """.trimIndent()

        val pack = LayoutPackManager.parseLayoutPackXmlForTest(xml)

        assertFalse(pack.isQwertyLike)
    }

    @Test
    fun parseLayoutPackXml_withVariants_parsesVariantMappings() {
        val xml = """
            <layout-pack id="community.with.variants" name="With variants">
                <row1>a z e r t y u i o p</row1>
                <row2>q s d f g h j k l m</row2>
                <row3>w x c v b n ' .</row3>
                <variants>
                    <key value="a">à á â</key>
                    <key value="e">é è ê</key>
                    <key value="'">’ `</key>
                </variants>
            </layout-pack>
        """.trimIndent()

        val pack = LayoutPackManager.parseLayoutPackXmlForTest(xml)

        assertEquals(listOf("à", "á", "â"), pack.variants["a"])
        assertEquals(listOf("é", "è", "ê"), pack.variants["e"])
        assertEquals(listOf("’", "`"), pack.variants["'"])
    }

    @Test
    fun parseLayoutPackXml_variantWithoutBase_throwsValidationError() {
        val xml = """
            <layout-pack id="community.invalid.variant" name="Invalid variant">
                <row1>a z e r t y u i o p</row1>
                <row2>q s d f g h j k l m</row2>
                <row3>w x c v b n ' .</row3>
                <variants>
                    <key>à á â</key>
                </variants>
            </layout-pack>
        """.trimIndent()

        val error = expectParseError(xml)

        assertTrue(error.message.orEmpty().contains("<key> in <variants> must define 'value'"))
    }

    @Test
    fun parseLayoutPackXml_tooFewKeys_throwsValidationError() {
        val xml = """
            <layout-pack id="community.too.few" name="Too few keys">
                <row1>a z e r</row1>
                <row2>q s d f g</row2>
                <row3>w x c v b</row3>
            </layout-pack>
        """.trimIndent()

        val error = expectParseError(xml)

        assertTrue(error.message.orEmpty().contains("between 5 and 12 keys"))
    }

    @Test
    fun parseLayoutPackXml_tooManyKeys_throwsValidationError() {
        val xml = """
            <layout-pack id="community.too.many" name="Too many keys">
                <row1>a z e r t y u i o p ^ $ *</row1>
                <row2>q s d f g h j k l m</row2>
                <row3>w x c v b n ' .</row3>
            </layout-pack>
        """.trimIndent()

        val error = expectParseError(xml)

        assertTrue(error.message.orEmpty().contains("between 5 and 12 keys"))
    }

    @Test
    fun parseLayoutPackXml_missingRow_throwsValidationError() {
        val xml = """
            <layout-pack id="community.invalid" name="Invalid">
                <row1>q w e r t y</row1>
                <row2>a s d f g</row2>
            </layout-pack>
        """.trimIndent()

        val error = expectParseError(xml)

        assertTrue(error.message.orEmpty().contains("Missing <row3>"))
    }

    @Test
    fun parseLayoutPackXml_invalidRowToken_throwsValidationError() {
        val xml = """
            <layout-pack id="community.invalid.token" name="Invalid token">
                <row1>q w e r t y u i o p</row1>
                <row2>a s d f g h j k l</row2>
                <row3>z x c v b token_too_long m</row3>
            </layout-pack>
        """.trimIndent()

        val error = expectParseError(xml)

        assertTrue(error.message.orEmpty().contains("invalid token"))
    }

    @Test
    fun parseLayoutPackXml_invalidRoot_throwsValidationError() {
        val xml = """
            <keyboard-layout id="wrong.root">
                <row1>q w e r t y</row1>
                <row2>a s d f g</row2>
                <row3>z x c v b</row3>
            </keyboard-layout>
        """.trimIndent()

        val error = expectParseError(xml)

        assertTrue(error.message.orEmpty().contains("Root tag must be <layout-pack>"))
    }

    @Test
    fun parseLayoutPackXml_malformedXml_throwsParseError() {
        val xml = """
            <layout-pack id="malformed.pack" name="Malformed">
                <row1>q w e r t y u i o p</row1>
                <row2>a s d f g h j k l</row2>
                <row3>z x c v b n m & ,</row3>
            </layout-pack>
        """.trimIndent()

        val error = expectParseError(xml)

        assertTrue(error.message.orEmpty().contains("Could not parse layout XML"))
    }

    private fun expectParseError(xml: String): LayoutPackParseException {
        try {
            LayoutPackManager.parseLayoutPackXmlForTest(xml)
        } catch (error: LayoutPackParseException) {
            return error
        }
        fail("Expected LayoutPackParseException")
        throw AssertionError("unreachable")
    }
}
