# S3·CloudFront 및 OCI 배포

프론트는 비공개 S3 버킷을 CloudFront OAC로만 제공한다. CloudFront 기본 동작은 S3를, 우선순위가 높은 `/api/*` 동작은 OCI Public IP의 HTTP `8080` Spring Boot 원본을 사용한다. `/api/*`는 캐시를 비활성화하고 API 메서드와 쿼리 문자열을 원본으로 전달한다.

## CloudFront 설정

- S3 원본은 OAC를 사용하고 버킷 공개 접근은 차단한다.
- 기본 S3 동작의 Viewer Request에 `deploy/cloudfront/spa-rewrite.js`를 CloudFront Function으로 연결한다. `/api/*` 동작에는 연결하지 않는다.
- OCI API 원본에 `X-ProjectX-Origin-Verify` Custom Header를 추가한다. 값은 OCI의 `/etc/projectx/backend.env`에 있는 `CLOUDFRONT_ORIGIN_SECRET`과 같아야 한다.
- 사용자 HTTPS는 CloudFront 기본 `cloudfront.net` 인증서에서 종료한다. 도메인과 인증서를 마련할 때까지 OCI 원본 연결은 HTTP로 유지한다.

CloudFront Custom Error Response로 SPA fallback을 처리하면 API 오류 응답까지 HTML로 바뀔 수 있으므로 사용하지 않는다.

## OCI 최초 설정

1. Java 17, Docker Engine, Docker Compose 플러그인을 설치한다.
2. `projectx` 시스템 사용자, `/opt/projectx`, `/etc/projectx`를 만들고 `projectx`를 Docker 그룹에 추가한다.
3. `deploy/docker-compose.yml`을 `/opt/projectx/docker-compose.yml`로, `deploy/systemd/projectx-backend.service`를 `/etc/systemd/system/projectx-backend.service`로 설치한다.
4. `deploy/.env.example`을 `/etc/projectx/backend.env`로 복사해 실제 값으로 채우고, 권한을 `600`으로 제한한다.
5. systemd daemon을 다시 읽고 `projectx-backend`를 활성화한다. 서비스가 시작되면 pgvector PostgreSQL 컨테이너도 시작된다.
6. OCI 인바운드 규칙은 관리자 SSH와 CloudFront에서 오는 `8080`만 허용한다. PostgreSQL `5432`는 외부에 열지 않는다.

## RAG 데이터

`V5__add_rag_document_chunks.sql`은 pgvector 확장과 문서 청크·메타데이터·임베딩 테이블 및 HNSW 코사인 인덱스를 만든다. `RAG_EMBEDDING_DIMENSIONS`는 선택한 Gemini 임베딩 모델의 출력 차원과 같아야 한다. 모델 또는 차원을 바꾸면 기존 임베딩을 재생성하고 인덱스를 다시 구축한다.

환경 변수는 `deploy/.env.example`을 기준으로 한다. Gemini API 키는 서버에만 저장하며 프론트와 Git에 포함하지 않는다.

## 수동 배포

프론트 빌드 결과물은 `deploy/scripts/deploy-frontend.sh`로 S3에 동기화하고 CloudFront 캐시를 무효화한다. Spring Boot 실행 JAR은 `deploy/scripts/deploy-backend.sh`로 OCI에 전송하고 `projectx-backend` 서비스를 재시작한다.

배포 후 CloudFront 주소에서 정적 파일·SPA 경로·`/api/health`를 확인한다. OCI IP로 `/api/health`를 직접 호출하면 403이어야 한다.
