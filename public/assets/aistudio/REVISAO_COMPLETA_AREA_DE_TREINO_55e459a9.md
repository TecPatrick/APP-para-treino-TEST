# Revisão completa da área de treino

**Projeto:** APP-para-treino-TEST  
**Data:** 26/10/2026  
**Versão analisada:** `15868e3` — main após a melhoria do Foco Muscular.  
**Entrega:** diagnóstico e plano de melhorias; nenhum código do aplicativo foi alterado nesta revisão.

## O que esta área faz

O app permite montar fichas, executar exercícios e marcar séries, controlar descanso, registrar treinos e consultar evolução. Há também corrida com GPS, indicadores de desempenho e medidas corporais em telas relacionadas.

A análise considera uma pessoa usando o celular durante o treino, com várias fichas, meses de histórico e conexão eventualmente indisponível. Não pressupõe milhares de usuários acessando um servidor ao mesmo tempo.

**Conclusão principal:** a sensação de bagunça vem de ações duplicadas, muitas informações antes dos exercícios e funções espalhadas entre telas. Mas há problemas de registro que devem ser corrigidos antes de embelezar os gráficos: progresso fabricado sem histórico, marcação de séries que sempre acrescenta registros e perda do detalhe dos exercícios ao finalizar.

## Método e limites

- **Ponytail Audit:** rastreamento dos fluxos, casos concretos, correção mínima e prioridades.
- **Graphify:** extração estrutural local de 60 arquivos Kotlin: 1.389 nós e 4.440 relações. Consulta dos caminhos de fichas, execução, registro, evolução e GPS; as conclusões foram confirmadas no código-fonte. Não houve extração semântica por API nem custo de tokens de API nessa etapa estrutural.
- Leitura das telas e dos trechos relacionados de navegação, ViewModel, modelos, repositório, DAO, configuração de build e testes. A análise foi direcionada aos fluxos de treino, não uma leitura integral de cada arquivo do aplicativo.
- Não abri essas telas em um emulador ou celular nesta revisão. Achados funcionais abaixo são demonstráveis pelo código; propostas de layout precisam ser avaliadas no aparelho.
- Os testes não foram executados novamente: o ambiente local não tem Java. O CI da alteração anterior passou, mas isso não significa que cobre os problemas identificados aqui.

## Mapa das telas e cobertura

| Área | O que foi revisado | Direção recomendada |
|---|---|---|
| Fichas de treino | Cabeçalho, ações, lista, estado vazio, editar/compartilhar/excluir | Fichas primeiro; eliminar acessos duplicados |
| Detalhe da ficha | Iniciar, resumo muscular, exercícios, adicionar exercício | Resumo compacto antes do início; foco na execução depois |
| Treino em andamento | Séries, carga, descanso, retomada, finalizar | Registro preciso e controles persistentes |
| Treino livre | Entrada, rotina temporária, adição de exercícios | Fluxo coerente com a sessão, sem substituir treino aberto |
| Editor de ficha | Dados gerais, campos numéricos, biblioteca e manual | Validação visível e edição confortável |
| Biblioteca | Busca, filtros musculares, seleção múltipla e GIFs | Selecionar primeiro, configurar depois |
| Substituição | Busca de alternativas e gravação da ficha | Preservar séries concluídas e identidade dos exercícios |
| Guia do exercício | GIF, postura, abertura de guia externo | Informação contextual sem ocupar o cartão principal |
| Finalização | Resumo, esforço, notas e gravação | Feedback de salvamento e prevenção de duplicação |
| Histórico | Recorte de data, lista, exclusão | Período explícito e exclusão reversível ou confirmada |
| Evolução de carga | Seleção, gráficos, PR, 1RM, registro manual | Somente dados reais; estimativas identificadas |
| Desempenho / Medidas & Hábitos | Entradas, indicadores, histórico agregado, fotos e medidas | Separar força de evolução corporal sem duplicar telas |
| Foco Muscular do perfil | Fonte dos volumes e relação com fichas | Preservar histórico; identificar estimativa por grupo |
| Corrida GPS | Entrada, permissões, opções, voz, controles e finalização | Controles de sessão visíveis; opções recolhidas |
| Cardio manual | Formulário e busca de chamada na navegação | Decidir se entra no fluxo; atualmente sem chamada localizada |

## Prioridades

