import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConventionPluginTest {

    @Test
    fun testVersioning(){
        assertEquals("0.0.5", getVersionFromRef("0.0.15", "0.0.4"))
        assertEquals("0.1.0", getVersionFromRef("0.1.0", "0.0.13"))
        assertEquals("0.1.5", getVersionFromRef("0.1.19", "0.1.4"))
        assertEquals("0.4.77", getVersionFromRef("0.4.76", "0.4.76"))
        assertEquals("1.0.0", getVersionFromRef("1.0.0", "0.50.66"))
        assertEquals("2.0.0", getVersionFromRef("2.0.0", "0.50.66"))
        assertEquals("2.0.0", getVersionFromRef("2.0.0", "1.23.44"))
    }
}