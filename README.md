# ProjectX Backend

금융 AI 챌린지 자산관리 MVP 백엔드 오케스트레이션 서버

## 1. Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 4.1.1
- **Build Tool**: Gradle
- **Database**: PostgreSQL 16
- **External Communication**: RestClient (FastAPI `ai-service` 연동)

## 2. Branch Strategy
Git 브랜치는 `master`, `develop`, `feat` 3단계로 분기하며, 선형 히스토리를 위해 **Rebase and Merge**를 기본 병합 방식으로 채택합니다.

- `master`: 상용/데모 배포 브랜치 (EC2 환경에서 pull 및 docker compose 배포 대상)
- `develop`: 개발 통합 및 검증 브랜치
- `feat/{기능명}`: 단위 기능 개발 브랜치 (`develop`에서 분기)

