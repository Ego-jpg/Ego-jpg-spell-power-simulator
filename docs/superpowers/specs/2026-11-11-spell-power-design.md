# Spell Power Simulator — design proposto

## Objetivo e restrições

Mod auxiliar exclusivamente client-side para Minecraft Java 1.21.1, Java 21, NeoForge 21.1.250 e Iron's Spells 'n Spellbooks 3.16.3. Destino: Ego-jpg/Ego-jpg-spell-power-simulator, PR para main. JEI será fixado em uma versão compatível após inspeção do JAR; a fonte de Iron's 3.16.3 declara 19.21.0.247.

Consultar pergaminhos no JEI sem exigir posse do item. Nunca modificar atributos, componentes, equipamento ou inventário reais, nem enviar operações de alteração ao servidor. Não executar casts para calcular valores.

## Abordagens

O usuário escolheu suporte explícito por magia: cada adaptador precisa de auditoria da fórmula e do caminho de dano na versão exata. Reutilizar funções puras reais quando acessíveis; reproduzir apenas operações verificadas quando as APIs exigirem uma entidade mutável. Uma entidade simulada foi considerada, mas exige reproduzir o contexto inteiro. Interpretar tooltips não atende à precisão desejada.

## Componentes

1. Integração JEI: manter referência ao runtime somente durante sua disponibilidade; obter o ItemStack sob o cursor pelas APIs públicas da versão fixada. Tratar overlay, receitas e ausência de ingrediente conforme as APIs efetivamente disponíveis, sem reflexão em campos internos.
2. Leitor de pergaminhos: verificar identidade do item, ler SPELL_CONTAINER sem alterações, rejeitar componentes ausentes, vazios ou incoerentes e extrair ID e nível reais de SpellData. A consulta não depende do inventário.
3. Captura do jogador: registrar valores efetivos do atributo geral e da escola, além dos modificadores e configurações sincronizadas usados pelo adaptador. Sem jogador ou configuração necessária disponível, mostrar indisponibilidade. Não inventar valores padrão silenciosamente.
4. Calculadora: registro explícito de adaptadores por ID e versão auditada. Receber uma captura imutável e os dois valores simulados separados; retornar dano atual, dano simulado, diferença absoluta e percentual, ou motivo de indisponibilidade. Zero no denominador produz percentual indisponível. Rejeitar entradas inválidas e resultados não finitos.
5. Interface: atalho configurável abre uma Screen client-side mantendo a tela anterior para retorno. Mostrar magia, nível, escola, atributos atuais, dois campos simulados, resultados e limitações. Usar widgets e traduções do Minecraft. Não pausar multiplayer nem transmitir simulações. Escolha visual inicial do implementador: painel simples com valores atuais e simulados lado a lado.

## Precisão e escopo do resultado

O número representa dano da magia antes de mitigação do alvo e eventos externos, com unidade explícita por impacto ou ocorrência conforme o adaptador. Não representar DPS, dano acumulado, fogo secundário ou dano final no alvo sem auditoria própria. Configurações sincronizadas devem ser lidas pelas APIs reais de Iron's. Preservar ordem de operações e conversões float relevantes.

Firebolt é o primeiro candidato: a fonte exata mostra getSpellPower * 0.5f no dano do projétil. Sua inclusão depende de conferir o JAR e os modificadores relevantes. Não há compromisso antecipado de suporte a outras magias. Classes substituídas por addons, versões diferentes, fórmulas especiais e contexto insuficiente devem resultar em não suportado quando não auditados.

## Implementação incremental e validação

Primeiro, fixar ferramentas e dependências e conferir código e JARs exatos. Em seguida, implementar a leitura JEI de ID e nível, com testes para entrada inválida e preservação dos componentes. Depois implementar captura, primeiro adaptador e interface.

Testes automatizados devem cobrir seleção de suporte, operações da calculadora, limites, percentual com base zero e casos derivados das fórmulas auditadas. Integrações Minecraft/JEI precisam de teste com runtime apropriado; testes unitários puros não provam funcionamento dentro do jogo. Rodar o build Gradle completo e registrar resultados reais.

Usar Gradle Wrapper oficial com verificação da distribuição. GitHub Actions deve usar Java 21, executar testes e build e publicar o JAR como artefato da execução. Documentar fontes auditadas, magias suportadas e testes no jogo pendentes. Criar branch e PR para main, sem declarar conclusão se a compilação não for confirmada.

## Ambiente reutilizável

Usar o checkout existente: tarefas cloud já são isoladas e não precisam de worktree adicional. Registrar instalação e comandos de validação nos campos de configuração após testar os passos. Instruções para iniciar cliente devem considerar disponibilidade gráfica. Nenhum processo deve ser presumido persistente entre tarefas.

## Bloqueios externos conhecidos

A política de rede bloqueou maven.neoforged.net com CONNECT 403. A ferramenta de configuração não conseguiu ler o rascunho nem confirmar um salvamento. A liberação dos repositórios Maven e downloads de Minecraft ainda precisa ser aplicada e validada antes da inspeção dos JARs e do build. Nenhuma instalação, compilação, publicação ou restauração futura foi confirmada.
