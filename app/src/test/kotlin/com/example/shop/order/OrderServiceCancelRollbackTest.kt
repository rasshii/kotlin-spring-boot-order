package com.example.shop.order

import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.InventoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.simple.JdbcClient
import java.util.UUID

/**
 * 注文を取り消す途中で例外が出たら、それまでに戻した引き当てが残らないことを確かめる。取消の保存は引き当ての戻しの
 * 後なので、注文は受付のまま残る。
 *
 * OrderServiceRollbackTest と同じく、テストのトランザクションを使わない。差し替えた repository の save を数えるので、
 * 注文と在庫の行は SQL で入れる。
 */
@Import(TestcontainersConfiguration::class, OrderServiceCancelRollbackTest.FailingInventoryConfiguration::class)
@SpringBootTest
class OrderServiceCancelRollbackTest(
    @Autowired private val orderService: OrderService,
    @Autowired private val jdbcClient: JdbcClient,
) {

    @TestConfiguration(proxyBeanMethods = false)
    class FailingInventoryConfiguration {
        /** @Primary なので、OrderService にはこちらが渡る。委譲先は、名前で指した本物の repository。 */
        @Bean
        @Primary
        fun failOnSecondSaveInventoryRepository(
            @Qualifier("inventoryRepository") delegate: InventoryRepository,
        ) = FailOnSecondSaveInventoryRepository(delegate)
    }

    private val orderId = UUID.fromString("019a1c00-0000-7000-8000-000000000001")
    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val marker = UUID.fromString("0199f3a8-0000-7000-8000-000000000004")

    private fun insertProduct(productId: UUID, name: String, price: Int, inventoryQuantity: Int) {
        jdbcClient.sql("insert into products (id, name, price, tax_rate, version) values (:id, :name, :price, 10, 0)")
            .param("id", productId).param("name", name).param("price", price).update()
        jdbcClient.sql("insert into inventories (product_id, quantity, version) values (:id, :quantity, 0)")
            .param("id", productId).param("quantity", inventoryQuantity).update()
    }

    /** ノート 2 冊とマーカー 1 本の受付の注文。在庫は、この注文の分を引き当てた後の数にしておく。 */
    private fun insertOrder() {
        jdbcClient.sql(
            "insert into orders (id, contact_email, status, ordered_at, version) values (:id, 'taro@example.com', 'PENDING', now(), 0)",
        ).param("id", orderId).update()
        listOf(Triple(notebook, 2, 330), Triple(marker, 1, 880)).forEachIndexed { index, (productId, quantity, unitPrice) ->
            jdbcClient.sql(
                """
                insert into order_lines (order_id, line_index, product_id, quantity, unit_price, tax_rate)
                values (:orderId, :index, :productId, :quantity, :unitPrice, 10)
                """.trimIndent(),
            )
                .param("orderId", orderId).param("index", index).param("productId", productId)
                .param("quantity", quantity).param("unitPrice", unitPrice)
                .update()
        }
    }

    private fun inventoryQuantity(productId: UUID): Int =
        jdbcClient.sql("select quantity from inventories where product_id = :id")
            .param("id", productId)
            .query(Int::class.java)
            .single()

    private fun orderStatus(): String =
        jdbcClient.sql("select status from orders where id = :id")
            .param("id", orderId)
            .query(String::class.java)
            .single()

    @Test
    fun `2 件目の引き当ての戻しで例外が出ると、1 件目の戻しも残らない`() {
        insertProduct(notebook, "ノート", 330, inventoryQuantity = 48)
        insertProduct(marker, "マーカー", 880, inventoryQuantity = 2)
        insertOrder()

        assertThrows<IllegalStateException> { orderService.cancel(orderId) }

        assertEquals(48, inventoryQuantity(notebook))
        assertEquals(2, inventoryQuantity(marker))
        assertEquals("PENDING", orderStatus())
    }
}
