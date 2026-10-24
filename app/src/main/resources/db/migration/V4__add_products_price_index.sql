/*
 * 商品の一覧を価格の順に読むための索引。並びの項目 (price) の後ろに主キー (id) を足し、一覧の
 * ORDER BY p.price, p.id と、続きの位置の条件 (p.price, p.id) > (...) の両方を、索引の順で読めるようにする。
 */
create index products_price_id_idx on products (price, id);
