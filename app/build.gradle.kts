plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.flyway)
    // Flyway は PostgreSQL への対応を別のモジュールに分けている。starter だけでは PostgreSQL に当てられない。
    implementation(libs.flyway.database.postgresql)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.data.jdbc.test)
    /*
     * Flyway の test 用の starter (spring-boot-starter-flyway-test) は、start.spring.io の生成物と違って足さない。
     * 本体の Flyway の starter を依存に含むので、足すと本体の starter が抜けても、テストの classpath に
     * Flyway の自動設定 (spring-boot-flyway) が残り、起動確認のテスト (ShopApplicationTests) が通ってしまう。
     * flyway-test が持つほかの依存 (starter-test と starter-jdbc-test) は、data-jdbc-test も持つ。
     */
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    compilerOptions {
        /*
         * -Xjsr305=strict: JSR-305 の null 許容の注釈を付けた Java の API を、注釈どおりの null 許容の型として
         * 扱う。付けないと誤った使い方が警告に留まり、型はプラットフォーム型のままになる。Spring 自身は
         * JSpecify の注釈を使っており、Kotlin はこちらを付けなくても厳密に扱う。
         * -Xannotation-default-target=param-property: 対象を書かずにコンストラクタのプロパティへ付けた注釈を、
         * 引数と、プロパティ (プロパティに付けられない注釈はフィールド) の両方に付ける。
         * Spring Boot の Kotlin の文書が推奨する設定。
         */
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
