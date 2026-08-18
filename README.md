# DEVHUB — Demo JavaFX

Esta pasta contém a adaptação do projeto **Gerenciador de Projetos Desktop** para uma aplicação desktop Java, estruturada com Maven e interface JavaFX.

## Migração de linguagem

O projeto de origem foi criado com React, TypeScript, Vite e Tailwind CSS. Na demo, a interface e os comportamentos foram migrados para Java 21 e JavaFX:

| Origem | Demo Java |
| --- | --- |
| React (`App.tsx`) | `ProjectController.java` + `Main.fxml` |
| Estado com `useState` | Lista em memória e ações no controlador JavaFX |
| Componentes React | Nós JavaFX criados pelo FXML e pelo controlador |
| Tailwind/CSS inline | `application.css` com seletores JavaFX |
| Vite/pnpm | Maven (`pom.xml`) |

## Funcionalidades migradas

- Catálogo de projetos de demonstração, com linguagem, caminho, tags, tamanho e quantidade de arquivos.
- Pesquisa por nome ou tag.
- Filtro por linguagem, favoritos e abas Todos/Favoritos/Recentes.
- Cards de projetos e visual alternativo em lista.
- Painel de detalhes com métricas, tags e árvore de arquivos expansível.
- Marcação e remoção de favoritos.
- Inclusão de um novo projeto por caixa de diálogo.
- Exclusão do projeto selecionado.

Os dados ainda são de demonstração e permanecem apenas em memória enquanto a aplicação está aberta. Não há integração com o sistema de arquivos, banco de dados ou terminal nesta etapa.

## Estrutura

```text
src/main/java/com/managerrepositories/
├── Main.java                         # Inicializa a aplicação JavaFX
├── controller/ProjectController.java # Comportamentos e dados da tela
└── model/
    ├── Project.java                  # Modelo de projeto
    └── FileNode.java                 # Modelo da árvore de arquivos

src/main/resources/
├── view/Main.fxml                    # Estrutura da interface
└── css/application.css               # Tema e estilos JavaFX
```

## Executar

Pré-requisitos: JDK 21 e Maven.

```powershell
mvn javafx:run
```

Para validar a compilação:

```powershell
mvn clean test
```

## Dependências

Definidas em `pom.xml`:

- `javafx-controls`
- `javafx-fxml`
- `javafx-maven-plugin`

