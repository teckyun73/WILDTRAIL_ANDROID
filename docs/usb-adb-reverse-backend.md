# USB ADB Reverse Backend Demo Guide

이 문서는 실제 Android 핸드폰을 USB로 PC에 연결한 뒤, 데모 앱이 PC에서 실행 중인 WildTrail FastAPI 백엔드에 접속하도록 설정하는 절차입니다.

## 언제 이 절차를 쓰는가

- 핸드폰 실기기에서 데모 APK를 실행할 때
- PC에서 백엔드 API를 `localhost:8000`으로 실행할 때
- 같은 Wi-Fi의 PC LAN IP 대신 USB 케이블만으로 앱과 백엔드를 연결하고 싶을 때

이 방식에서는 앱의 API URL을 반드시 아래 값으로 설정합니다.

```text
http://127.0.0.1:8000
```

`10.0.2.2`는 Android Emulator 전용 주소입니다. USB로 연결한 실제 핸드폰에서는 `ADB reverse`를 사용하고, 앱 안에서는 `127.0.0.1`을 선택해야 합니다.

## 사전 준비

1. 핸드폰에서 개발자 옵션을 켭니다.
2. 핸드폰에서 USB 디버깅을 켭니다.
3. 핸드폰과 PC를 USB 케이블로 연결합니다.
4. 핸드폰에 USB 디버깅 허용 팝업이 뜨면 허용합니다.
5. PC에 Android SDK Platform Tools가 설치되어 있어야 합니다.

이 PC의 기본 ADB 경로는 다음과 같습니다.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
```

## 1. USB 기기 인식 확인

PowerShell에서 다음 명령을 실행합니다.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices -l
```

정상 예시는 다음과 같습니다.

```text
List of devices attached
Y95P8DGEKNSSMZT8       device product:zircon_global model:23090RA98G device:zircon
```

`unauthorized`가 보이면 핸드폰 화면의 USB 디버깅 허용 팝업을 승인한 뒤 다시 실행합니다.

## 2. PC에서 백엔드 실행

WildTrail 백엔드 폴더로 이동합니다.

```powershell
cd "C:\CURSUR_PJT\ICT Education 20260429\backend"
```

가상환경의 Python으로 FastAPI 서버를 실행합니다.

```powershell
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

별도 PowerShell 창에서 백엔드 상태를 확인합니다.

```powershell
Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:8000/health" -TimeoutSec 10
```

정상이라면 `StatusCode`가 `200`으로 표시됩니다.

## 3. ADB reverse 연결 설정

백엔드가 실행 중인 상태에서 다음 명령을 실행합니다.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8000 tcp:8000
```

설정 여부를 확인합니다.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse --list
```

정상 예시는 다음과 같습니다.

```text
UsbFfs tcp:8000 tcp:8000
```

이 결과는 핸드폰의 `127.0.0.1:8000` 요청이 USB를 통해 PC의 `127.0.0.1:8000`으로 전달된다는 뜻입니다.

## 4. 앱에서 API URL 선택

1. 핸드폰에서 WildTrail 앱을 실행합니다.
2. 홈 화면에서 `식별` 또는 다른 섹터로 진입합니다.
3. 하단 탭에서 `상태`를 엽니다.
4. API 환경 프리셋 중 `ADB reverse`를 선택합니다.
5. Base URL이 아래 값인지 확인합니다.

```text
http://127.0.0.1:8000
```

6. `상태 확인`을 누릅니다.
7. 서비스 상태와 모델 상태가 표시되면 연결 성공입니다.

## 5. 데모 전 빠른 점검 명령

데모 직전에 아래 순서로 확인하면 됩니다.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

& $adb devices -l
Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:8000/health" -TimeoutSec 10
& $adb reverse tcp:8000 tcp:8000
& $adb reverse --list
```

앱에서는 `상태 > ADB reverse > 상태 확인`을 누릅니다.

## 문제 해결

### `adb reverse --list`에는 보이는데 앱 연결이 실패함

- PC에서 백엔드가 실행 중인지 확인합니다.
- `http://127.0.0.1:8000/health`가 PC PowerShell에서 `200`을 반환해야 합니다.
- 앱의 Base URL이 `http://127.0.0.1:8000`인지 확인합니다.
- 앱을 완전히 종료한 뒤 다시 열고 `ADB reverse` 프리셋을 다시 선택합니다.

### PC에서 `/health`가 연결 거부됨

백엔드가 실행 중이 아니거나 8000번 포트에서 Listen 중이 아닙니다. 백엔드를 다시 실행합니다.

```powershell
cd "C:\CURSUR_PJT\ICT Education 20260429\backend"
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

### 8000번 포트가 이미 사용 중이라고 나옴

이미 백엔드가 떠 있거나 다른 프로세스가 8000번을 사용 중입니다.

```powershell
Get-NetTCPConnection -LocalPort 8000 -ErrorAction SilentlyContinue
```

`Listen` 상태가 있고 `/health`가 `200`이면 그대로 사용해도 됩니다.

### `adb devices`에 `unauthorized`가 표시됨

핸드폰 화면에서 USB 디버깅 허용 팝업을 승인합니다. 팝업이 보이지 않으면 USB 케이블을 뺐다가 다시 연결하고, 개발자 옵션의 USB 디버깅을 껐다가 다시 켭니다.

### `10.0.2.2`로 설정했는데 실패함

정상입니다. `10.0.2.2`는 에뮬레이터에서만 PC 로컬 서버를 가리킵니다. USB로 연결한 실제 핸드폰에서는 `ADB reverse`와 `http://127.0.0.1:8000`을 사용합니다.

## 데모 성공 기준

- `adb devices -l`에서 실기기가 `device`로 표시됩니다.
- `adb reverse --list`에 `tcp:8000 tcp:8000`이 표시됩니다.
- PC에서 `http://127.0.0.1:8000/health`가 `200`을 반환합니다.
- 앱의 `상태` 화면에서 `ADB reverse`를 선택한 뒤 `상태 확인`이 성공합니다.
- 식별, 도감, 기록, 여행 기능에서 백엔드 데이터가 정상 표시됩니다.
