# MQTT 운영 배포 체크리스트

> **완료된 항목**: 인증서 발급, Mosquitto 기동, 보안 그룹, deploy hook 설정 (2026-04-25)  
> **남은 항목**: GitHub Secrets 추가, RPi5 클라이언트 인증서

---

## ✅ 1. TLS 인증서 발급 (완료)

certbot-dns-cloudflare로 SAN 인증서 발급 완료:
- `api.sjparkx1129.com`, `mqtt.sjparkx1129.com` 포함
- 저장 위치: `/etc/letsencrypt/live/api.sjparkx1129.com/`
- 복사 위치: `/opt/smartoffice/certs/` (소유자: uid 1883)

자동 갱신 hook: `/etc/letsencrypt/renewal-hooks/deploy/mosquitto-reload.sh`  
(갱신 시 certs 복사 + mosquitto 재시작 자동 실행)

---

## ✅ 2. Mosquitto 기동 (완료)

```bash
export MQTT_CERT_DIR=/opt/smartoffice/certs
docker compose -f docker-compose.prod.yml up -d mosquitto
```

passwd 파일 생성 (임시 컨테이너 사용):
```bash
docker run --rm \
  -v ~/smartoffice/docker/mosquitto:/mosquitto/config \
  eclipse-mosquitto:2 \
  mosquitto_passwd -c -b /mosquitto/config/passwd smartoffice-server <password>
sudo chown 1883:1883 ~/smartoffice/docker/mosquitto/passwd
```

---

## ✅ 3. EC2 보안 그룹 — 포트 8883 (완료)

AWS 콘솔 → EC2 → Security Groups → Inbound Rules → Custom TCP 8883 추가.

---

## 4. GitHub Secrets 추가 (미완료)

`deploy.yml`의 `docker run` 명령에 아래 줄 추가:

```yaml
-e MQTT_BROKER_HOST=${{ secrets.MQTT_BROKER_HOST }} \
```

GitHub → Settings → Secrets → Actions에 `MQTT_BROKER_HOST = localhost` 등록.

---

## 5. RPi5 클라이언트 인증서 발급 (미완료)

`require_certificate true` 설정이므로 RPi5도 클라이언트 인증서 필요:

- 자체 CA 구성 후 클라이언트 cert 서명, 또는 서버 CA로 서명
- RPi5 Python Paho 코드:

```python
client.tls_set(ca_certs="ca.crt", certfile="client.crt", keyfile="client.key")
```
