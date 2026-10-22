/*
 * プラグインはルートで一度だけ読み込み、各サブプロジェクトが適用する。app と kotlin-basics がそれぞれ
 * 版を書いて Kotlin プラグインを読み込むと、版が同じでも、Kotlin プラグインは「サブプロジェクトごとに
 * 複数回読み込まれた」という警告を出す。
 */
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}
