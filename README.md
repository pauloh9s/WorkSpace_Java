# SimplyWork

Aplicação desktop em JavaFX para organizar e consultar projetos de desenvolvimento em uma única interface. O SimplyWork apresenta um catálogo de projetos com informações como linguagem, caminho, tags, quantidade de arquivos, tamanho e data de abertura.

## Recursos

- Visualização de projetos em cards ou lista.
- Pesquisa por nome e tags.
- Filtros por linguagem e favoritos.
- Abas para todos os projetos, favoritos e projetos recentes.
- Painel de detalhes com métricas, tags e árvore de arquivos.
- Marcação de projetos como favoritos.
- Inclusão e exclusão de projetos durante a sessão.

## Estado atual

O catálogo usa dados de exemplo mantidos em memória. As alterações feitas na aplicação duram apenas enquanto ela está aberta; ainda não há leitura do sistema de arquivos, persistência em banco de dados nem integração com terminal.

## Tecnologias

- Java 21
- JavaFX 21
- FXML e CSS para a interface
- Maven

## Estrutura do projeto

```text
src/main/java/com/managerrepositories/
├── Main.java                         # Ponto de entrada da aplicação
├── controller/ProjectController.java # Controle da interface e do catálogo
└── model/
    ├── Project.java                  # Modelo de projeto
    └── FileNode.java                 # Modelo da árvore de arquivos

src/main/resources/
├── view/Main.fxml                    # Layout da interface
└── css/application.css               # Estilos JavaFX
```

## Como executar

Pré-requisitos: JDK 21 e Maven.

```powershell
mvn javafx:run
```

## Validação

```powershell
mvn clean test
```

## Dependências principais

- `javafx-controls`
- `javafx-fxml`
- `javafx-maven-plugin`
