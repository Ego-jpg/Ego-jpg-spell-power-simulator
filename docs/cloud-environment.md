# Ambiente cloud

O checkout existente já é isolado; não crie outro worktree sem solicitação explícita.

Inicialização testada:

```bash
cd /workspace/Ego-jpg-spell-power-simulator
bash tools/cloud-setup.sh
source /workspace/cloud-java-env.sh
./gradlew --no-daemon test build
```

O helper instala/ativa Temurin JDK 21 verificado por SHA-256 em `/workspace/toolchains`, preserva proxy e confiança TLS e mantém o cache Gradle fora do checkout. O ramo de download corresponde ao mesmo artefato oficial baixado e verificado durante a preparação; a execução repetida reutilizou o JDK instalado. Não são necessários segredos adicionais.

Para iniciar o cliente, disponibilize um desktop gráfico com OpenGL e `DISPLAY` configurado, execute a inicialização acima e depois `./gradlew runClient`. Inspecione `run/logs/latest.log`; processo iniciado não comprova funcionamento. Os passos interativos estão em [validation.md](validation.md). Processos não devem ser presumidos persistentes entre tarefas.

O ambiente atual compilou o mod e executou os testes. A tentativa de iniciar o cliente sem display falhou em `glfwInit`.

A ferramenta de onboarding retornou `INVALID_ARGUMENT` na leitura e no salvamento do rascunho. **A persistência dos campos `install_script` e `start_skill` não foi confirmada**, e nenhuma publicação ou restauração em nova tarefa foi validada. As instruções reproduzíveis foram preservadas neste repositório; o impedimento de persistência precisa ser resolvido no fluxo de configuração do ambiente.
