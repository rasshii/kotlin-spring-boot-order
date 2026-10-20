# kotlin-spring-boot-order

[Kotlin × Spring Boot API 開発ガイド](https://notes.rewheel.dev/docs/kotlin-spring-boot-api/) のサンプルコードです。EC サイトの注文システムを Kotlin と Spring Boot で作ります。

## 構成

| ディレクトリ | 中身 |
| --- | --- |
| `kotlin-basics/` | ガイド第 1 部 (Kotlin 入門) のコード例。Spring に依存しません。コード例はテストとして置いてあり、記事に載せた実行結果と出力を比べます |
| `app/` | Spring Boot のアプリケーション。ガイド第 2 部から作っていきます |

ガイドの記事は、main ではなく tag を指してこのリポジトリにリンクします。`api-06` は第 6 章まで読んだ時点のコードです。

## 動かすのに要るもの

- JDK 25
- Docker (`app/` のテストが PostgreSQL をコンテナで立てます)

Gradle は同梱の Gradle Wrapper (`./gradlew`) が取得するので、別に入れる必要はありません。

## 動かし方

```bash
# 第 1 部のコード例をすべて動かす
./gradlew :kotlin-basics:test

# すべてのテストを動かす (Docker が要る)
./gradlew build
```

## 版

| 項目 | 版 |
| --- | --- |
| Spring Boot | 4.1.1 |
| Kotlin | 2.3.21 |
| Java | 25 |

版は `gradle/libs.versions.toml` にまとめています。

## ライセンス

[MIT](LICENSE)
