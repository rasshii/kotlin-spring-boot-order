rootProject.name = "kotlin-spring-boot-order"

dependencyResolutionManagement {
    // 依存の取得先はここだけで決める。サブプロジェクトが別の取得先を足すとビルドを失敗させる。
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

include("app", "kotlin-basics")
