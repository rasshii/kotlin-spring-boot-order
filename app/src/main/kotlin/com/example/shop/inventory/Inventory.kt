package com.example.shop.inventory

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

/** 商品ごとに持つ、注文に引き当てられる数量。商品の ID を主キーにする。 */
@Table("inventories")
data class Inventory(
    @Id val productId: UUID,
    val quantity: Int,
    @Version val version: Long? = null,
) {
    fun canAllocate(amount: Int): Boolean = quantity >= amount

    /**
     * [amount] 個を引き当てた在庫を返す。
     *
     * 足りるかは、呼ぶ側が [canAllocate] で先に確かめる。足りない在庫に呼ぶのはプログラムの誤りなので、check で止める。
     */
    fun allocate(amount: Int): Inventory {
        require(amount > 0) { "引き当てる数量は 1 以上にしてください: $amount" }
        check(canAllocate(amount)) { "在庫が足りません: $productId は $quantity 個、引き当てる数量は $amount 個" }
        return copy(quantity = quantity - amount)
    }

    /** 引き当てた [amount] 個を戻した在庫を返す。 */
    fun deallocate(amount: Int): Inventory {
        require(amount > 0) { "引き当てを戻す数量は 1 以上にしてください: $amount" }
        return copy(quantity = quantity + amount)
    }
}
