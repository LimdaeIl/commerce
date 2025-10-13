# 프라이데이 - 이커머스 서비스 <a href="https://github.com/LimdaeIl/commerce"><img src="https://upload.wikimedia.org/wikipedia/commons/thumb/7/79/Spring_Boot.svg/512px-Spring_Boot.svg.png?20230616230349" align="left" width="100"></a>

[![views](https://myhits.vercel.app/api/hit/https%3A%2F%2Fgithub.com%2FLimdaeIl%2Fcommerce?color=green&label=views&size=small)](https://github.com/LimdaeIl/commerce)
[![GitHub issues](https://img.shields.io/github/issues/LimdaeIl/commerce)](https://github.com/LimdaeIl/commerce/issues)
[![GitHub PRs Closed](https://img.shields.io/github/issues-pr-closed/LimdaeIl/commerce)](https://github.com/LimdaeIl/commerce/pulls?q=is%3Apr+is%3Aclosed)
![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk)
[![Spring Boot 3.5.6](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?logo=thymeleaf)](https://www.thymeleaf.org/)
[![springdoc-openapi 2.8.13](https://img.shields.io/badge/OpenAPI-springdoc%202.8.13-6BA539?logo=openapiinitiative)](https://springdoc.org/)
[![Swagger UI](https://img.shields.io/badge/Swagger-UI-85EA2D?logo=swagger)](https://swagger.io/tools/swagger-ui/)
[![JJWT 0.12.6](https://img.shields.io/badge/JJWT-0.12.6-000000?logo=jsonwebtokens)](https://github.com/jwtk/jjwt)
[![Spring Security Crypto](https://img.shields.io/badge/Spring%20Security-Crypto-6DB33F?logo=springsecurity)](https://spring.io/projects/spring-security)
[![JPA / Hibernate](https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate)](https://hibernate.org/)
[![MySQL 8.0.43](https://img.shields.io/badge/MySQL-8.0.43-4479A1?logo=mysql)](https://www.mysql.com/)
[![Redis 7.2](https://img.shields.io/badge/Redis-7.2-DC382D?logo=redis)](https://redis.io/)
[![Micrometer](https://img.shields.io/badge/Micrometer-core-3C78D8?logo=micrometer)](https://micrometer.io/)
[![Spring Actuator](https://img.shields.io/badge/Spring-Actuator-6DB33F?logo=spring)](https://docs.spring.io/spring-boot/docs/current/actuator-api/htmlsingle/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?logo=gradle)](https://gradle.org/)
[![Asciidoctor 3.3.2](https://img.shields.io/badge/Asciidoctor-3.3.2-E40046?logo=asciidoctor)](https://asciidoctor.org/)
[![Spring REST Docs](https://img.shields.io/badge/Spring%20REST%20Docs-MockMvc-6DB33F?logo=spring)](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5)](https://junit.org/junit5/)
[![java-dotenv 5.2.2](https://img.shields.io/badge/dotenv-5.2.2-00C7B7?logo=dotenv)](https://github.com/cdimascio/java-dotenv)
[![Docker](https://img.shields.io/badge/Docker-Engine-2496ED?logo=docker)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://docs.docker.com/compose/)
[![mysqld-exporter 0.15.1](https://img.shields.io/badge/Prometheus-mysqld_exporter%200.15.1-E6522C?logo=prometheus)](https://github.com/prometheus/mysqld_exporter)
[![redis_exporter 1.62.0](https://img.shields.io/badge/Prometheus-redis_exporter%201.62.0-E6522C?logo=prometheus)](https://github.com/oliver006/redis_exporter)
![Toss Payments](https://img.shields.io/badge/Payments-Toss-0064FF)
[![GitHub Actions](https://img.shields.io/badge/GitHub-Actions-2088FF?logo=githubactions)](https://github.com/features/actions)
![CodeRabbit](https://img.shields.io/badge/Code%20Review-CodeRabbit-6C63FF)

## 프로젝트 목표

- 쿠팡, 11번가, 무신사 등의 전자상거래 시스템인 이커머스 도메인을 기반으로 상품, 카테고리, 주문, 결제 등의 서비스를 이해하고,  MSA 확장을 고려한 모놀리식 아키텍처 기반의 백엔드 시스템을 구축하고 배포와 자동화가 목표인 개인 프로젝트 입니다.
- 모놀리식 아키텍처 구조이지만, 유연한 확장성과 유지보수성을 높이기 위한 설계 구조를 갖추기 위해 도메인 주도 설계(DDD) 철학을 바탕으로 레이어드 아키텍처로 내부 도메인 패키지를 구성하고, 서로 다른 도메인의 데이터 통신을 위해 헥사고날 아키텍처(Ports/Adapters)을 채택합니다.

  

## 기술 사항

- **아키텍처**: Layered + Hexagonal(Ports/Adapters), 모놀리식 기반 MSA 확장 구조
- **도메인 경계**: DDD 기반 모듈링, 공통 유틸/에러 규격/응답 포맷 일원화
- **데이터**: Snowflake ID(분산 키), 카테고리 트리(Adjacency List + Path Enumeration), 복합 인덱스
- **인증/보안**: Filter + Resolver/AOP, 경로 기반 접근 제어(Allowlist), JWT Rotation/Blacklist, 이메일 인증
- **문서화/관측**: Swagger, Prometheus + Grafana 대시보드
- **결제/연동**: 토스 결제 API 연동
- **배포/자동화**: AWS EC2/RDS, GitHub Actions CI/CD, PR 단위 정적 리뷰(CodeRabbit)
- **성능 최적화**: 인덱스만으로 WHERE/ORDER BY/LIMIT 해결(Backward Index Scan 활용), 대용량 오프셋 대응



## 성과

**카테고리 조회 성능 최적화로 p95 응답 시간 90% 이상 단축**

> 스마트한 복합 인덱스 하나로 카테고리별 상품 조회 데이터 검색, 정렬, 페이지 넘김을 동시에 해결했습니다. 
> 그 결과, 2분 넘게 걸리던 작업을 1.5초로 단축시켜 수십 배의 성능 개선을 이뤄냈습니다.

카테고리별 상품 목록 조회 시 발생하던 심각한 지연 문제를 **'복합 인덱스'** 설계로 해결하여, 사용자 경험과 시스템 안정성을 크게 향상시켰습니다.

**✔︎ 압도적인 속도 개선**

- **느린 원인:** 기존에는 수백만 건의 상품을 전부 스캔하고 정렬하는 방식(`Full Scan`, `Filesort`)이라 페이지 뒤쪽으로 갈수록(OFFSET 80,000) 응답 시간이 **2분 이상** 소요되었습니다.
- **해결 방법:** **`category_id`와 `product_id`를 묶은 복합 인덱스**를 적용하여, 데이터 검색과 정렬을 한 번에 처리하도록 개선했습니다.
- **결과:** 이제는 아무리 깊은 페이지를 조회해도 **평균 1.2~1.5초** 만에 응답이 완료됩니다. 첫 페이지는 **0.3초 이내**로 훨씬 빠릅니다.

**✔︎ 안정성과 효율성 확보**

- 불필요한 데이터 스캔과 임시 정렬 과정이 사라져, 쿼리 실행 시간이 일정하게 유지됩니다. 덕분에 사용량이 많아져도 p95, p99 지표가 급증하지 않고 **안정적으로 유지**됩니다.
- 전체 데이터(약 486만 건) 대신 필요한 부분(약 8만 건)만 효율적으로 접근하여 **서버의 CPU와 메모리 사용량을 크게 줄였습니다.** 이는 더 많은 사용자가 동시에 서비스를 이용해도 쾌적한 속도를 보장한다는 의미입니다.

**✔︎ 한눈에 보는 Before & After**

| 항목          | **최적화 이전**                  | **최적화 이후 (현재)**                    |
| ------------- | -------------------------------- | ----------------------------------------- |
| **처리 방식** | 데이터 전체를 훑고, 일일이 정렬  | 필요한 부분만 골라 **인덱스**로 바로 처리 |
| **응답 속도** | **2분 이상** (먼 페이지 기준)    | **약 1.5초 이내** (어떤 페이지든)         |
| **안정성**    | 사용자가 몰리면 속도 급격히 저하 | 사용량과 무관하게 **안정적인 속도** 유지  |
| **서버 자원** | CPU/메모리 사용량 매우 높음      | 자원 사용량 **대폭 감소**, 효율성 증대    |



 **'인접 리스트'와 '경로 열거' 조합으로 카테고리 트리 쿼리 p95 최대 5배 개선**

> 직계 관계는 ‘인접 리스트’로 단순하고 빠르게, 전체 하위 트리는 미리 계산된 ‘경로’로 한 번에 조회하도록 구조를 이원화하여, 
> 복잡했던 트리 관련 API의 p95 응답 속도를 2~5배 개선했습니다.

느리고 비효율적이던 기존의 재귀적 트리 탐색 방식을 개선하여, 직계 탐색과 서브트리 전체 조회의 성능을 극적으로 향상시키고 시스템의 안정성을 확보했습니다.

**✔︎ 탐색 방식별 맞춤 최적화**

1. **직계 탐색 (부모 ↔ 자식): `인접 리스트`로 빠르고 간단하게**
   - **개선점:** 모든 카테고리가 자신의 `parent_id`를 갖는 간단한 구조를 활용, **인덱스**를 통해 단 한 번의 쿼리로 부모나 자식 목록을 O(logN)의 빠른 속도로 찾아냅니다.
   - **결과:** 메뉴 네비게이션, 브레드크럼(이동 경로) 표시 등의 응답 시간이 **평균 0.1초대**로 단축되어 사용자의 체감 지연이 크게 감소했습니다.
2. **서브트리 탐색 (특정 카테고리 하위 전체): `경로 열거`로 재귀 제거**
   - **개선점:** 각 카테고리에 '1/3/12'처럼 전체 경로를 텍스트로 저장하는 **`Materialized Path`** 전략을 도입했습니다. 덕분에 '1/3/%' 와 같은 검색 조건만으로 모든 하위 카테고리를 **재귀나 반복 조인 없이 한 번에** 가져올 수 있습니다.
   - **결과:** 수백 ms에서 1.2초까지 걸리던 하위 카테고리 전체 조회 p95 응답 속도를 **평균 0.2초대**로 **60~80% 단축**했습니다.

**✔︎ 시스템 안정성 및 유지보수 편의성 확보**

- **자원 효율화:** 상품 목록에서 특정 카테고리(및 하위) 상품을 필터링할 때, 복잡한 서브 쿼리 대신 미리 계산된 경로 값으로 조인 범위를 최소화하여 **DB 부하를 크게 줄였습니다.**
- **데이터 일관성:** 카테고리를 생성하거나 이동할 때 **경로 정보만 갱신**해주면 모든 조회에 즉시 반영되므로, 애플리케이션이나 캐시 로직이 단순해지고 데이터 불일치 가능성이 줄었습니다.

**✔︎ 한눈에 보는 Before & After**

| 조회 유형          | **최적화 이전 (재귀/다중 조인)** | **최적화 이후 (현재)**                 |
| ------------------ | -------------------------------- | -------------------------------------- |
| **직계 탐색**      | 부모/자식을 찾기 위해 반복 조인  | **인덱스**로 한 번에 범위 스캔         |
| **하위 전체 탐색** | 3~6회 이상 재귀 호출 (느림)      | **저장된 경로**로 단번에 프리픽스 검색 |
| **응답 속도(p95)** | 400ms ~ **1200ms**               | **120ms ~ 250ms**                      |



**JWT 토큰 회전 및 블랙리스트 도입으로 실시간 보안 통제 및 운영 안정성 강화**

인증 시스템의 보안 수준을 높이고 운영 효율을 개선하기 위해, JTI(JWT 고유 식별자)와 Redis를 결합한 토큰 무효화 전략 및 경로 기반의 체계적인 인증 필터를 구현했습니다.

**✔︎ 주요 보안 강화 전략**

1. **토큰 회전(Rotation) & 블랙리스트: 즉각적인 무효화와 재사용 방지**
   - **동작 방식:**
     - 모든 토큰에 고유 ID(**JTI**)를 부여합니다.
     - 사용자가 로그아웃하거나 토큰을 재발급받으면, 이전에 사용하던 토큰의 JTI를 Redis **블랙리스트**에 즉시 등록합니다.
     - 동시에, 해당 사용자가 유효하게 사용할 수 있는 토큰은 단 하나임을 Redis에 기록하여 **재사용(Replay Attack)을 방지**합니다.
   - **효과:** 모든 요청은 Redis를 통해 O(1)의 매우 빠른 속도로 유효성을 검증받으므로, 시스템에 부하를 주지 않으면서도 탈취된 토큰의 접근을 **지연 없이 즉시 차단**할 수 있습니다.
2. **경로 기반 인증 필터: 체계적인 접근 제어**
   - **개선점:** 모든 API 요청이 단일 인증 필터(`JwtAuthenticationFilter`)를 거치도록 설계했습니다.
   - **정책 관리:** URL 패턴에 따라 **`인증 불필요(Exclude)`**, **`인증 선택(Optional)`**, **`인증 필수(Required)`** 정책을 설정 파일(yml)에서 중앙 관리하여, 코드 변경 없이 유연하게 접근 제어 범위를 조정할 수 있습니다.

**✔︎ 운영 안정성 및 효율성**

- **표준화된 예외 처리:** 토큰 만료, 위변조, 누락 등 다양한 인증 오류를 일관된 형식의 JSON 응답으로 표준화하여, 클라이언트에서의 오류 처리를 간소화했습니다.
- **보안 설계:** Access/Refresh 토큰의 서명 키를 분리하고, 역할(Role) 정보는 Access 토큰에만 담는 등 보안 기본 원칙을 준수했습니다.
- **모니터링:** 블랙리스트 적중률, 인증 거부 횟수 등의 지표를 Prometheus/Grafana로 시각화하여 이상 징후를 신속하게 탐지할 수 있는 기반을 마련했습니다.(권장)

**✔︎ 한눈에 보는 동작 흐름**

| 단계                   | 설명                                                         |
| ---------------------- | ------------------------------------------------------------ |
| **1. 로그인/재발급**   | 새로운 Access/Refresh 토큰 발급. 유효한 토큰 정보(JTI)를 Redis에 업데이트 |
| **2. 로그아웃/재발급** | 사용이 끝난 이전 토큰의 JTI를 즉시 **Redis 블랙리스트**에 등록 |
| **3. API 요청**        | 인증 필터가 요청 경로에 맞는 **인증 정책**을 확인            |
| **4. 토큰 검증**       | **블랙리스트**에 있는지, 가장 최신 토큰이 맞는지 Redis로 **1~2회 조회하여** 빠르게 판별 |
| **5. 처리**            | 검증 성공 시, 요청에 사용자 정보(ID, 역할)를 담아 컨트롤러로 전달 |





### 🛒 friday-commerce (Application)

| Category           | Technology                                                   |
| ------------------ | ------------------------------------------------------------ |
| **IDE**            | IntelliJ IDEA                                                |
| **Language**       | **Java 21**                                                  |
| **Framework**      | **Spring Boot 3.5.6** (Web, Validation, Data JPA, Actuator, Mail, Thymeleaf) |
| **Auth/Security**  | JJWT **0.12.6**, Spring Security Crypto, Filter/Resolver 기반 JWT |
| **API Docs**       | springdoc-openapi **2.8.13** (Swagger UI)                    |
| **ORM**            | JPA (Hibernate)                                              |
| **Database**       | **MySQL 8.0.43 (InnoDB)**                                    |
| **NoSQL / Cache**  | **Redis 7.2** (AOF, allkeys-lru)                             |
| **Metrics**        | Micrometer Core + Prometheus Registry                        |
| **Email**          | Spring Mail (SMTP)                                           |
| **Build**          | Gradle (Asciidoctor 3.3.2, Spring REST Docs)                 |
| **Container**      | Docker / **Docker Compose**                                  |
| **Exporters**      | mysqld-exporter **0.15.1**, redis_exporter **1.62.0**        |
| **Testing**        | JUnit 5, spring-boot-starter-test, **Spring REST Docs + MockMvc** |
| **Config**         | `java-dotenv 5.2.2`, 환경변수/프로필 분리                    |
| **Observability**  | Actuator `/actuator/prometheus`                              |
| **Payment**        | **토스 결제 API**                                            |
| **CI/CD & Review** | **GitHub Actions**, **CodeRabbit**(PR 리뷰 자동화)           |

------

### ☁️ Infrastructure / Deployment

| Category             | Technology                                                   |
| -------------------- | ------------------------------------------------------------ |
| **Compute**          | **AWS EC2 t3.small** (2 vCPU, 2GB RAM)                       |
| **OS**               | **Ubuntu (LTS)**                                             |
| **Database**         | **AWS RDS MySQL (t3 계열)**                                  |
| **Monitoring**       | **Prometheus 2.54.1**, **Grafana 11.1.0**                    |
| **Networking**       | Security Group 최소 포트, `:10000`(App), `:9090`(Prometheus), `:3000`(Grafana) |
| **Logging**          | 애플리케이션 로그 로테이션, RDS 자동 백업                    |
| **Runtime 튜닝(예)** | G1GC, `Xmx≈1024m`, HikariCP `maxPoolSize≈20`                 |



## 시스템 아키텍처

<img width="823" height="405" alt="image" src="https://github.com/user-attachments/assets/4a457547-53ca-49ed-9413-46605b80a48f" />


## 이동하기
- [📖Notion으로 이동하기](https://optional94.notion.site/Friday-Commerce-24e63f08977f800eb9ccc032ecf1b437?source=copy_link)
- [📕PPT로 이동하기](https://www.canva.com/design/DAG1dbNrxi8/4qEk1UMHPZCxQ5hCY1QOHg/edit?utm_content=DAG1dbNrxi8&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)

