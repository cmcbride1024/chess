# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

[Server Design](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDAEooDmSAzmFMARDQVqhFHXyFiwUgBF+wAIIgQKLl0wATeQCNgXFDA3bMmdlAgBXbDADEaYFQCerDt178kg2wHcAFkjAxRFRSAFoAPnJKGigALhgAbQAFAHkyABUAXRgAegt9KAAdNABvfMp7AFsUABoYXDVvaA06lErgJAQAX0xhGJgIl04ePgEhaNF4qFceSgAKcqgq2vq9LiaoFpg2joQASkw2YfcxvtEByLkwRWVVLnj2FDAAVQKFguWDq5uVNQvDbTxMgAUQAMsC4OkYItljAAGbmSrQgqYb5KX5cAaDI5uUaecYiFTxNAWBAIQ4zE74s4qf5o25qeIgab8FCveYw4DVOoNdbNL7ydF3f5GeIASQAciCWFDOdzVo1mq12p0YJL0ilkbQcSMPIIaQZBvSMUyWYEFBYwL53hUuSgBdchX9BqK1VLgTKtUs7XVgJbfOkIABrdBujUwP1W1GChmY0LYyl4-UTIkR-2BkNoCnHJMEqjneORPqUeKRgPB9C9aKULGRYLoMDxABMAAYW8USmWM+geugNCYzJZrDZoNJHjBQRBOGgfP5Aph62Ei9W4olUhlsjl9Gp8R25SteRsND1i7AEzm9XnJjAEFOkGgbd75Yf+dncZeDXSYyaYI8Xm99wdH5hRdQFyDBCFZQ+O14URL1o0dWNayGd9ThTFBiVJckdSpZNCUNS5vzuU0UFZC0rUfT4EOA51IldSVpSg215S7CsZ3VTUy2op043PVDqXQ0t0zYt9dTQ-DkNPISrW7LNT2QxdGxgVt21KVjM17NB+1McwrFsMwUFDSd2EsZgbD8AIgmQBt-ikxIZAg9JgU3bcuF3exhMzKtqBrPixIE-D4lvEzLTmdT0AOHDc0-I0iMZX8nnI61wrQICeJFMCQXBSE0xktjYIgJEuONEC-Nwq9UxJMlRPKmLCMQn8NBQBAnhQJLKLtNKkNA+IHPBJzcvLTMCqK-1uO6srosEmqpokwsoh8uJvP6ebFObNtMD7AddOHaYNAnNwYAAcTtTFzLnKyQmYQY7ISI7gSyXJ2DtYoUuW3zIiij9BJvNwTuqLgws8iKZu+ub6po+4ErAdqUq6jEMqBCCcpSkbBvGhH5q+8T80qrCYFBnGCwhniHief7VDmeGQLozLkahZ7qjFGQ6gswJ2SfFZtAQUAgw55YamKRmUAlGCEUK39TpMErnUmsHcYwyWmZkLhWYu-nTrqYpud5jWAbqYXRYBwmAoVySVweO1mdVmA2bZaD9ZgbWeZAPmHdUA27SN1R3rPQY1uUtsO2F621csvWPadtAdddiObcN07NO0nSh1sbALCgbAWvgM0DAp2dLIXaylwWmJ4mSNJHpyYXXuBtBg69u0TxXf5sdN69mVIwIKaBvLM096pvcixN5eJmAZahv9Ybr6naIBJHsqhVHxdGqMJ+Qtu8IVzCyQJzeKoI8e4sn8m7TmBPqlnuNaYXyClZFsW4OF9R16xkeibx3eTH3uqj4a4iYCd1ZBTZm59G6XwxjTeebpGL32ZmGTUIcVYmy3mPCeJFgFnwvisJBV9EYwI9AzcBOCrYyAQffF+x8N7v3bqmJBKCD7m0WpbZWvsFLFyUipBuysk7bVTjYRwzVbzeBgAAKQgPeY6MEbAxyDEXK6tkLbJGeFXGuHk+7oA7FnYAgioBwAgLeKAA8UDM2botVuNDUFEgAFYSLQD3FKPIea6P0YY4xzNh4Xg-ofdB0Np4aNSpAuerosp333L6OuaNipULfl42hisqoID3pYxhsV-7xT-CAmQYDlZ4J6uBRecCyErwoUE3in0UkGh3kk7+lT0JfnSfcWx94e7hPqM4ygrjoDuJkHkm+BDPRtO0S4gx0ByHPzKdQuJViMIMJijdC2bDVocPWi2TaWk+F6RsGYHR8AWp4AjNgLOhBLwF3nIpRRzD7KOWcrkIwSzwg-x+iAfZUAFBHOQCAS8VM5n1IiL4l5pEoAc0Bn06BfUHrAi9ALBUfJNjKl2GjRYkzYn8RmdU5J0zUkk1jEyV5FNQUov6RCgawselh3Zu7OosiI6CzQNgtGEynngz-pDPFQKsmEtfsSm5RTGWkMoY0qZaKD4Yt+eDAFrykqyS5TE4lzVWqDVknUfcUSxqvzlt42I4qzbzSkks-2KzA5rK2kAA)

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared tests`     | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

### Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

