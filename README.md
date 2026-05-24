# 🎮 Stella - Game Project

Um jogo 2D desenvolvido em Java com sistema de colisão, medo e inimigos.

---

## 📁 Estrutura do Projeto

O projeto foi reorganizado em **pacotes** (packages) para melhor organização:

```
src/
└── com/stella/
    ├── core/
    │   ├── App.java              → Classe principal que inicia o jogo
    │   └── GamePanel.java        → Painel principal, renderização e loop do jogo
    │
    ├── player/
    │   ├── Player.java           → O jogador, movimento e detecção de inimigos
    │   └── KeyHandler.java       → Detecta entrada do teclado (WASD)
    │
    ├── world/
    │   ├── Tile.java             → Representa um bloco do mapa
    │   └── TileManager.java      → Carrega e desenha o mapa
    │
    ├── entities/
    │   ├── Entity.java           → Classe base para entidades (posição, colisão)
    │   ├── superObject.java      → Classe base para objetos do mundo (inimigos, itens)
    │   └── Enemy.java            → Inimigo do jogo
    │
    ├── physics/
    │   └── CollisionChecker.java → Verifica colisões com o mapa
    │
    └── assets/
        └── AssetSetter.java      → Inicializa objetos do jogo (inimigos, itens)
```

---

## 🔧 Como Compilar e Rodar

### Opção 1: Terminal (Recomendado)

```bash
# Compilar
javac -d bin src/com/stella/core/App.java

# Rodar
java -cp bin com.stella.core.App
```

### Opção 2: VS Code

Se estiver usando VS Code com extensão Java:
- Clique em **Run** (▶️) ao lado da classe `App.java`

---

## 🎯 Como Funciona

### 📍 Movimentação
- **W** = Cima
- **A** = Esquerda
- **S** = Baixo
- **D** = Direita

### 👻 Sistema de Medo
O jogador sente medo quando um inimigo está próximo:
- **< 20px**: Barra de medo 100%
- **20-75px**: Barra de medo 75%
- **75-150px**: Barra de medo 50%
- **150-300px**: Barra de medo 20%

### 💥 Colisões
O sistema de colisão verifica se o jogador pode se mover em uma direção analisando os tiles:
- **Tile 67** = Parede (tem colisão)
- Outros tiles = Passáveis

---

## � Matemática da Câmera e do Mundo
A câmera do jogo não é uma entidade física: ela é só a posição do mundo que aparece no canto superior-esquerdo da tela.

### 1. Mundo vs Tela
- **Mundo**: o espaço inteiro onde o jogador anda, medido em pixels.
- **Tela**: a parte visível do mundo que o jogador vê agora.
- `cameraX` e `cameraY` dizem qual ponto do mundo está no canto superior-esquerdo da tela.

### 2. Converter posição do mundo para a tela
Se um ponto no mundo tem coordenadas `worldX`, `worldY`, então a posição dele na tela é:

```text
screenX = worldX - cameraX
screenY = worldY - cameraY
```

Isso funciona assim:
- Se `cameraX = 0`, o canto esquerdo do mundo aparece no canto esquerdo da tela.
- Se `cameraX = 200`, significa que o mundo já foi deslocado 200px para a esquerda.
- Um objeto que está em `worldX = 300` vai aparecer em `screenX = 100`.

### 3. Centralizar o jogador
Queremos que o jogador fique no meio da tela enquanto for possível.

Se a tela tem `screenWidth` e `screenHeight`, então a câmera deve ir até:

```text
cameraX = player.worldX - (screenWidth / 2)
cameraY = player.worldY - (screenHeight / 2)
```

No nosso projeto usamos uma forma equivalente com `player.screenX` e `player.screenY`, porque já definimos onde o jogador deve ficar na tela.

### 4. Limitar a câmera ao mapa
A câmera não pode sair do mapa. Se ela for além do lado esquerdo ou direito, a imagem começa a sumir.

Calculamos os limites assim:

```text
maxCameraX = worldWidth - screenWidth
maxCameraY = worldHeight - screenHeight
```

Depois fazemos a câmera respeitar esses limites:

```text
cameraX = clamp(cameraX, 0, maxCameraX)
cameraY = clamp(cameraY, 0, maxCameraY)
```

Onde `clamp` significa:

```text
clamp(v, min, max) = Math.max(min, Math.min(max, v))
```

Isso garante que:
- `cameraX` nunca fique menor que 0
- `cameraX` nunca fique maior que a borda direita do mapa
- o mesmo vale para `cameraY`

### 5. Atualizar a posição do jogador na tela
Depois de limitar a câmera, recalculamos onde o jogador deve aparecer:

```text
player.screenX = player.worldX - cameraX
player.screenY = player.worldY - cameraY
```

Isso faz o jogador ficar centralizado enquanto a câmera ainda pode se mover.

### 6. Quando o jogador chega na borda do mapa
Se o jogador estiver perto da borda, a câmera para de se mover, mas o jogador continua andando.

Exemplo:
- O mapa tem 3600px de largura (`worldWidth`)
- A tela tem 1440px de largura (`screenWidth`)
- `maxCameraX = 3600 - 1440 = 2160`

Se o jogador andar até `player.worldX = 2200`, a câmera fica em `2160`.
Nesse caso o jogador aparece deslocado dentro da tela, não mais centralizado.

### 7. O que desenhar na tela
Para não desenhar tudo, desenhamos apenas os tiles e objetos que estão dentro da tela.

Depois de calcular `ScreenX` e `ScreenY`, a verificação é simples:

```text
if (ScreenX + tileSize > 0 && ScreenX < screenWidth &&
    ScreenY + tileSize > 0 && ScreenY < screenHeight) {
    // desenha o tile ou objeto
}
```

Isso significa “desenhe se qualquer parte do tile estiver visível na tela”.

### 8. Resumo intuitivo
- A câmera é a posição do mundo que está na borda da tela.
- Para desenhar, subtraímos a posição da câmera.
- A câmera é limitada para não sair do mapa.
- O jogador anda livremente, e a câmera segue até o limite.

---

## �📝 Comentários no Código

**Cada arquivo tem comentários claros e diretos**:
- ✅ Explicam o propósito de cada classe
- ✅ Descrevem o que cada método faz
- ✅ Esclarecem variáveis importantes
- ✅ Linguagem simples, não técnica

---

## 🔄 Ordem de Inicialização (Importante!)

1. **TileManager** - Carrega o mapa e tiles
2. **CollisionChecker** - Verifica colisões
3. **Player** - O jogador
4. **AssetSetter** - Coloca inimigos no mundo

Esta ordem é crítica para evitar null pointers!

---

## 🛠️ Detalhes Técnicos

- **Linguagem**: Java 17+
- **Resolução**: 1440x864px (20x12 tiles na tela)
- **Mundo**: 50x50 tiles
- **FPS**: 60 (16ms por frame)
- **Câmera**: Centralizada no jogador

---

## 📌 Próximas Melhorias Possíveis

- [ ] Animações do jogador
- [ ] Mais tipos de inimigos
- [ ] Sistema de itens
- [ ] Efeitos de som
- [ ] Menu de pausa
- [ ] Diferentes níveis

---

**Desenvolvido com ❤️ em Java**
