# EasyData

**一行代码，填满复杂测试对象。**

EasyData 是面向 Java 开发者的零配置测试数据生成器。无需 YAML、无需 Provider 链，2 分钟上手。

```java
User user = EasyData.generate(User.class);
```

## 特性

- 一行 API，零配置
- 支持基础类型、嵌套对象、`List` / `Set` / `Map`
- 字段忽略 `@EasyData.Ignore`
- 编译期固定值 `@EasyData.Field("张三")`
- Builder 自定义字段与集合大小
- JUnit 5 扩展 `@GenerateData`
- **编译期校验**：独立 `easy-data-processor` 模块（不进入运行时 classpath）
- JDK 8+，基于 [Java Faker](https://github.com/DiUS/java-faker)

## 模块结构

```
easy-data/                     # 仓库根目录 (easy-data-parent)
├── pom.xml
├── easy-data-core/            # 运行时模块，artifactId: easy-data-core
└── easy-data-processor/       # 编译期处理器
```

| 模块 | Maven 坐标 | 作用 |
|------|-----------|------|
| `easy-data-core` | `com.easydata:easy-data-core` | 运行时生成逻辑 + 注解 |
| `easy-data-processor` | `com.easydata:easy-data-processor` | 编译期 `@EasyData` 校验 |

## 快速开始

### Maven

运行时依赖：

```xml
<dependency>
    <groupId>com.easydata</groupId>
    <artifactId>easy-data-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

编译期校验（推荐，不进运行时 classpath）：

```xml
<dependency>
    <groupId>com.easydata</groupId>
    <artifactId>easy-data-processor</artifactId>
    <version>1.0.0</version>
    <optional>true</optional>
    <scope>provided</scope>
</dependency>
```

或使用 `annotationProcessorPaths`（更干净，处理器完全隔离）：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.easydata</groupId>
                <artifactId>easy-data-processor</artifactId>
                <version>1.0.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Gradle

```gradle
dependencies {
    implementation 'com.easydata:easy-data-core:1.0.0'
    annotationProcessor 'com.easydata:easy-data-processor:1.0.0'
}
```

### 最简用法

```java
User user = EasyData.generate(User.class);
```

### Builder 风格

```java
User user = EasyData.of(User.class)
    .field("name", "张三")
    .size("orders", 5)
    .ignore("password")
    .generate();

List<User> users = EasyData.of(User.class).generate(10);
```

### 注解固定字段值

```java
@EasyData
public class User {
    @EasyData.Field("张三")
    private String name;

    @EasyData.Field("18")
    private int age;

    @EasyData.Ignore
    private String password;
}
```

Builder `.field(...)` 优先级高于 `@EasyData.Field`。

### 忽略字段

```java
@EasyData
public class User {
    @EasyData.Ignore
    private String password;
}
```

### 编译期字段校验

引入 `easy-data-processor` 后，编译阶段自动校验：

| 规则 | 级别 | 说明 |
|------|------|------|
| `@EasyData` 目标类 | ERROR | 必须是具体类，且有 no-arg 构造器 |
| 嵌套 POJO 字段 | ERROR | 不能是 interface / abstract；需 no-arg 构造器 |
| `@EasyData.Ignore` | ERROR | 不能标注 static / final 字段 |
| `@EasyData.Field` | ERROR | 仅支持标量类型；值必须可解析；不可与 `@Ignore` 同用 |
| `defaultSize` | WARNING | 必须 > 0 |
| final 实例字段 | WARNING | 运行时无法填充 |
| raw 集合类型 | WARNING | 建议使用 `List<User>` 等参数化类型 |

### JUnit 5 集成

```java
@Test
@GenerateData
void testCreateUser(User user) {
    assertNotNull(user.getName());
}
```

## 实现要点

### 嵌套对象递归

`DataGenerator.populate()` 反射遍历所有字段，对 POJO 类型委托 `ObjectFieldGenerator` → 再次调用 `DataGenerator.generate()`。

### 泛型 List&lt;T&gt;

通过 `Field.getGenericType()` 读取 `ParameterizedType`，提取元素类型后递归生成。

### 避免无限递归

- **类型级**：`generatingTypes` 检测 `A → B → A`
- **实例级**：`visiting`（IdentityHashMap）防止重复 populate

### JDK 8 时间类

`DateTimeGenerator` 用 Faker 生成 `Date`，再转换为 `LocalDate` / `LocalDateTime`。

## 与竞品对比

| 工具 | 特点 | EasyData 优势 |
|------|------|---------------|
| Instancio | API 复杂 | 一行代码搞定 |
| Podam | 配置繁琐 | 零配置 |
| MockNeat | 长期不维护 | 持续维护、Apache 2.0 |

## 构建与发布

```bash
mvn clean verify          # 编译 + 全模块测试
mvn clean install         # 本地安装
mvn clean deploy -Prelease  # 发布（需 GPG）
```

## 路线图

- [x] 独立 `easy-data-processor` 模块
- [x] `@EasyData.Field` 编译期固定值
- [ ] 自定义 Provider SPI
- [ ] Kotlin / Records 支持

## License

Apache License 2.0
