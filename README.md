# default-discord-bot

![Java Version](https://img.shields.io/badge/java-11-blue)
[![GitHub License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Discord bot aimed at working with media content written in Java.

The main task of this bot is to organize the process of approving and publishing media content in different categories.
Users suggest content, then moderators decide whether to approve it or not. Approved content is queued up for
publication in the Discord text channel.

- [Installation using Docker](#installation-using-docker)
- [Build](#build)
- [Configuration](#configuration)
- [Possible problems](#possible-problems)
- [Commands](#commands)

---

## Installation using Docker

- Download and install the latest stable version of Docker from [official site](https://docs.docker.com/get-docker/).

- Define required environment variables to configure the application. More about configuration process can be
  found [here](#configuration). Notice that by default for connecting to database following environment variables are
  needed:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQL10Dialect
```

- Go to the project directory and run:

```
$ docker-compose up -d
```

## Build

- Download and install the latest stable version of Maven from [official site](https://maven.apache.org/download.cgi).

- Configure the application. More about configuration process can be found [here](#configuration).

- Go to the project directory and build:

```
$ mvn clean package spring-boot:repackage
```

## Configuration

Application properties have the "ddb" prefix. An example of the configuration can be found
in [application.yml.template](src/main/resources/application.yml.template) file. Also, configuration can be done using
environment variables, an example of which can be found in [.env.template](.env.template) file.

- token `Type: String`

  The token of the discord bot.

- defaults `Optional`

  The section of properties responsible for default values.

  - prefix `Type: String` `Optional`

    Default value is `"!"`.

    The guild commands prefix.

  - positive-approval-emoji `Type: Character` `Optional`

    Default value is `'✅'`.

    The category positive approval emoji.

  - negative-approval-emoji `Type: Character` `Optional`

    Default value is `'❎'`.

    The category negative approval emoji.

- discord-event-pool-size `Type: Integer` `Optional`

  Default value is `0`.

  The event pool size of the discord bot. Must be greater than or equal to `0`. When `0`, the number of available
  processors is used (`Runtime#availableProcessors()`).

- max-discord-reconnect-delay `Type: Integer` `Optional`

  Default value is `64`.

  The maximum reconnection delay of the discord bot in seconds. Must be greater than or equal to `64`.

- max-vk-reconnect-delay `Type: Integer` `Optional`

  Default value is `64`.

  The maximum reconnection delay of the vk bot in seconds. Must be greater than or equal to `64`.

## Possible problems

### Found non-empty schema(s) but no schema history table:

The problem occurs after upgrading from versions 1.0.3.0 and 1.1.3.0 to 1.2.3.0 and higher.
Can be solved by setting an environment variable:

```
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
```

After the first successful start of the application, this environment variable can be removed.
More can be found [here](https://flywaydb.org/documentation/configuration/parameters/baselineOnMigrate).

## Commands

The bot can perform certain actions after typing corresponding commands in the Discord text channels. For the bot to
respond to the command, it must be preceded by a prefix.

- ping

  Sends the answer "...pong" to the same text channel. Used to check if the bot is online.

- qsize

  Sends a message containing the number of images in the queue for the corresponding categories.

  List the names of the required categories separated by a space to send information about the corresponding categories,
  otherwise send information about all of them.

- connections

  Initiates a connections session in the DM's with the bot.

  Connections are used for additional features within other services (e.g. vk, telegram, twitter, etc.).

- config

  > The user typing this command must have administrator rights on the corresponding discord server.

  Initiates a configuration session in the DM's with the bot.

## License

default-discord-bot is licensed using the MIT License, as found in the [LICENSE](LICENSE) file.
