# Data Object

- 数据对象所定义的是 `Pigeon` 需要进行持久化的相关 model
- 为了与具体实现解耦，`pigeon-core` 仅将所需字段定义为 getter&setter interface，因此
    - 主程序可以在提供 core 所必须的字段外自主决定具体的数据对象结构（如 表名， id 生成规则，字段名，自定义字段如 creation time, 索引 等）
    - 主程序可自主决定采用的 ORM 类型（如 mybatis, hibernate...）
    - 主程序可自主决定采用的 DB 类型，可以是 MySQL, PgSQL 甚至是 MongoDB 等
