-- 練習問題の解答例と比べるための、null を許さない形。既にある行が値を持たないので、当てると失敗する。
alter table products add column description varchar(500) not null;
