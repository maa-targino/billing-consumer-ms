# billing-consumer-ms

Microsserviço consumidor de faturas, desenvolvido como projeto de laboratório para praticar arquitetura hexagonal, mensageria assíncrona e deploy em Kubernetes (minikube) com uma esteira Jenkins.

Ele é a metade "consumer" de um pipeline de dados maior: um batch (`billing_producer_batch`, ainda a ser desenvolvido) vai gerar faturas via IA (Groq) e publicá-las no RabbitMQ; este microsserviço consome essas mensagens e persiste tudo no PostgreSQL.

## Índice

- [Visão geral do laboratório](#visão-geral-do-laboratório)
- [O que o consumer faz](#o-que-o-consumer-faz)
- [Arquitetura hexagonal](#arquitetura-hexagonal)
- [Modelo de dados e persistência](#modelo-de-dados-e-persistência)
- [Mensageria (RabbitMQ)](#mensageria-rabbitmq)
- [Stack técnica](#stack-técnica)
- [Testes](#testes)
- [Infraestrutura no minikube](#infraestrutura-no-minikube)
- [Build e deploy](#build-e-deploy)
- [Esteira Jenkins](#esteira-jenkins)
- [Rodando localmente](#rodando-localmente)
- [Repositórios relacionados](#repositórios-relacionados)
- [Limitações conhecidas](#limitações-conhecidas)

## Visão geral do laboratório

Todo o ambiente roda num cluster **minikube** local (driver **Hyper-V**, sem Docker Desktop instalado). O cluster hospeda:

| Pod | Papel |
|---|---|
| `postgres` | Banco de dados relacional, persiste clientes/produtos/faturas |
| `rabbitmq` | Broker de mensageria (com plugin de management) |
| `jenkins` | Esteira CI (build + testes) |
| `billing-consumer-ms` | Este microsserviço |

O `billing_producer_batch` (ainda não implementado) vai rodar fora do cluster, agendado via cron, chamando a API da Groq para gerar faturas e publicando-as na fila que este consumer escuta.

## O que o consumer faz

1. Escuta a fila `qq.billing.in` do RabbitMQ (via `@RabbitListener`).
2. Recebe um payload JSON (`InvoiceMessage`) contendo uma fatura, com cliente e itens de linha aninhados.
3. Valida o payload com Bean Validation — importante porque quem gera a mensagem é um agente de IA, então o contrato precisa ser validado antes de qualquer persistência.
4. Verifica idempotência: se a fatura (`invoiceNumber`) já existe, ignora a mensagem (protege contra reentrega da fila).
5. Resolve **cliente** por chave natural (`email`) e **produto** por chave natural (`name`) — se já existem, reaproveita o cadastro; se não, cria um novo. Isso evita duplicar clientes/produtos a cada fatura repetida.
6. Persiste a fatura e seus itens de linha em uma única transação.
7. Em caso de erro de processamento, a exceção propaga (sem `try/catch` no listener) para que o mecanismo de **retry + DLQ** do Spring AMQP entre em ação.

## Arquitetura hexagonal

O projeto segue o padrão **ports & adapters**, isolando o domínio de qualquer detalhe técnico (JPA, RabbitMQ, Spring):

```
com.billing.consumer
├── domain/                         # Núcleo: POJOs imutáveis, sem JPA/Spring
│   ├── Invoice.java
│   ├── Customer.java
│   ├── Product.java
│   └── InvoiceLineItem.java
│
├── application/
│   ├── port/
│   │   ├── in/                     # Portas de entrada (o que o mundo externo pode pedir)
│   │   │   ├── IngestInvoiceUseCase.java
│   │   │   └── IngestInvoiceCommand.java
│   │   └── out/                    # Portas de saída (o que o domínio precisa do mundo externo)
│   │       ├── InvoiceRepositoryPort.java
│   │       ├── CustomerRepositoryPort.java
│   │       └── ProductRepositoryPort.java
│   └── service/
│       └── InvoiceIngestionService.java   # Implementa o use case, só depende de ports
│
├── adapter/
│   ├── in/messaging/                # Adapter de entrada: RabbitMQ → domínio
│   │   ├── InvoiceListener.java             # @RabbitListener, fino, só delega
│   │   ├── InvoiceMessageMapper.java        # DTO validado → IngestInvoiceCommand
│   │   └── dto/                             # Payloads com Bean Validation
│   │
│   └── out/persistence/             # Adapter de saída: domínio → PostgreSQL
│       ├── entity/                          # Entidades JPA (@Entity), isoladas do domínio
│       ├── *JpaRepository.java               # Spring Data, package-private
│       ├── *RepositoryAdapter.java           # Implementam os ports, mapeiam entity↔domínio
│       └── mapper/                           # Mappers entity↔domínio
│
└── config/
    └── RabbitConfig.java             # Exchange, filas, DLQ, conversor JSON
```

**Por que essa separação:**

- O **domínio** (`Invoice`, `Customer`, `Product`, `InvoiceLineItem`) é imutável e não tem nenhuma dependência de framework — poderia ser testado e reutilizado sem Spring, sem JPA, sem nada.
- A **aplicação** define casos de uso (`IngestInvoiceUseCase`) e os contratos de que precisa do mundo externo (`*RepositoryPort`), sem saber *como* esses contratos são implementados.
- Os **adapters** conectam o mundo real (RabbitMQ, PostgreSQL) aos ports da aplicação. Trocar o Postgres por outro banco, ou o RabbitMQ por outro broker, exigiria mudar só a camada de adapter — domínio e aplicação não seriam tocados.
- As **entidades JPA** (`CustomerEntity`, `InvoiceEntity`, etc.) são objetos separados dos objetos de domínio, com mappers dedicados. Isso é mais verboso, mas garante que anotações de persistência (`@Entity`, `@Column`, etc.) nunca vazem para o domínio.
- Ao persistir uma fatura, `InvoiceEntityMapper` usa `EntityManager.getReference()` para ligar cliente/produto pela FK sem disparar um `SELECT` desnecessário — cliente e produto já foram resolvidos (e persistidos) por seus próprios adapters antes da fatura ser salva.

## Modelo de dados e persistência

Schema PostgreSQL (gerenciado externamente — `spring.jpa.hibernate.ddl-auto=validate`, o Hibernate só valida que as entidades batem com as tabelas, nunca cria/altera schema):

```
customers            (customer_id PK, company_name, contact_name, email, billing_address, shipping_address)
products_services    (product_id PK, name, description, unit_price, tax_rate)
invoices             (invoice_id PK, invoice_number UNIQUE, customer_id FK, issue_date, due_date, status, notes)
invoice_line_items   (line_item_id PK, invoice_id FK, product_id FK, quantity, historic_unit_price,
                       discount_amount, tax_amount, line_total)
```

Pontos de design:

- `status` é `String`, não `enum` — os valores vêm de um agente de IA, e um `@Enumerated(STRING)` falharia diante de qualquer valor inesperado fora do enum.
- `email` (cliente) e `name` (produto) são chaves naturais usadas para resolver duplicidade — não há índice único no banco para `email`, a resolução é feita na aplicação (`findByEmail`/`findByName` antes de criar).
- `invoice_number` tem constraint `UNIQUE` no banco **e** é checado explicitamente (`existsByInvoiceNumber`) antes de processar, garantindo idempotência mesmo com reentrega de mensagens.

## Mensageria (RabbitMQ)

| Recurso | Nome |
|---|---|
| Exchange | `ex.billing` (direct) |
| Fila principal | `qq.billing.in` |
| Routing key | `billing.routing.key` |
| Dead-letter queue | `qq.billing.dlq` |

Configuração de resiliência (`application.properties`):

- Retry automático: até 3 tentativas, com 2s de intervalo inicial.
- `default-requeue-rejected=false`: depois de esgotar as tentativas, a mensagem vai para a DLQ em vez de voltar pra fila principal em loop infinito.
- Conversor JSON (`JacksonJsonMessageConverter`) configurado manualmente — o Spring Boot não autoconfigura isso para AMQP.
- Validação Bean Validation habilitada nos listeners via `RabbitListenerConfigurer` — sem isso, `@Valid` no listener seria inerte.

## Stack técnica

- **Java 25** (LTS)
- **Spring Boot 4.1.1**
- **Spring Data JPA** + **Hibernate 7** (driver PostgreSQL)
- **Spring AMQP** (RabbitMQ)
- **Bean Validation** (Hibernate Validator)
- **Lombok** (nas entidades JPA — não no domínio)
- **JUnit 5**, **Mockito**, **AssertJ**, **Testcontainers**

## Testes

A suíte segue a pirâmide de testes, alinhada com as camadas hexagonais:

| Classe | Tipo | O que cobre |
|---|---|---|
| `InvoiceTest` | Unitário puro | Invariantes do domínio (imutabilidade, id nulo até persistir) |
| `InvoiceIngestionServiceTest` | Unitário (Mockito) | Idempotência, resolução de cliente/produto novo vs. existente, múltiplos itens |
| `InvoiceListenerTest` | Unitário | Listener delega corretamente o comando mapeado ao use case |
| `InvoiceMessageValidationTest` | Unitário | Bean Validation rejeita payload malformado (crítico: payload vem de uma IA) |
| `InvoicePersistenceAdapterTest` | Integração (Testcontainers) | Mappers entity↔domínio, queries por chave natural, cascade de itens, contra um Postgres real efêmero |

Rodar tudo:

```bash
./mvnw test
```

O teste de persistência sobe um Postgres real via **Testcontainers** — exige um Docker acessível. Em ambientes sem Docker Desktop (como este, que usa minikube com driver Hyper-V), é possível apontar o Testcontainers para o daemon dentro da VM do minikube:

```powershell
minikube docker-env --shell powershell | Invoke-Expression
$env:TESTCONTAINERS_HOST_OVERRIDE = (minikube ip)
./mvnw test -Dtest=InvoicePersistenceAdapterTest
```

## Infraestrutura no minikube

Os manifestos Kubernetes de toda a infraestrutura do laboratório (Postgres, RabbitMQ, Jenkins e este consumer) estão centralizados no repositório [`billing-k8s-manifests`](https://github.com/maa-targino/billing-k8s-manifests) — este repo só mantém uma cópia local em [`k8s/app.yaml`](k8s/app.yaml).

Serviços expostos via **NodePort** (em vez de `ClusterIP` + `kubectl port-forward`), porque port-forward derruba conexões de longa duração como um consumer AMQP ou um pool de conexões JDBC:

| Service | Porta interna | NodePort |
|---|---|---|
| `postgres-service` | 5432 | 30709 |
| `rabbitmq-service` (AMQP) | 5672 | 31449 |
| `rabbitmq-service` (management) | 15672 | 31368 |
| `jenkins-service` | 8080 | 30000 |

O `billing-consumer-ms`, quando rodando **dentro** do cluster (via Deployment), usa os nomes internos de Service (`postgres-service`, `rabbitmq-service`) em vez do NodePort — só quem está fora do cluster (IDE local, DBeaver, esta documentação) precisa do NodePort + IP do node (`minikube ip`).

O `billing-consumer-ms` **não expõe nenhuma porta HTTP** — é um consumidor de fila puro, sem `spring-boot-starter-web`. Por isso o `Deployment` não tem `livenessProbe`/`readinessProbe` HTTP nem `Service` associado (uma tentativa inicial de usar probes em `/actuator/health` derrubava o pod em loop, já que nada escuta em porta nenhuma).

## Build e deploy

Sem Docker Desktop instalado, a imagem é construída direto no runtime do minikube, sem precisar do CLI `docker` no host:

```powershell
./mvnw package -DskipTests
minikube image build -t billing-consumer-ms:latest .
kubectl apply -f k8s/app.yaml
```

O `Dockerfile` é single-stage: copia o jar já compilado pelo Maven e roda com `eclipse-temurin:25-jre`. O `imagePullPolicy: Never` no manifesto reflete que a imagem nunca vem de um registry — ela já está carregada no node pelo `minikube image build`.

## Esteira Jenkins

O `Jenkinsfile` deste repositório cobre a etapa de **build e testes**:

1. Checkout do repositório.
2. `./mvnw clean package`, excluindo `InvoicePersistenceAdapterTest` (o pod do Jenkins não tem Docker acessível para o Testcontainers).
3. Publica resultados de teste (JUnit) e o jar como artefato.

**Build de imagem e deploy continuam manuais**, por decisão consciente nesta primeira versão: o pod do Jenkins roda dentro do próprio cluster e, por padrão, não tem `kubectl` configurado com RBAC para aplicar manifests, nem o binário `minikube` (que é uma ferramenta de host). Fechar esse ciclo dentro da esteira exigiria configurar RBAC + Kaniko (para build de imagem sem Docker daemon), o que fica como evolução futura.

Configuração do job: Pipeline apontando para este repositório (branch `main`), script path `Jenkinsfile` (default), credencial de acesso via token do GitHub (repositório privado).

## Rodando localmente

Pela IDE (IntelliJ), sem precisar do Deployment no cluster:

1. Garanta que `postgres-service` e `rabbitmq-service` estão como `NodePort` (já são, por padrão, neste cluster).
2. `application.properties` já aponta para o IP do minikube + NodePorts — ajuste se o IP mudar (`minikube ip`, muda se o cluster for recriado).
3. Rode `BillingConsumerMsApplication`.

**Importante:** nunca rode a aplicação simultaneamente no IntelliJ e como pod no cluster — os dois competem como consumidores concorrentes da mesma fila, e mensagens podem ser entregues para qualquer um dos dois de forma imprevisível.

## Repositórios relacionados

- [`billing-k8s-manifests`](https://github.com/maa-targino/billing-k8s-manifests) — manifestos Kubernetes centralizados (Postgres, RabbitMQ, Jenkins, este consumer).
- `billing_producer_batch` — ainda não implementado. Vai gerar faturas via IA (Groq) e publicá-las na fila que este consumer escuta, seguindo a mesma arquitetura hexagonal.

## Limitações conhecidas

Decisões conscientes de simplicidade para este laboratório, não recomendadas para produção sem revisão:

- **Sem volumes persistentes**: Postgres e RabbitMQ perdem todos os dados se o pod for recriado (sem `PersistentVolumeClaim`).
- **Credenciais em texto plano**: usuário/senha do Postgres e RabbitMQ estão direto no `application.properties` e nos manifests — não há `Secret` do Kubernetes.
- **Uma réplica**: nenhum dos Deployments tem mais de uma réplica; não há alta disponibilidade.
- **IP do minikube hardcoded**: `application.properties` e os NodePorts assumem que o IP do minikube (`minikube ip`) não muda — ele pode mudar se o cluster for recriado do zero.
- **Esteira incompleta**: Jenkins só builda e testa; build de imagem e deploy são manuais (ver [Esteira Jenkins](#esteira-jenkins)).
