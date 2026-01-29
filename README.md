Библиотека с готовой и продвинутой реализацией самых частоиспользуемых вещей в Paper плагинах.

## Система переводов

Необходимость каждый раз создавать систему переводов было главной причиной создания OutbreakLib.

OutbreakLib реализует системы загрузки из yaml файлов или базы данных, и предоставляет возможность в удобном виде
группировать и использовать переводы.

Под капотом используется Kyori Adventure с MiniMessage и их Translator, что позволяет использовать
`TranslatableComponent` с автоматическим выбором нужного языка при отправке сообщений игрокам.

При использовании OutbreakLib как плагина, рекомендуется использовать глобальный объект `GlobalLocaleData` в качестве
хранилища переводов. В плагине уже реализована загрузка в него переводов из базы данных, указанной в `db.properties`,
которая ожидается как глобальная база для всех переводов всех плагинов.

### Структура данных

Переводы в OutbreakLib хранятся будучи сгруппированными по языкам и (исключительно логически) по пространству имён.

Язык под капотом является `java.util.Locale` и обычно ожидается как строка вида `ru_RU` из кода языка и кода страны.

Пространство имён - обычная строка, используемая для определения принадлежности к конкретному плагину или группе
переводов. Может состоять из латиницы в нижнем регистре, цифр, `_` и `-`.

Ключ - краткое кодовое название переводимого сообщения. Обычно всегда идёт вместе с пространством имён, через `:`.

В реализации используется класс `Key` из Adventure, так что в действительности пространства и ключи хранятся вместе, без
отдельных `Map`.

Пример:

```
ru_RU
├─ outbreaklib:reloaded : "<green>Конфиг перезагружен!"
└─ otherplugin:example-message : "<white>Пример сообщенеия"
en_US
├─ outbreaklib:reloaded : "<green>Configuration reloaded!"
└─ otherplugin:example-message : "<white>Example message"
```

### Источники загрузки переводов

В OutbreakLib есть реализации для двух источников: `.yml` файлы и SQL-база данных.

Источники прикрепляются к объектам `LocaleData`, которые хранят в себе

#### YAML

Источник `YamlDirectoryLocaleSource` ожидает папку, в которой находятся файлы следующего вида:

```
ru_RU.yml
en_US.yml
fr_FR.yml
```

`YamlDirectoryLocaleSource` автоматически парсит названия файлов как языки и загружает из них все данные. Пространство
имён для привязки к ним указывается вручную.
Пример:

```kotlin
YamlDirectoryLocaleSource(
    "outbreaklib", // пространство имён
    dataFolder.resolve("messages").resolve("locales") // путь к папке с .yml
)
```

```kotlin
val res = Res(this.javaClass.classLoader)
val ld = GlobalLocaleData

fun onLoad() {
    // Извлекаем 
    res.extract("messages", dataFolder)

    yamlConfigsLocaleSource = YamlDirectoryLocaleSource(
        "outbreaklib",
        dataFolder.resolve("messages").resolve("locales")
    )
}


// onEnable()
res.extract("messages", dataFolder)

```

## API для ресурсов JAR