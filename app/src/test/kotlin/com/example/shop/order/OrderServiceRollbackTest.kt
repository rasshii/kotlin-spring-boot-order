package com.example.shop.order

import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
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
import java.util.concurrent.atomic.AtomicInteger

/**
 * 本物の在庫の repository に委譲し、2 回目の save でだけ例外を投げる。注文を作る途中や取り消す途中で、データベースの
 * 障害や楽観ロックの失敗が起きた場面を作る。
 */
class FailOnSecondSaveInventoryRepository(
    private val delegate: InventoryRepository,
) : InventoryRepository by delegate {
    private val saves = AtomicInteger()

    override fun <S : Inventory> save(entity: S): S {
        if (saves.incrementAndGet() == 2) {
            throw IllegalStateException("2 回目の在庫の保存で失敗させる")
        }
        return delegate.save(entity)
    }
}

/**
 * 注文を作る途中で例外が出たら、それまでの書き込みが戻ることを確かめる。
 *
 * テストのメソッドを @Transactional にすると、サービスのトランザクションがテストのトランザクションに加わり、
 * サービスの境界でのロールバックを確かめられない。このクラスはテストのトランザクションを使わない。
 */
@Import(TestcontainersConfiguration::class, OrderServiceRollbackTest.FailingInventoryConfiguration::class)
@SpringBootTest
class OrderServiceRollbackTest(
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

    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val marker = UUID.fromString("0199f3a8-0000-7000-8000-000000000004")

    private fun insert(productId: UUID, name: String, price: Int, inventoryQuantity: Int) {
        // 差し替えた repository の save を数えないよう、準備の行は SQL で入れる。
        jdbcClient.sql("insert into products (id, name, price, tax_rate, version) values (:id, :name, :price, 10, 0)")
            .param("id", productId).param("name", name).param("price", price).update()
        jdbcClient.sql("insert into inventories (product_id, quantity, version) values (:id, :quantity, 0)")
            .param("id", productId).param("quantity", inventoryQuantity).update()
    }

    private fun inventoryQuantity(productId: UUID): Int =
        jdbcClient.sql("select quantity from inventories where product_id = :id")
            .param("id", productId)
            .query(Int::class.java)
            .single()

    @Test
    fun `2 件目の引き当てで例外が出ると、1 件目の引き当ても戻る`() {
        insert(notebook, "ノート", 330, inventoryQuantity = 50)
        insert(marker, "マーカー", 880, inventoryQuantity = 3)
        val command = CreateOrderCommand(
            contactEmail = "taro@example.com",
            lines = listOf(CreateOrderCommand.Line(notebook, 2), CreateOrderCommand.Line(marker, 1)),
        )

        assertThrows<IllegalStateException> { orderService.create(command) }

        assertEquals(50, inventoryQuantity(notebook))
        assertEquals(3, inventoryQuantity(marker))
    }
}
