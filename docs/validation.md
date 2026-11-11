# Auditoria e validação

## Dependências examinadas antes da implementação

- Iron's: tag [v1.21.1-3.16.3](https://github.com/iron431/irons-spells-n-spellbooks/tree/v1.21.1-3.16.3), commit `532c98dfa5b219f3927aca9b23d9042c00e767bb`.
- JAR oficial de Iron's: `irons_spellbooks-1.21.1-3.16.3.jar`, versão Modrinth `slKLosTb`. SHA-512 conferido com os metadados oficiais: `fd782f98c6c59b193c4832f33775291d2a7e639e1e23dd47510bfb494d99d182bec1253566b9394f96c733a0d0108be34bd729614b82dc29e55e096fdeb96f5b`.
- JAR JEI `jei-1.21.1-neoforge-19.21.0.247.jar` e fontes da API dessa versão, obtidos do Maven oficial de JEI.
- NeoForge `21.1.250`: POM, JAR universal e fontes/classes do ambiente de desenvolvimento.
- APIs/bytecode examinados com `javap`: `IJeiRuntime`, overlays de ingredientes/favoritos, `IRecipesGui`, `ISpellContainer`, `SpellData`, `SchoolType`, `SpellConfigManager`, `AbstractSpell` e `FireboltSpell`.
- Wrapper Gradle 8.14.3: scripts e JAR oficiais; SHA-256 do JAR e da distribuição conferidos com o serviço oficial Gradle.

## Fórmula habilitada

Somente `irons_spellbooks:firebolt`, classe exata `FireboltSpell`, níveis 1–10, versão exata 3.16.3. O leitor preserva o nível positivo armazenado no componente, mesmo que exceda o limite de suporte da calculadora. Níveis adicionais não são silenciosamente limitados.

`FireboltSpell` define base 12 e incremento 1 por nível e passa `getSpellPower(level, entity) * 0.5f` ao projétil. `AbstractSpell` multiplica a base de nível pelo atributo geral efetivo, potência efetiva da escola configurada e `POWER_MULTIPLIER` sincronizado. O adaptador preserva o cast inicial de potência geral para `float`, a promoção para `double`, o cast da configuração para `float`, a ordem das multiplicações e o arredondamento antes de multiplicar por `0.5f`.

O dano atual é obtido do método real de Iron's. O adaptador também compara esse valor com a fórmula auditada; uma divergência impede a simulação. Esse teste detecta mudanças em parte do escalamento, mas não certifica addons nem alterações que só afetem outras entradas.

O nível é o do pergaminho. `SpellOnCastEvent` pode alterar o nível no servidor e `SpellDamageEvent` pode alterar o dano. A simulação não dispara esses eventos, não lança magias e não promete reproduzir modificações externas. O resultado é por impacto direto, antes de mitigação do alvo, sem queimadura ou DPS.

## Verificação automatizada executada em 2026-11-11

Comando: `./gradlew test build`, Java Temurin 21.0.13, Gradle 8.14.3.

Resultado: **BUILD SUCCESSFUL; 17 testes executados, 0 falhas, 0 erros, 0 ignorados**.

| Grupo | Testes | Evidência |
|---|---:|---|
| ScrollReaderTest | 4 | Componentes reais, ID/nível, item incorreto, ausente/vazio, nível inválido, preservação do ItemStack |
| JeiSelectionTest | 2 | Consulta das APIs externas de lista/favoritos/receitas, cursor vazio, descarte do runtime indisponível |
| CalculationTest | 5 | Fixtures independentes de nível/potências/configuração, diferenças e zero/overflow/entradas inválidas |
| PowerInputParserTest | 2 | Edição parcial, valores inválidos e números finitos não negativos |
| SimulatorNavigationTest | 1 | Camadas reais do NeoForge preservam resultado de crafting, campos editados após resize e retorno à tela anterior |
| SpellCalculatorTest | 3 | Recusa de magia/classe/nível não auditados, comparação com Iron's, atributos preservados, configuração sincronizada e modificadores efetivos em todos os níveis 1–10 |

O teste de navegação falhou ao reproduzir a limpeza do resultado de crafting e passou após substituir `setScreen` por `pushGuiLayer`/`popGuiLayer`. A tela e seus campos são exercitados sem renderização nativa, com a fronteira de janela/narração substituída; isso não valida aparência nem interação com JEI real.

Os testes de integração carregam registros e datapacks por NeoForge. Uma entidade isolada usa o mapa real de atributos; apenas a fronteira do mundo é substituída por um mock, pois o servidor efêmero do framework não disponibiliza um mundo jogável. As APIs externas do JEI são substituídas por doubles de teste. A seleção em uma interface JEI real ainda precisa de verificação interativa.

O teste de preservação de atributos detectou que `getAttribute` criava uma instância persistível ao consultar presença. A produção usa `hasAttribute` e leitura de valor, e o estado serializado permanece idêntico antes/depois do cálculo.

Artefato gerado: `build/libs/spell-power-simulator-0.1.0.jar`.

## Cliente e testes no Minecraft pendentes

`./gradlew runClient` foi executado no ambiente cloud e falhou na inicialização GLFW: não havia display gráfico ativo. Essa execução **não valida** carregamento interativo do mod, interface ou gameplay. Não há magias validadas em uma sessão jogável nesta entrega.

Checklist para um cliente gráfico com as versões fixadas:

- [ ] Abrir JEI, receitas e favoritos; consultar pergaminhos sem possuí-los e conferir ID/nível.
- [ ] Testar atalho padrão, reconfiguração para teclado/mouse e digitação na busca.
- [ ] Comparar Firebolt níveis 1–10 com os valores de Iron's e separar impacto de queimadura/mitigação.
- [ ] Alterar equipamentos/efeitos e configuração do servidor; confirmar atributos e potência sincronizados exibidos.
- [ ] Validar campos separados, zero, limites, reset, resize/escala GUI, traduções e retorno por Esc.
- [ ] Consultar outras magias e confirmar “não suportada”, sem números estimados.
- [ ] Entrar/sair/trocar de mundo e confirmar que o runtime JEI anterior não permanece utilizável.

Todas as outras magias permanecem sem adaptador. Novos adaptadores exigem auditoria do caminho completo de dano e testes específicos; não basta assumir proporcionalidade com Spell Power.
