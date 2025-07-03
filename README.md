# Projeto-IA
## Agente Inteligente com Q-Learning em um Ambiente Gridworld
Implementação em Java de um agente que utiliza Aprendizado por Reforço, especificamente o algoritmo Q-Learning, para aprender a navegar de forma autônoma em um ambiente de grade (Gridworld), evitando obstáculos e buscando um objetivo.

## Descrição Geral
Este projeto consiste na criação de um agente inteligente que opera em um ambiente Gridworld 5x5. O agente não tem conhecimento prévio do ambiente e deve, através de tentativa e erro, aprender qual é o melhor caminho para sair de um ponto de partida e chegar a um ponto de destino. No percurso, existem "armadilhas" que penalizam o agente e um "objetivo" que o recompensa.

O cérebro do agente é o algoritmo Q-Learning, que constrói uma "tabela de consulta" (a Q-Table) para associar a qualidade de cada ação possível em cada estado do ambiente.
