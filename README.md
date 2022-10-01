# Lucenesaur

Project Lucenesaur is a derivative of apache lucene library and word -saur (from dinosaur)

The project works on providing support for new languages that have been added in newer versions of apache lucene library. 
But old systems using the existing apache-lucene versions are not able to update to newer apache library due to a lot of conflicts

## Jflex
The project uses jflex to generate the Basic Tokenizer. 

To generate java file from jflex, follow following guidelines-

1. Follow [JFlex official documentation](https://www.jflex.de/manual.html#Installing) to download and unzip jflex library
2. Decompress into directory of choice, for example
    ```shell
    tar -C /usr/share -xvzf jflex-1.8.2.tar.gz
    ```
3. Add symbolic link to `jflex-1.8.2/bin/jflex` file
    ```shell
    ln -s /usr/share/jflex-1.8.2/bin/jflex /usr/bin/jflex
    ```
4. From root directory, run
    ```shell
    jflex src/main/java/io/github/rajat19/analysis/indic/IndicTokenizerImpl.jflex
    ```
   
---
## Open source integration

Using [Gradle test logger plugin](https://github.com/radarsh/gradle-test-logger-plugin) to print the test results beautifully on running `./gradlew test`