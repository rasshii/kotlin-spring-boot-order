-- 既にある行には説明が無いので、null を許す列として足す。
alter table products add column description varchar(500);
