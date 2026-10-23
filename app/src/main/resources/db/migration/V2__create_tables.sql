/*
 * 商品・在庫・注文・注文明細のテーブル。
 * 主キーの UUID はアプリで作る (データベースの既定値に頼らない)。
 * version の列は、Spring Data JDBC が新規の行か既存の行かを見分けるのと、楽観ロックに使う。
 * 金額は円の整数で持つ。価格は消費税を含む。
 */
create table products (
    id uuid primary key,
    name varchar(100) not null,
    price integer not null check (price >= 0),
    tax_rate integer not null check (tax_rate in (8, 10)),
    version bigint not null
);

-- 在庫は商品ごとに 1 行。主キーを商品の ID にする。
create table inventories (
    product_id uuid primary key references products (id),
    quantity integer not null check (quantity >= 0),
    version bigint not null
);

create table orders (
    id uuid primary key,
    contact_email varchar(254) not null,
    status varchar(20) not null check (status in ('PENDING', 'CANCELLED')),
    ordered_at timestamp with time zone not null,
    version bigint not null
);

/*
 * 注文明細は注文の一部なので、注文の ID と注文の中の位置 (0 から) を主キーにする。
 * 単価と税率は、注文した時点の商品の値を写す。後で税率が変わっても注文の行は変えないので、
 * 商品の側の税率の制約 (8 か 10) をここには付けない。
 */
create table order_lines (
    order_id uuid not null references orders (id),
    line_index integer not null,
    product_id uuid not null references products (id),
    quantity integer not null check (quantity > 0),
    unit_price integer not null check (unit_price >= 0),
    tax_rate integer not null check (tax_rate >= 0),
    primary key (order_id, line_index),
    unique (order_id, product_id)
);
