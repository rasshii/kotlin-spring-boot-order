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
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.postgresql)
    // bootRun のときだけ classpath に入り、app/compose.yaml の PostgreSQL を立てて接続先にする。jar には入らない。
    developmentOnly(libs.spring.boot.docker.compose)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.data.jdbc.test)
    /*
     * Flyway の test 用の starter (spring-boot-starter-flyway-test) は、start.spring.io の生成物と違って足さない。
     * 本体の Flyway の starter を依存に含むので、足すと本体の starter が抜けても、テストの classpath に
     * Flyway の自動設定 (spring-boot-flyway) が残り、起動確認のテスト (ShopApplicationTests) が通ってしまう。
     * flyway-test が持つほかの依存 (starter-test と starter-jdbc-test) は、data-jdbc-test も持つ。
     * 検証の test 用の starter (spring-boot-starter-validation-test) も足さない。中身は starter-test と本体の検証の
     * starter で、starter-test は data-jdbc-test が持ち、本体の starter は上で足しているので、増えるものが無い。
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
         * 扱う。start.spring.io の生成物と同じ設定。Spring 自身の null 許容の注釈 (JSpecify) は、Kotlin 2.1 以降、
         * このフラグが無くても厳密に扱われる (Spring Boot の Kotlin の文書)。
         * -Xannotation-default-target=param-property: 対象を書かずにコンストラクタのプロパティへ付けた注釈を、
         * 引数と、プロパティ (プロパティに付けられない注釈はフィールド) の両方に付ける。Kotlin 2.2 の新しい
         * 規則に伴う警告を避けるために、Spring Boot の Kotlin の文書が推奨する設定。
         * -Xemit-jvm-type-annotations: 型に付けた Java の注釈 (List<@Valid CreateOrderLineRequest> の @Valid) を
         * bytecode に出す。付けないと Kotlin はこの注釈を出さず、Hibernate Validator がリストの要素を検証しない。
         */
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property",
            "-Xemit-jvm-type-annotations",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootRun {
    // 手元で動かすときの設定 (application-dev.yaml) を使う。jar を java -jar で動かすときは付かない。
    systemProperty("spring.profiles.active", "dev")
}