- **P1 — corrigir primeiro:** resultado errado, risco de perda de registro ou alteração inesperada da sessão.
- **P2 — organizar e tornar confiável:** navegação, consistência, validação e clareza das métricas.
- **P3 — acabamento e manutenção:** simplificação que pode vir depois dos fluxos principais.

Esforço é relativo: **pequeno** = ajuste localizado; **médio** = várias telas ou estados relacionados; **maior** = armazenamento, migração e sincronização. Não são estimativas de prazo.

## Corrigir primeiro

### 1. [P1] Evolução mostra progresso inventado quando não há histórico

**Esforço:** pequeno. **Evidência:** [ExerciseProgressionScreen.kt:273](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L273), [ExerciseProgressionScreen.kt:534](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L534).

- **O que é:** A tela monta gráficos de carga, recorde e estimativa de força.
- **Problema:** Sem registros relevantes, cria seis semanas artificiais. Uma carga-base de 60 kg gera 49,2 → 51,6 → 54 → 56,4 → 58,2 → 60 kg, com a nota “Dados semanais sincronizados”. O cartão ainda fala em pico registrado na nuvem.
- **Ajuste mínimo:** Remover a série simulada e as cargas de referência como fonte de desempenho. Mostrar “Ainda não há registros deste exercício” e um acesso para registrar o primeiro resultado. Não apresentar PR ou ganho percentual sem dados reais.
- **Se ficar como está:** O usuário acredita que evoluiu e atingiu recordes que nunca registrou.
- **Critério de aceite:** Conta vazia: nenhum ponto, PR ou percentual inventado. Primeiro registro: um único ponto real.

### 2. [P1] O treino finalizado perde o detalhe necessário para a evolução

