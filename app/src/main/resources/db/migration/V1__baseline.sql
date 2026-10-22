/*
 * マイグレーションの起点。テーブルは第 2 部で V2 以降として足す。
 * 起動確認のテスト (ShopApplicationTests) は、このファイルが version 1 として履歴 (flyway_schema_history) に
 * 成功で記録されたことで、Flyway が起動時にマイグレーションを当てたことを確かめる。
 */
