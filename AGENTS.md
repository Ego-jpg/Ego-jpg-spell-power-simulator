# Spell Power Simulator

## Ambiente e compatibilidade
- Windows/PowerShell: JDK Temurin 21.0.13+9; Git 2.56.1; VS Code 1.146.0. Auditoria: 2026-11-12.
- Ferramentas opcionais no perfil: Node.js 26.13.1 LTS, npm 11.20.0, gh 2.103.3. Extensões verificadas: Codex `openai.chatgpt` 26.1106.51428, Java 1.58.0, Gradle 3.19.0.
- Minecraft 1.21.1, NeoForge 21.1.250, ModDevGradle 2.0.148; Java toolchain 21.
- Dependências declaradas em `build.gradle`: Iron's Spellbooks 1.21.1-3.16.3, JEI 19.32.0.359, GeckoLib 4.7.5.1, Curios 9.5.1+1.21.1, Player Animator 2.0.1+1.21.1, Iron's Lib 1.21.1-2.1.0.
- Testes: JUnit BOM 5.11.4, Mockito 5.14.2, NeoForge testframework 21.1.250. Não atualizar versões incidentalmente.
- Gradle Wrapper 8.14.3 com SHA-256 fixado; usar o Wrapper, sem Gradle global. Não atualizar pelo IDE incidentalmente nem desativar a verificação de integridade.

## Estrutura e invariantes
- `src/main/java/dev/egojpg/spellpowersimulator`: `client` (tela/atalho), `jei`, `scroll` (leitura), `calculation` (fórmulas).
- `src/main/resources`: metadados e traduções; `src/test/java`: testes existentes; `docs/validation.md`: limites e validação no jogo.
- Mod exclusivamente cliente: não modificar atributos, inventário, componentes ou dados do servidor. Preservar leitura imutável, foco de campos de texto e fórmulas auditadas.
- Separar APIs cliente de código carregável no servidor. Confirmar APIs de dependências na versão exata antes de integrar.

## Comandos e validação
- Windows: `.\gradlew.bat --console=plain test build`; Linux/CI: `./gradlew --console=plain test build`.
- Teste focado: `.\gradlew.bat test --tests 'nome.completo.Classe'`; cliente gráfico: `.\gradlew.bat runClient`.
- VS Code: tarefas Gradle; `powershell -NoProfile -ExecutionPolicy Bypass -File tools/validate.ps1` guarda log e resumo em `build/validation/`, preservando o código de saída. A opção vale somente para esse processo, sem alterar a política global.
- JARs: `build/libs/`; relatórios JUnit: `build/reports/tests/test/`. Workflow existente testa/compila e armazena artefatos; não duplicar CI.
- Executar uma validação proporcional após alterações; repetir somente após mudança relevante ou diagnóstico. Não declarar sucesso quando bloqueado por download/checksum. Testes automatizados não substituem validação gráfica do atalho e JEI.

## Contexto, ferramentas e segurança
- Começar com `git status --short`, buscas `rg` específicas e trechos relacionados; preservar trabalho não commitado.
- Reutilizar respostas e decisões da sessão; resumir erros e guardar logs extensos em `build/`. Não ler todo o repositório nem criar documentação redundante.
- Consultar Context7 quando houver dúvida sobre APIs/versões; resolver a biblioteca e consultar apenas o conceito necessário. Priorizar código local para dúvidas que ele já responde. Não repetir consultas sem motivo.
- Context7 já funcional; preservar sua configuração. GitHub conectado disponível via plugin; preferir Git/gh para operações locais e CLI, plugin para contexto remoto quando útil. Não instalar MCP redundante.
- Não versionar tokens, senhas, `.env` ou logs com segredos. Usar autenticação oficial e escopo mínimo; nunca imprimir credenciais.
- Não executar operações destrutivas, push, merge, publicação ou releases sem autorização explícita. Não alterar configuração global ou dependências sem necessidade concreta.
