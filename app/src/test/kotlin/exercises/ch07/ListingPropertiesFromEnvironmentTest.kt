package exercises.ch07

import com.example.shop.product.ListingProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.env.SystemEnvironmentPropertySource

/** bean を何も持たない設定のクラス。Spring Boot に設定ファイルと環境変数を読ませるためだけに起動する。 */
@Configuration(proxyBeanMethods = false)
class EmptyConfiguration

/**
 * 第 7 章の練習問題 1: 環境変数の名前が、設定の名前 shop.listing.* に対応すること。
 * 環境変数が application.yaml の値より強いこと。名前を誤った環境変数は、どの設定にも対応しないこと。
 */
class ListingPropertiesFromEnvironmentTest {

    /** [variables] を OS の環境変数として持つ環境を作る。実際の環境変数は使わない。 */
    private fun environmentWith(variables: Map<String, String>) = StandardEnvironment().apply {
        // 環境変数の名前を設定の名前に読み替える規則は、この名前の源にだけ当てられる。
        propertySources.replace(
            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, variables),
        )
    }

    /**
     * Spring Boot を起動して application.yaml と [variables] を読ませ、shop.listing を ListingProperties に束縛する。
     * どちらの値が勝つかは、Spring Boot が決める順のまま。
     */
    private fun bindWithApplicationYaml(variables: Map<String, String>): ListingProperties {
        val application = SpringApplication(EmptyConfiguration::class.java).apply {
            setEnvironment(environmentWith(variables))
            setWebApplicationType(WebApplicationType.NONE)
            setBannerMode(Banner.Mode.OFF)
        }
        application.run().use { context ->
            return Binder.get(context.environment).bind("shop.listing", ListingProperties::class.java).get()
        }
    }

    /** 環境変数だけを源にして、shop.listing を ListingProperties に束縛する。 */
    private fun bindFromVariablesOnly(variables: Map<String, String>): ListingProperties {
        val source = environmentWith(variables).propertySources
            .get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)!!
        return Binder(ConfigurationPropertySources.from(source).filterNotNull())
            .bind("shop.listing", ListingProperties::class.java)
            .get()
    }

    @Test
    fun `SHOP_LISTING_MAXLIMIT は application yaml の max-limit より強い`() {
        val properties = bindWithApplicationYaml(mapOf("SHOP_LISTING_MAXLIMIT" to "50"))

        assertEquals(ListingProperties(defaultLimit = 20, maxLimit = 50), properties)
    }

    @Test
    fun `名前を誤った環境変数はどの設定にも対応せず、application yaml の値のまま`() {
        val properties = bindWithApplicationYaml(mapOf("SHOP_LISTINGS_MAXLIMIT" to "50"))

        assertEquals(ListingProperties(defaultLimit = 20, maxLimit = 100), properties)
    }

    @Test
    fun `上限を既定の件数より小さくすると束縛の時点で止まる`() {
        val exception = assertThrows<BindException> {
            bindFromVariablesOnly(mapOf("SHOP_LISTING_DEFAULTLIMIT" to "20", "SHOP_LISTING_MAXLIMIT" to "10"))
        }

        assertInstanceOf(IllegalArgumentException::class.java, exception.rootCause())
    }

    private fun Throwable.rootCause(): Throwable = generateSequence(this) { it.cause }.last()
}
