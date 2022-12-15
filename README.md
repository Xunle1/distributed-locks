# distributed-locks

This repository contains the projects of learning distributed-locks

## 分布式锁

### 传统锁

-   JVM 本地锁

    >   三种情况下会失效：多例模式、事务操作、集群部署

-   MySQL 悲观锁

    >   使用 DML 操作或者 select for update 会触发锁机制
    >
    >   如果是查询或更新索引字段那么会使用行级锁，索引失效时会使用表级锁
    >
    >   适用写并发量较高的情况

-   MySQL 乐观锁

    >   使用版本号机制
    >
    >   适用读多写少，争抢不是很激烈的情况

性能的比较：悲观锁 > JVM 本地锁 > 乐观锁



---

### Redis 分布式锁

-   **使用 setnx 实现**

    >   1.  防止死锁：设置过期时间
    >   2.  设置过期时间原子性：使用 set key value ex time nx 实现
    >   3.  防止误删除：使用 UUID 防止误删
    >   4.  自动续期

-   **防误删原子性**

    >   防误删需要先判断是否自己持有锁，再进行删除。如果在判断后时锁过期，则会导致删除其他线程持有的锁
    >
    >   使用 lua 脚本实现原子操作
    >
    >   在 Redis 中使用 lua 脚本：`EVAL script numkeys key [keys…] arg [args…]`
    >
    >   lua 防误删操作：
    >
    >   ```lua
    >   if (redis.call('get', 'lock') == ARGV[1]) then redis.call('del', 'lock') end
    >   ```
