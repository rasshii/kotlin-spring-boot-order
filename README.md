# kotlin-spring-boot-order

[Kotlin × Spring Boot API 開発ガイド](https://notes.rewheel.dev/docs/kotlin-spring-boot-api/) のサンプルコードです。EC サイトの注文システムを Kotlin と Spring Boot で作ります。

## 構成

| ディレクトリ | 中身 |
| --- | --- |
| `kotlin-basics/` | ガイド第 1 部 (Kotlin 入門) のコード例。Spring に依存しません。コード例はテストとして置いてあり、記事に載せた実行結果と出力を比べます |
| `app/` | Spring Boot のアプリケーション。ガイド第 2 部から作っていきます |

ガイドの記事は、main ではなく tag を指してこのリポジトリにリンクします。`api-06` は第 6 章まで、`api-07` は第 7 章まで読んだ時点のコードです。第 2 部からは、アプリが変わる章ごとに tag を付けています。

## 動かすのに要るもの

- JDK 25
- Docker (`app/` を起動すると PostgreSQL をコンテナで立てます。テストも PostgreSQL をコンテナで立てます)

Gradle は同梱の Gradle Wrapper (`./gradlew`) が取得するので、別に入れる必要はありません。

## 動かし方

```bash
# 第 1 部のコード例をすべて動かす
./gradlew :kotlin-basics:test

# アプリを起動する (Docker が要る。止めるときは Ctrl+C)
./gradlew :app:bootRun

# すべてのテストを動かす (Docker が要る)
./gradlew build
```

`bootRun` は `dev` の profile (`app/src/main/resources/application-dev.yaml`) で起動し、`app/compose.yaml` の PostgreSQL を Docker Compose で立てて接続します。アプリを止めるとコンテナも止まり、データは次の起動まで残ります。

## 版

| 項目 | 版 |
| --- | --- |
| Spring Boot | 4.1.2 |
| Kotlin | 2.3.21 |
| Java | 25 |

ライブラリとプラグインの版は `gradle/libs.versions.toml` にまとめています。Java の版は、`app/build.gradle.kts` と `kotlin-basics/build.gradle.kts` の toolchain で指定しています。

## ライセンス

[MIT](LICENSE)