**Esforço:** maior. **Evidência:** [VirtusViewModel.kt:2273](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2273), [WorkoutAndProgressModels.kt:94](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/data/model/WorkoutAndProgressModels.kt#L94), [ExerciseProgressionScreen.kt:202](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L202).

- **O que é:** Durante a sessão, as séries ficam associadas ao identificador de cada exercício.
- **Problema:** Ao finalizar, são salvos apenas totais do treino. A evolução tenta identificar exercícios pelo texto do nome da rotina ou das notas. Um treino chamado “Treino A”, com supino e remada e notas vazias, não permite reconstruir a carga real de cada exercício.
- **Ajuste mínimo:** Persistir exercício, ordem da série, repetições e carga executadas, vinculados ao treino concluído. Usar esses registros nos gráficos. Preservar os totais históricos antigos, sem inventar séries para preencher a migração.
- **Se ficar como está:** O treino pode estar no histórico e ainda assim não alimentar corretamente a evolução por exercício.
- **Critério de aceite:** Finalizar dois exercícios com cargas diferentes; reabrir e consultar cada um com seus próprios valores.

### 3. [P1] Registrar uma carga cria um treino fictício de 45 minutos

**Esforço:** médio. **Evidência:** [VirtusViewModel.kt:1823](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L1823), [ExerciseProgressionScreen.kt:1251](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L1251).

- **O que é:** O botão de registrar carga permite anotar um resultado manualmente.
- **Problema:** recordExerciseProgression grava um WorkoutLog com duração fixa de 45 minutos e esforço 8. Registrar somente uma carga altera também o histórico e a contagem de treinos, sem o usuário ter informado esse treino.
- **Ajuste mínimo:** Salvar o resultado como registro de exercício. Se a intenção for registrar um treino completo, pedir duração e dados reais em um fluxo explícito. Não preencher duração ou esforço como se tivessem sido medidos.
- **Se ficar como está:** Os indicadores de frequência, duração e volume ficam contaminados por anotações de carga.
- **Critério de aceite:** Anotar uma carga não acrescenta automaticamente 45 minutos nem uma sessão ao histórico.

### 4. [P1] Marcar uma série nunca desmarca: acrescenta outra

**Esforço:** pequeno a médio. **Evidência:** [VirtusViewModel.kt:2240](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2240), [VirtusViewModel.kt:2167](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2167), [WorkoutCards.kt:592](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutCards.kt#L592).

- **O que é:** Os cartões oferecem botões de marcação para cada série.
- **Problema:** toggleSetLog ignora setIndex e sempre adiciona um registro. Tocar duas vezes na mesma série aumenta a contagem duas vezes; tocar na série 3 primeiro pode deixar a série 1 visualmente marcada, porque a tela usa somente a quantidade concluída.
- **Ajuste mínimo:** Identificar cada série pelo exercício e pelo índice. Marcar e desmarcar a mesma série de forma consistente, recalculando contagem e volume. Permitir confirmar as repetições realmente executadas, em vez de usar sempre repsMin.
- **Se ficar como está:** Um toque repetido infla o volume e o progresso deixa de corresponder ao que aparece na tela.
- **Critério de aceite:** Marcar série 3, desmarcar e tocar duas vezes: nenhum registro duplicado e nenhum outro índice alterado.

### 5. [P1] O ajuste de descanso é sobrescrito pelo contador

**Esforço:** pequeno. **Evidência:** [VirtusViewModel.kt:2183](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2183), [VirtusViewModel.kt:2252](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2252).

- **O que é:** O treino permite aumentar ou diminuir o descanso.
- **Problema:** O temporizador mantém remaining em uma variável local; adjustRestTimer muda apenas o estado exibido. Exemplo: 90 segundos + 15 mostra 105, mas o próximo ciclo pode escrever 89.
- **Ajuste mínimo:** Manter uma única fonte de tempo, preferencialmente o instante de término calculado por relógio monotônico. Ajustar esse término e derivar a exibição. Encerrar e vibrar apenas uma vez.
- **Se ficar como está:** Os botões aparentam funcionar, mas o tempo não respeita a escolha do usuário.
- **Critério de aceite:** Adicionar 15 segundos, aguardar dois segundos e verificar aproximadamente 103; repetir para diminuir e pular.

### 6. [P1] Uma nova sessão pode apagar a atual; o progresso só vive em memória

**Esforço:** médio. **Evidência:** [VirtusViewModel.kt:750](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L750), [VirtusViewModel.kt:2144](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2144), [WorkoutScreen.kt:329](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L329).

- **O que é:** A pessoa pode iniciar uma ficha ou um treino livre enquanto navega.
- **Problema:** startActiveWorkout substitui o estado sem verificar se já existe treino aberto. Além disso, o registro das séries está em MutableStateFlow, sem recuperação persistente localizada: encerramento do processo pode perder a sessão. Isso não significa que toda troca de aba a apague.
- **Ajuste mínimo:** Se houver sessão, oferecer continuar ou descartar explicitamente antes de iniciar outra. Salvar um rascunho local com séries e horários e recuperá-lo após reinício do processo.
- **Se ficar como está:** Uma sequência real de treino pode desaparecer por iniciar outra ficha ou por encerramento do aplicativo.
- **Critério de aceite:** Iniciar B durante A não apaga A sem decisão explícita; recuperar a sessão após recriação do processo.

### 7. [P1] Editar ou substituir recria todos os IDs dos exercícios

**Esforço:** médio. **Evidência:** [WorkoutScreen.kt:556](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L556), [VirtusViewModel.kt:2085](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2085), [VirtusViewModel.kt:2116](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2116), [WorkoutCards.kt:399](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutCards.kt#L399).

- **O que é:** Adicionar ou substituir um exercício durante o treino chama o salvamento completo da ficha.
- **Problema:** O salvamento apaga os exercícios da rotina e os reinsere com id = 0. As séries da sessão continuam vinculadas aos IDs anteriores; após a atualização, podem sumir das marcações visuais. A sequência de apagar e reinserir também não está envolvida numa transação única nesse fluxo.
- **Ajuste mínimo:** Atualizar exercícios existentes preservando seus IDs; criar IDs apenas para itens novos. Aplicar a alteração local de forma atômica e tratar a sincronização depois. Definir explicitamente o que acontece às séries já feitas ao substituir o exercício atual.
- **Se ficar como está:** Uma pequena edição pode desassociar o progresso da tela; uma falha entre exclusão e inserção pode deixar a ficha incompleta.
- **Critério de aceite:** Concluir séries, adicionar outro exercício e substituir um item: séries anteriores preservadas; falha de gravação não deixa meia ficha.

### 8. [P1] Campos de carga não preservam decimais e entradas inválidas viram valores silenciosos

**Esforço:** médio. **Evidência:** [WorkoutRoutineEditorDialog.kt:423](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutRoutineEditorDialog.kt#L423), [WorkoutRoutineEditorDialog.kt:469](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutRoutineEditorDialog.kt#L469), [WorkoutDialogs.kt:642](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutDialogs.kt#L642), [ExerciseProgressionScreen.kt:1253](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L1253).

- **O que é:** Carga, séries e repetições são editadas no formulário, na inclusão manual e na evolução.
- **Problema:** O editor exibe a carga com toInt(), eliminando a parte decimal; ao digitar “12,5”, toFloatOrNull falha. Outros fluxos substituem entrada inválida por valores padrão ou pela carga anterior, sem explicar. O editor principal tem validação, mas os diálogos não seguem o mesmo contrato.
- **Ajuste mínimo:** Manter o texto durante a digitação; aceitar vírgula e ponto; converter ao confirmar e mostrar erro junto ao campo. Validar números finitos, carga não negativa, séries/repetições positivas e limites coerentes. Formatar carga sem truncamento.
- **Se ficar como está:** O usuário pode achar que salvou uma carga diferente da efetivamente registrada.
- **Critério de aceite:** 12,5 e 12.5 salvam 12,5; apagar o campo não insere um valor por conta própria; inválidos não são gravados.

### 9. [P1] Excluir um treino no histórico é imediato

**Esforço:** pequeno. **Evidência:** [WorkoutDialogs.kt:365](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutDialogs.kt#L365), [VirtusViewModel.kt:2304](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2304).

- **O que é:** Cada registro tem um ícone de lixeira.
- **Problema:** O toque chama a exclusão diretamente. Ao contrário da exclusão de ficha, não há confirmação nesse diálogo nem ação de desfazer.
- **Ajuste mínimo:** Adicionar confirmação com nome e data, ou desfazer com restauração segura. Manter o contexto da lista após a ação.
- **Se ficar como está:** Um toque acidental remove um registro de treino.
- **Critério de aceite:** Cancelar a exclusão preserva o registro; confirmar remove exatamente o item escolhido.

### 10. [P2] O histórico geral recebe somente os treinos de um dia

**Esforço:** pequeno. **Evidência:** [WorkoutScreen.kt:141](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L141), [WorkoutScreen.kt:511](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L511), [WorkoutDialogs.kt:335](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutDialogs.kt#L335).

- **O que é:** O botão “Histórico” abre a lista de treinos.
- **Problema:** WorkoutScreen filtra os registros pela data selecionada antes de enviá-los ao diálogo. Se hoje estiver vazio, a mensagem diz que nenhum treino foi registrado ainda, mesmo havendo treinos anteriores.
- **Ajuste mínimo:** Abrir histórico completo com período visível, ou nomear explicitamente “Treinos de 26/10”. Distinguir “Nenhum treino neste período” de “Você ainda não registrou treinos”. Reaproveitar os filtros já existentes em Desempenho quando aplicável.
- **Se ficar como está:** A pessoa pode interpretar um filtro oculto como perda de dados.
- **Critério de aceite:** Treino de ontem continua acessível ao abrir o histórico hoje, ou o filtro atual fica evidente.

### 11. [P2] Semanas podem aparecer fora de ordem e o primeiro ganho usa uma base artificial

**Esforço:** pequeno. **Evidência:** [ExerciseProgressionScreen.kt:211](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L211), [ExerciseProgressionScreen.kt:224](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L224), [ExerciseProgressionScreen.kt:248](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionScreen.kt#L248).

- **O que é:** A evolução agrupa registros por semana e calcula diferenças.
- **Problema:** As chaves são textos como 2026-W2 e 2026-W10, ordenados alfabeticamente: W10 vem antes de W2. O primeiro comparativo começa em 85% da carga atual, mesmo sem semana anterior real; o ano civil também merece tratamento na virada de semana/ano.
- **Ajuste mínimo:** Agrupar por data de início da semana e ordenar cronologicamente. Calcular diferença apenas quando existir registro anterior comparável. Separar carga medida de estimativa e não extrair um “recorde” de qualquer primeiro número kg nas notas.
- **Se ficar como está:** O gráfico e os ganhos semanais podem contar uma evolução incorreta mesmo com dados reais.
- **Critério de aceite:** Testar semanas 2 e 10, dezembro/janeiro e um único registro sem comparativo anterior.

### 12. [P2] O Foco Muscular muda o passado quando uma ficha é editada

**Esforço:** médio. **Evidência:** [VirtusViewModel.kt:2412](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2412).

- **O que é:** O mapa do perfil distribui o volume histórico pelos grupos musculares.
- **Problema:** O cálculo busca a ficha atual pelo routineId e divide o volume igualmente entre seus músculos. Alterar os músculos da ficha muda a atribuição de treinos antigos; excluir a ficha pode fazer esses treinos deixarem de entrar no mapa.
- **Ajuste mínimo:** Guardar os grupos ou exercícios executados junto ao registro da sessão. Para dados antigos sem detalhe, apresentar o resultado como estimativa e não reescrever silenciosamente seu significado.
- **Se ficar como está:** Um histórico inalterado pode produzir um mapa diferente apenas porque a ficha foi reorganizada.
- **Critério de aceite:** Editar ou excluir uma ficha não muda a distribuição já registrada dos treinos concluídos.


## Organizar a experiência e reduzir manutenção

### 13. [P2] Fichas ficam atrás de ações duplicadas

**Esforço:** pequeno. **Evidência:** [WorkoutScreen.kt:219](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L219), [WorkoutScreen.kt:305](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L305), [WorkoutCards.kt:633](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutCards.kt#L633).

- **O que é:** É a porta de entrada para escolher e iniciar o treino.
- **Problema:** “Nova Ficha” e “Histórico” aparecem no cabeçalho e em botões empilhados. Cada ficha ainda apresenta três ícones e dois botões, embora tocar no cartão já abra os exercícios.
- **Ajuste mínimo:** Manter um único “+ Nova ficha”, acessos compactos a Histórico/Evolução e treino livre secundário. Em cada ficha: título, grupos, quantidade de exercícios, menu ⋮ e um botão Iniciar. Usar “Continuar treino” para a sessão ativa.
- **Se ficar como está:** A primeira tela continua parecendo uma lista de ferramentas, com o treino escondido abaixo.
- **Critério de aceite:** Na tela pequena, a primeira ficha aparece sem passar por quatro botões grandes; nenhuma função existente é perdida.

### 14. [P2] O resumo muscular ocupa espaço de execução e mistura idiomas

**Esforço:** pequeno a médio. **Evidência:** [WorkoutCards.kt:345](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutCards.kt#L345), [RoutineMuscleSummaryCard.kt:123](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/components/RoutineMuscleSummaryCard.kt#L123), [RoutineMuscleSummaryCard.kt:189](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/components/RoutineMuscleSummaryCard.kt#L189), [WorkoutCards.kt:541](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutCards.kt#L541).

- **O que é:** O detalhe da ficha mostra estatísticas, mapa corporal e cartões de exercícios.
- **Problema:** O resumo completo, com desenho de 240 dp e títulos como “Routine Summary”, aparece antes dos exercícios mesmo durante a sessão. Os cartões somam ações de troca, GIF, quatro ajustes de carga e séries.
- **Ajuste mínimo:** Antes do início: resumo compacto e mapa expansível. Durante a sessão: priorizar exercícios, séries e descanso; recolher o resumo. Traduzir os textos. Usar campo de carga editável e uma ação contextual “Como fazer”, deixando “Trocar” no menu.
- **Se ficar como está:** A pessoa precisa rolar por conteúdo de consulta para acessar as ações que usa a cada série.
- **Critério de aceite:** Em treino aberto, chegar rapidamente à próxima série; mapa e guia continuam acessíveis sem dominar a tela.

### 15. [P2] Editor e biblioteca pedem decisões demais de uma vez

**Esforço:** médio. **Evidência:** [WorkoutRoutineEditorDialog.kt:213](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutRoutineEditorDialog.kt#L213), [WorkoutRoutineEditorDialog.kt:423](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutRoutineEditorDialog.kt#L423), [WorkoutRoutineEditorDialog.kt:552](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutRoutineEditorDialog.kt#L552), [WorkoutDialogs.kt:383](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutDialogs.kt#L383).

- **O que é:** A ficha combina dados gerais, exercícios da biblioteca e criação manual.
- **Problema:** Quatro campos numéricos dividem uma única linha; fechar o editor chama onDismiss sem proteção de rascunho. Há experiências distintas para escolher vários exercícios e adicionar um único. O botão Salvar pode ficar desabilitado sem apontar claramente o campo pendente.
- **Ajuste mínimo:** Separar visualmente “Dados da ficha” e “Exercícios”, sem criar um assistente longo. Mostrar nome e resumo por exercício e expandir os campos ao editar. Explicar erros, proteger alterações não salvas e padronizar busca/filtros/seleção. Preservar seleção ao buscar outro músculo e mostrar contador de selecionados.
- **Se ficar como está:** Montar uma ficha continua trabalhoso e alterações podem ser descartadas sem intenção.
- **Critério de aceite:** Editar com teclado aberto e fonte ampliada; cancelar saída preserva rascunho; busca e filtros não perdem seleção.

### 16. [P2] A finalização precisa separar salvar localmente de sincronizar

**Esforço:** médio. **Evidência:** [WorkoutDialogs.kt:217](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutDialogs.kt#L217), [WorkoutScreen.kt:493](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L493), [VirtusViewModel.kt:2273](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2273), [VirtusViewModel.kt:2088](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/VirtusViewModel.kt#L2088).

- **O que é:** O encerramento coleta esforço e notas e grava o treino.
- **Problema:** A janela fecha logo após disparar uma gravação assíncrona; não há estado de salvando no fluxo mostrado. O início da finalização ainda deixa isRunning verdadeiro até gravar, permitindo chamadas repetidas. No salvamento de ficha, uma mensagem de sucesso ao final pode substituir a mensagem de erro de sincronização.
- **Ajuste mínimo:** Adicionar estado de gravação que bloqueie confirmação repetida, erro recuperável e sucesso após confirmação local. Exibir sincronização pendente separadamente. Resumir duração, séries e volume antes de confirmar; manter esforço e notas fáceis de entender.
- **Se ficar como está:** O usuário não distingue registro salvo, gravação em andamento e erro na nuvem, e pode repetir a ação.
- **Critério de aceite:** Toque repetido gera um registro; erro local mantém a sessão recuperável; sem internet informa “Salvo neste aparelho”.

### 17. [P2] Corrida concentra configuração e ação na mesma coluna longa

**Esforço:** médio. **Evidência:** [GpsTrackerScreen.kt:592](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/gps/GpsTrackerScreen.kt#L592), [GpsTrackerScreen.kt:805](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/gps/GpsTrackerScreen.kt#L805), [GpsTrackerScreen.kt:1046](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/gps/GpsTrackerScreen.kt#L1046), [GpsTrackerScreen.kt:1210](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/gps/GpsTrackerScreen.kt#L1210).

- **O que é:** A tela reúne mapa, métricas, voz, meta de ritmo, histórico e sessão GPS.
- **Problema:** Os controles de iniciar/pausar/finalizar vêm depois de blocos de opções. Abrir a janela de finalização não pausa a sessão nesse callback; o tempo pode continuar mudando enquanto a pessoa escreve o nome e as notas.
- **Ajuste mínimo:** Colocar controles da sessão em área persistente e opções de mapa/voz/ritmo recolhidas. Ao finalizar, definir claramente se pausa ou congela o resumo antes da gravação; Cancelar deve retomar de forma previsível. Preservar os tratamentos de permissão e o serviço de corrida existentes.
- **Se ficar como está:** A ação essencial pode ficar distante no scroll e o resumo de término pode variar durante o preenchimento.
- **Critério de aceite:** Pausar acessível com opções abertas ou fechadas; resultado final não incorpora inadvertidamente o tempo de digitação.

### 18. [P3] Navegação e telas antigas precisam de uma decisão única

**Esforço:** pequeno a médio. **Evidência:** [MainActivity.kt:281](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/MainActivity.kt#L281), [MainActivity.kt:303](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/MainActivity.kt#L303), [WorkoutScreen.kt:162](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/WorkoutScreen.kt#L162), [ExerciseProgressionModal.kt:37](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/ExerciseProgressionModal.kt#L37), [CardioLogScreen.kt:15](https://github.com/TecPatrick/APP-para-treino-TEST/blob/15868e3f4f1f14f278edbd28b19994041f17c640/app/src/main/java/com/example/ui/screens/workout/CardioLogScreen.kt#L15).

- **O que é:** Há entradas em Treinos e Desempenho para evolução, uma versão modal antiga e um formulário de cardio manual.
- **Problema:** A volta da evolução aponta sempre para Treinos mesmo quando a entrada veio de Desempenho. O estado do modal antigo nunca é ativado nas referências encontradas. CardioLogScreen não teve chamada localizada na árvore pesquisada; sua existência não significa que seja uma aba disponível ao usuário.
- **Ajuste mínimo:** Preservar a origem na navegação de volta, usar uma única tela de evolução e remover o modal sem acesso após conferir referências finais. Decidir conscientemente se cardio manual será conectado ao fluxo; antes disso, validar duração/distância/calorias, que hoje aceitam conversões silenciosas.
- **Se ficar como está:** A navegação permanece imprevisível e versões antigas geram manutenção sem benefício ao usuário.
- **Critério de aceite:** Entrar pela área de Desempenho e voltar retorna à origem; nenhum acesso aponta para a versão antiga.

## Proposta de organização das telas

Não é necessário criar mais destinos na barra principal do aplicativo. A primeira etapa pode reutilizar as telas existentes e apenas mudar a ordem, os rótulos e os acessos.

### Entrada de Treinos

1. Cabeçalho “Treinos” com “+ Nova ficha”.
2. Cartão “Continuar treino”, somente quando houver sessão aberta.
3. Acessos compactos “Histórico” e “Evolução”.
4. Seção “Minhas fichas”: letra, nome, grupos e quantidade de exercícios.
5. Ação secundária “Treino livre”.

Cada ficha abre ao toque. O único botão principal do cartão é “Iniciar treino”; editar, compartilhar e excluir ficam em ⋮. No estado vazio, mostrar uma ação principal de criar ficha e uma alternativa de usar modelo, sem repetir botões de criação.

### Dentro do treino

| Antes de começar | Durante a sessão | Ao finalizar |
|---|---|---|
| Nome e resumo curto | Nome, tempo e progresso | Duração, séries e volume reais |
| Lista de exercícios | Lista com série, carga, repetições e confirmação | Esforço e notas |
| Mapa muscular expansível | Descanso acessível sem voltar ao topo | Estado “Salvando…” |
| Botão “Iniciar treino” | “Finalizar” secundário, protegido de toque acidental | Confirmação local e situação de sincronização |

Não colocar GIFs grandes e o mapa completo antes das séries durante a execução. Manter o guia em “Como fazer”, com alternativa textual quando a imagem não carregar.

### Histórico

- Período visível, lista por data e totais do período.
- Abrir um treino para consultar exercícios e séries quando o armazenamento detalhado estiver implementado.
- Filtros curtos e exclusão com confirmação ou desfazer.
- Diferenciar lista vazia, filtro sem resultados, carregamento e erro.

### Evolução

- Seletor de exercício, período e uma métrica principal de cada vez.
- Carga, volume e séries vêm de registros executados; 1RM continua rotulado como estimativa, calculado somente com entradas válidas e faixa de repetições definida.
- Não exibir “na nuvem” para dados que misturam origem local e remota. Usar “Último registro”, “Melhor carga” e “Sincronização pendente” quando aplicável.
- Sem histórico: orientação simples para registrar; nunca um gráfico fictício.
- Em Medidas & Hábitos, manter peso, medidas e fotos separados dos resultados de força. Não reformular nutrição nesta entrega.

### Padrão visual comum

- Laranja para ação principal; verde para concluído; vermelho reservado à ação destrutiva; neutros para secundárias.
- Menos caixa alta e títulos menores: “Evolução”, “Como fazer”, “Adicionar exercício”, “Continuar treino”.
- Garantir área confortável de toque e espaçamento entre ações. Botões visuais de 24–32 dp precisam ser conferidos com sua área efetiva de interação; o tamanho declarado sozinho não prova falha de acessibilidade no Compose.
- Testar fonte ampliada, teclado aberto, nomes longos e largura pequena antes de aprovar o layout.
- Padronizar kg, repetições, séries, min e ritmo; evitar mostrar decimais como inteiros sem aviso.

## Plano de implementação sugerido

| Etapa | Itens | Resultado esperado |
|---|---|---|
| 1 — impedir registros errados | 1, 3, 4, 5, 8, 9 | Sem progresso inventado, duplicação de séries, tempo sobrescrito ou exclusão acidental |
| 2 — proteger sessão e histórico | 2, 6, 7, 12, 16 | Dados detalhados e recuperáveis, IDs estáveis, gravação confiável |
| 3 — limpar a aba | 10, 13, 14, 15 | Fichas e exercícios aparecem primeiro; formulários mais claros |
| 4 — acertar consultas e caminhos | 11, 17, 18 | Evolução cronológica, corrida mais simples e retorno correto |

A correção de 3 deve aproveitar o modelo de registro detalhado de 2; até ele existir, não promover uma anotação isolada a treino completo. Mudanças de dados devem ter migração preservando os registros antigos. Melhorias visuais podem ser entregues separadamente das mudanças de armazenamento para facilitar revisão e rollback.

## Verificações necessárias antes de publicar mudanças

| Cenário | O que precisa ser verdadeiro |
|---|---|
| Conta sem treinos | Nenhuma evolução ou recorde fabricado |
| Uma série tocada repetidamente | Registro único ou alternância clara, sem inflar volume |
| Séries executadas fora de ordem | Marcação corresponde ao índice escolhido |
| Descanso +15 / −15 / pular | Tempo correto e apenas uma conclusão |
| Outra ficha iniciada com sessão ativa | Decisão explícita; nenhum descarte silencioso |
| Reinício do processo | Sessão e rascunho recuperáveis conforme contrato |
| Inclusão/substituição durante sessão | IDs e séries anteriores continuam coerentes |
| Falha durante salvamento da ficha | Estado anterior íntegro; mensagem acionável |
| Carga 12,5 kg e campo vazio | Decimal preservado; erro explicado sem valor inventado |
| Finalização repetida e sem internet | Um treino local, sincronização pendente distinta de falha |
| Semana 2, semana 10 e virada do ano | Ordem cronológica e deltas reais |
| Editar/excluir ficha após treinar | Histórico muscular não reclassificado silenciosamente |
| Histórico aberto em dia vazio | Filtro explícito e acesso a registros anteriores |
| Fonte ampliada e aparelho estreito | Campos, ações e títulos utilizáveis sem sobreposição |
| GPS negado, indisponível e sessão em segundo plano | Mensagem clara e preservação do fluxo atual; validar em aparelho real |

### Cobertura encontrada e lacunas

Há testes de persistência de fichas e logs, migração de banco, classificação muscular, cores do mapa e aspectos de ritmo/auto-pausa. Não localizei testes específicos cobrindo alternância por índice de série, ajuste de descanso, preservação dos IDs após edição, recuperação da sessão de musculação, gráficos sem histórico e ordenação semanal. A prioridade é adicionar testes pequenos desses comportamentos, não testes que apenas repetem a implementação.

Foram reproduzidas isoladamente as consequências aritméticas de ordenação textual de semanas, da sequência simulada de 60 kg e da sobrescrita do descanso. Essas verificações não substituem execução do app nem testes Android.

## O que manter

- Tema escuro e identidade laranja.
- Separação já existente entre lista de fichas e detalhe do treino.
- Biblioteca com busca e seleção múltipla.
- Guia por exercício e alternativa textual.
- Filtros de período já implementados em áreas relacionadas.
- Armazenamento local existente, proteção da exclusão de ficha e fluxo de permissões do GPS.
- O novo desenho do Foco Muscular; o problema remanescente é a origem histórica dos valores, não a necessidade de outro redesenho.

## O que não acrescentar agora

Sem novo framework, biblioteca de navegação, modelo 3D, sistema de recomendações ou novas camadas genéricas. Reutilizar os componentes existentes e corrigir os contratos de dados onde necessário.

**Simplificação possível:** remover o modal legado de evolução elimina 264 linhas desse arquivo, além do estado e bloco de chamada sem ativação. Não estimei economias dos demais componentes nem identifiquei dependência que precise ser adicionada ou removida para esse trabalho.

**Veredito:** começar pela integridade dos registros e, em seguida, reorganizar a entrada e a execução. Deixar a tela bonita sobre dados incorretos apenas esconde os problemas.

**Não verificado:** navegação e aparência no aparelho, TalkBack, teclado real, GPS em campo, consumo de bateria, recuperação real após encerramento do processo e comportamento de sincronização entre aparelhos. Não foi feita auditoria de segurança de Firebase nem revisão integral de dieta, compras ou autenticação. Nenhum achado de layout é apresentado como medição visual feita no celular.
