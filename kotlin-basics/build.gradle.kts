/*
 * API ガイド第 1 部 (Kotlin 入門) のコード例を動かすサブプロジェクト。Spring に依存しない。
 * コード例はすべてテストの側 (src/test/kotlin) に置き、記事に載せた実行結果を
 * src/test/resources/snippets/<名前>.out と比べる。
 */
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    // compile エラーになる例 (src/test/compile-errors) を、テストの中でコンパイラに掛けるために使う。
    testImplementation(libs.kotlin.compiler.embeddable)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    // コンパイラを同じ JVM の中で動かすので、既定の最大ヒープでは足りないことがある。
    maxHeapSize = "1g"
    /*
     * CompileErrorSnippetsTest が実行時に読む compile エラーの例は、source set に入っていない。
     * 入力として宣言しないと、例だけを直したときに test が UP-TO-DATE と判定されて回らない。
     */
    inputs.files(fileTree("src/test/compile-errors"))
        .withPropertyName("compileErrorSnippets")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
