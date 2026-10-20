package basics.ch02.packages

import basics.assertSnippetOutput
import com.example.shop.order.main as orderMain
import org.junit.jupiter.api.Test

/**
 * `ch02-package-order` の例は、記事に見せる package 行 (`com.example.shop.order`) を目印の中に含む。
 * 1 つのファイルは 1 つの package にしか属せないので、テストは別のファイルに置く。
 * 01 章が案内する `--tests 'basics.ch02.*'` の絞り込みに入るよう、package は basics.ch02 の下にする。
 */
class PackageOrderTest {
    @Test
    fun packageOrder() = assertSnippetOutput("ch02-package-order") { orderMain() }
}
