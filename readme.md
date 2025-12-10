# Warhot 🔥😏

## Documento de Apresentação

---

# 📋  Índice

1. Visão Geral do Projeto
2. Conceito e Objetivo do Jogo
3. Tecnologias Utilizadas
4. Arquitetura e Estrutura
5. Mecânicas de Gameplay
6. Sistemas Implementados
7. Geração Procedural
8. Progressão e Balanceamento

---

# 1. Visão Geral do Projeto

## 1.1 O Que é?

**Dungeon Crawler** é um jogo onde o jogador explora um dungeon gerado proceduralmente, enfrentando monstros, coletando armas e progredindo até derrotar o boss final.

## 1.2 Características Principais

* **100% Terminal** - Interface ASCII colorida
* **Geração Procedural** - Cada dungeon é único
* **Sistema de Combate** - Combate por turnos com múltiplas opções
* **Sistema de Loot** - Armas com raridades e estatísticas

---

# 2. Conceito e Objetivo do Jogo

## 2.1 Objetivo Principal

**Derrotar o Boss Goblin King** que se esconde na sala de boss do dungeon.

## 2.2 Regras do Jogo

### 2.2.1 Exploração

* O jogador se move usando **WASD**
* Atravessar portas leva a novas salas
* Salas não descobertas estão marcadas com **[?]**
* Cada sala pode conter monstros e baús

### 2.2.2 Combate

* Combate é iniciado ao colidir com um monstro
* Sistema de **turnos**
* Ações:

  * **[1]** Ataque Básico
  * **[2]** Ataque Poderoso
  * **[3]** Bola de Fogo
  * **[R]** Fugir

### 2.2.3 Morte

* HP = 0 → **GAME OVER**

---

# 3. Tecnologias Utilizadas

## 3.1 Linguagem

* **Java 17+**
* **Jline** (terminal interativo)

## 3.2 Estrutura de Pacotes

```
src/
├── engine/          
├── entidades/       
├── items/           
├── mundo/           
└── Main.java        
```

---

# 4. Arquitetura e Estrutura

## 4.1 Padrões de Design

### 4.1.1 SRP

Ex.: CombatManager, RoomManager etc.

### 4.1.2 Manager Pattern

```
Game
  ├── CombatManager
  ├── RoomManager
  ├── NavigationManager
  ├── InputHandler
  └── CollisionDetector
```

### 4.1.3 Composition over Inheritance

## 4.2 Diagrama de Classes

```
┌─────────────┐
│    Game     │
└──────┬──────┘
       │
       ├─────────────┬─────────────┬──────────────┐
       ▼             ▼             ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│  Player  │  │ Dungeon  │  │ Managers │  │ GameUI   │
└────┬─────┘  └────┬─────┘  └──────────┘  └──────────┘
     │             │
     ▼             ▼
┌─────────┐  ┌──────────┐
│  Stats  │  │  Level   │
│Inventory│  │   Room   │
└─────────┘  └────┬─────┘
                  │
                  ▼
             ┌─────────┐
             │   Map   │
             │Monsters │
             │ Chests  │
             └─────────┘
```

## 4.3 Fluxo de Execução

```
Main.start()
    ↓
Game.start()
    ↓
Inicialização
    ↓
Game Loop
    ↓
Game Over
```

---

# 5. Mecânicas de Gameplay

## 5.1 Movimento

### 5.1.1 Controles

WASD, M, I, E, Q

### 5.1.2 Colisão

* Paredes bloqueiam
* Portas permitem passagem
* Monstros iniciam combate

### 5.1.3 Sprite

```
@
```

## 5.2 Combate

### 5.2.1 Turnos

Player → Monstro

### 5.2.2 Ações

| Ação            | Custo | Efeito        | Tecla |
| --------------- | ----- | ------------- | ----- |
| Ataque Básico   | 0     | normal        | 1     |
| Ataque Poderoso | 5 MP  | 1.5x          | 2     |
| Bola de Fogo    | 8 MP  | 10–15 dmg     | 3     |
| Fugir           | —     | random * luck | R     |

## 5.3 Loot e Inventário

### 5.3.1 Baús

* Armas aleatórias
* Raridades

### 5.3.2 Inventário

* Limite 20 armas
* Equipar 1 arma

---

# 6. Sistemas Implementados

## 6.1 Atributos (Stats)

Strength, Dexterity, Intelligence, Endurance, Luck, Vitality

## 6.2 Sistema de Combate

* `inCombat`
* `currentEnemy`
* `CombatManager`

## 6.3 Sistema de Navegação

* Transição de salas
* Minimapa (M)

## 6.4 Sistema de Renderização

* Cores ANSI
* Viewport

---

# 7. Geração Procedural

## 7.1 Algoritmo do Dungeon (5x5)

Random Walk, START, BOSS, TREASURE etc.

## 7.2 Geração de Mapas de Sala

60x30, salas internas, BSP, corredores.

---

# 8. Progressão e Balanceamento

## 8.1 Dificuldade por Sala

```java
case START -> 0;
case NORMAL -> 1;
case TREASURE -> 2;
case BOSS -> 5;
```

## 8.2 Quantidade de Monstros

| Sala     | Qtde |
| -------- | ---- |
| START    | 0    |
| NORMAL   | 2–4  |
| TREASURE | 3–4  |
| BOSS     | 1    |

## 8.3 Sistema de Loot

Baseado em luck.

---

# Comandos Rápidos

## Code Page

```
chcp 65001
```

## Compilar

```
javac -cp "lib\jline-terminal-3.25.0.jar;lib\jline-reader-3.25.0.jar;lib\jline-terminal-jna-3.25.0.jar;lib\jna-5.14.0.jar;." -d out src\Main.java src\engine\*.java src\entidades\*.java src\mundo\*.java src\items\*.java
```

## Executar

```
java -cp "lib\jline-terminal-3.25.0.jar;lib\jline-reader-3.25.0.jar;lib\jline-terminal-jna-3.25.0.jar;lib\jna-5.14.0.jar;out" Main
```