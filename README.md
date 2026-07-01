🚀 Demomo Project
Java 21 & Spring Boot 3.3.5 기반의 백엔드 데모 프로젝트입니다.

🛠 기술 스택
Language: Java 21 (LTS)

Framework: Spring Boot 3.3.5

Build Tool: Gradle

Config: YAML

📂 주요 구조
DemomoApplication.java: 메인 실행 클래스

application.yaml: 환경 설정 파일

build.gradle: 의존성 및 빌드 설정

⚙️ 실행 방법
Bash
./gradlew bootRun

## Google OAuth 설정

Google Cloud Console에서 OAuth 2.0 웹 클라이언트를 만든 뒤 승인된 리디렉션 URI에 아래 주소를 등록합니다.

`http://localhost:8080/login/oauth2/code/google`

실행 전에 다음 환경 변수를 설정합니다 (PowerShell 예시).

```powershell
$env:GOOGLE_CLIENT_ID="발급받은-client-id"
$env:GOOGLE_CLIENT_SECRET="발급받은-client-secret"
$env:OAUTH2_SUCCESS_REDIRECT_URI="http://localhost:8080/"
./gradlew bootRun
```
