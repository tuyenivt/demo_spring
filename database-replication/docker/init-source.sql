CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED WITH mysql_native_password BY 'repl';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';

CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED WITH mysql_native_password BY 'app';
GRANT SELECT, INSERT, UPDATE, DELETE ON demo_db.* TO 'app'@'%';

FLUSH PRIVILEGES;
