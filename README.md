# StageBuilder
A simple stage builder annotation processing library for Pojo Classes and Records.

## Supported Annotations

| Annotation                  | Target Level                                 | Description                                                                                                                                                                                                 |
|-----------------------------|----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@StageBuilder`             | Class, Record                                | Marks a class or record for which a stage builder should be generated. Place this annotation on your POJO or record to enable builder generation.                                                           |
| `@StageBuilder.Default`     | Field, Constructor Param, Record Component   | Marks a field, constructor parameter, or record component as having a default value. The generated builder allows this field to be omitted; if not set, the default value is used during object construction. |
| `@StageBuilder.Optional`    | Field, Constructor Param, Record Component   | Marks a field, constructor parameter, or record component as optional. The generated builder allows this field to be skipped; if not set, it will be `null` (for reference types) or the Java default (for primitives). |

## Get Started

## Get Started

1. **Add the dependency** to your `build.gradle`:
   ```groovy
   dependencies {
       annotationProcessor 'org.devnuxs:stagebuilder-processor:1.0.0'
       implementation 'org.devnuxs:stagebuilder-api:1.0.0'
   }
   ```



### Examples

#### Example 1: Record Component Annotations
```java
import org.devnuxs.stagebuilder.api.StageBuilder;

@StageBuilder
public record PersonRecord(String name, int age, @StageBuilder.Default("default@email.com") String email) {}

// Usage:
PersonRecord person = PersonRecordStageBuilder.builder()
    .name("John")
    .age(30)
    .build(); // email will be set to default
```

This will generate a stage builder where `email` can be skipped (defaults to `"default@email.com"` if not provided).

#### Example 2: Constructor Parameter Annotations
```java
@StageBuilder
public class Person {
    private final String name;
    private final int age;
    private final String email;
    private String phone;

    public Person(String name, int age,
        @StageBuilder.Default("default@email.com") String email,
        @StageBuilder.Optional String phone) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
    }
}
```
This will generate a stage builder where `email` can be skipped (defaults to `"default@email.com"` if not provided), and `phone` can be skipped (will be `null` if not provided).

#### Example 3: Private Field Annotations with Setter Methods
```java
@StageBuilder
public class User {
    @StageBuilder.Optional
    private String nickname;

    private String name;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ... constructor, other fields, etc ...
}
```
In this example, the builder will recognize the `@StageBuilder.Optional` annotation on the private field `nickname` (because there is a public setter), and will allow you to skip setting it. The builder will call the setter for `nickname` if it is set during the build process.

#### Example 4: Creating Variants with the `from()` Builder Method
```java
@StageBuilder
public record Person(String name, int age, @StageBuilder.Optional String email) {}

// Create an original object
Person original = PersonStageBuilder.builder()
    .name("John Doe")
    .age(30)
    .build();

// Create a new variant with modified fields using from()
Person older = PersonStageBuilder.from(original)
    .age(31)
    .build();

// Create another variant with additional field
Person withEmail = PersonStageBuilder.from(original)
    .email("john.doe@example.com")
    .build();

// Modify multiple fields at once
Person updated = PersonStageBuilder.from(original)
    .name("John Smith")
    .age(32)
    .email("john.smith@example.com")
    .build();
```

The `from()` method is useful when you want to create new objects that are similar to existing ones with just a few field changes, avoiding the need to manually specify all field values again.