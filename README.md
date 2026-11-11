# Spell Power Simulator

Mod auxiliar **client-side** para consultar pergaminhos no JEI e comparar dano com valores simulados de Spell Power geral e da escola. Não exige possuir o pergaminho e não altera atributos, componentes, inventário ou dados do servidor.

## Versões

| Componente | Versão de desenvolvimento |
|---|---|
| Minecraft Java | 1.21.1 |
| Java JDK | 21 |
| NeoForge | 21.1.250 |
| Iron's Spells 'n Spellbooks | 1.21.1-3.16.3 |
| JEI | 19.32.0.359 (SoMake) |
| GeckoLib | 4.7.5.1 |
| Curios | 9.5.1+1.21.1 |
| Player Animator | 2.0.1+1.21.1 |
| Iron's Lib | 1.21.1-2.1.0 |

Instale o JAR deste projeto somente no cliente, junto dessas dependências. Nenhum componente deste mod precisa ser instalado no servidor. Os JARs de terceiros não são incluídos no artefato.

## Uso

1. Abra um inventário com o JEI, os favoritos ou uma página de receitas.
2. Posicione o cursor sobre um pergaminho e pressione **P**. O atalho pode ser alterado em Opções → Controles → Spell Power Simulator, inclusive para um botão do mouse.
3. Edite os dois multiplicadores efetivos separadamente: `1.0` significa 100% do atributo, `1.5` significa 150%, não um bônus adicional de 150%.
4. Compare dano atual, simulado, diferença em unidades de dano e diferença percentual. Com dano atual zero, o percentual é exibido como `—`.
5. Use “Usar poderes atuais” para copiar os atributos efetivos atuais, ou Esc/Concluído para voltar à tela anterior.

O atalho não intercepta digitação em campos de texto ou na busca do JEI. A seleção usa as APIs públicas do JEI e o componente real `SPELL_CONTAINER` de Iron's, preservando ID e nível gravados. Atributos atuais e configuração são relidos enquanto a tela estiver aberta; os campos simulados permanecem independentes.

## Precisão e suporte

**Suporte inicial: `irons_spellbooks:firebolt`, níveis 1–10, classe original da versão 3.16.3.** Todas as outras magias e classes substituídas são indicadas como não suportadas. Não existe uma fórmula genérica aplicada a magias desconhecidas.

O resultado de Firebolt é **dano direto por impacto no nível do pergaminho**, antes de defesas do alvo e eventos do servidor. Não inclui queimadura, resistência, armadura, críticos externos, mudanças de nível feitas por eventos de lançamento ou regras adicionadas por outros mods. Não representa dano final garantido em um alvo nem DPS. Addons que alterem bytecode/eventos ainda precisam de auditoria; a comparação com o cálculo atual detecta algumas mudanças de escalamento, mas não comprova compatibilidade com addons arbitrários.

A calculadora usa atributos efetivos, incluindo modificadores de equipamento/efeitos já aplicados pelo jogo, escola obtida pela API de Iron's e `POWER_MULTIPLIER` da configuração gerida e sincronizada por Iron's. Não altera atributos para executar o cálculo. Entradas inválidas, valores não finitos, contexto ausente ou divergência da fórmula auditada não produzem estimativas apresentadas como exatas.

Veja [auditoria e validação](docs/validation.md) para fontes, detalhes numéricos e testes no jogo ainda pendentes.

## Desenvolvimento

```bash
./gradlew test build
./gradlew runClient
```

Use um JDK 21 completo (`java`, `javac`, `javap`), não apenas JRE. O Wrapper oficial Gradle 8.14.3 verifica o SHA-256 da distribuição. O primeiro build prepara Minecraft/NeoForge e baixa os assets; os seguintes reutilizam o cache. `runClient` requer uma sessão gráfica com OpenGL.

Os testes JUnit rodam com o suporte de testes do ModDevGradle/NeoForge. Os testes de integração carregam os registros e datapacks de Minecraft e Iron's; não equivalem a uma sessão interativa do cliente.

Em ambiente cloud Linux, `bash tools/cloud-setup.sh` prepara o JDK e gera `/workspace/cloud-java-env.sh` com inicialização de Java, proxy e confiança TLS da plataforma. Depois use `source /workspace/cloud-java-env.sh` antes dos comandos Gradle. Não coloque credenciais em arquivos do projeto.

O checkout cloud já é isolado; não é necessário criar outro worktree. O GitHub Actions executa testes e build com Java 21 e disponibiliza o JAR e relatórios como artefatos da execução. O JAR fica em `build/libs/spell-power-simulator-0.1.1.jar`.

## Organização

- `scroll`: leitura imutável dos componentes e seleção de magia/nível.
- `jei`: ciclo de vida do runtime e consulta do ingrediente sob o cursor.
- `calculation`: valores efetivos, fórmulas auditadas e comparação numérica.
- `client`: atalho e interface Minecraft, com traduções inglês/português.
