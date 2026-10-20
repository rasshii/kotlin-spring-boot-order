/*
 * プラグインはルートで一度だけ読み込み、各サブプロジェクトが適用する。app と kotlin-basics がそれぞれ
 * 版を書いて Kotlin プラグインを読み込むと、プラグインがサブプロジェクトごとに別のクラスローダーに載る。
 * Kotlin プラグインはこの読み込み方を想定しておらず、警告を出す。
 */
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}
