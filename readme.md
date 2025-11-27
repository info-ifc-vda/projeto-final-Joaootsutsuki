### Compilar:

```bash
cd warhot
javac -cp "lib\jline-terminal-3.25.0.jar;lib\jline-reader-3.25.0.jar;lib\jline-terminal-jna-3.25.0.jar;lib\jna-5.14.0.jar;." -d bin Main.java engine/*.java entidades/*.java mundo/*.java items/*.java
java -cp "lib\jline-terminal-3.25.0.jar;lib\jline-reader-3.25.0.jar;lib\jline-terminal-jna-3.25.0.jar;lib\jna-5.14.0.jar;bin" Main
```