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

<<<<<<< HEAD
- Java 21
- JavaFX 21
- FXML e CSS para a interface
- Maven
=======
## Workspace local

A aba **WORKSPACE** transforma a demo em um gerenciador local de pastas para teste:

1. Clique em **WORKSPACE** na barra lateral ou no botão da barra superior.
2. Escolha a pasta que concentrará seus programas.
3. Cada subpasta imediata será exibida como um projeto.

Na leitura, o sistema obtém nome, caminho, quantidade de arquivos, tamanho, data de modificação e a árvore de arquivos de cada projeto. As linguagens são identificadas pelas extensões encontradas, por exemplo: `.java`, `.kt`, `.py`, `.js`, `.ts`, `.tsx`, `.go`, `.rs`, `.c`, `.cpp`, `.cs`, `.php`, `.rb`, `.swift`, `.html` e `.css`. A linguagem predominante aparece no card, enquanto as demais aparecem como etiquetas com suas quantidades.

Esta etapa é estritamente de leitura: não move, renomeia, exclui nem modifica arquivos do computador. O workspace escolhido vale apenas durante a sessão atual.

Antes de escolher um workspace, a tela mostra dados de demonstração. Não há banco de dados ou integração de terminal nesta etapa.
>>>>>>> d968ba8 (aplicação abrindo pastas, e ao iniciar, abrindo a lista de recente)

## Estrutura do projeto

```text
src/main/java/com/managerrepositories/
├── Main.java                         # Ponto de entrada da aplicação
├── controller/ProjectController.java # Controle da interface e do catálogo
└── model/
    ├── Project.java                  # Modelo de projeto
    └── FileNode.java                 # Modelo da árvore de arquivos

src/main/java/com/managerrepositories/service/
└── WorkspaceScanner.java             # Leitura e identificação de linguagens locais

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
