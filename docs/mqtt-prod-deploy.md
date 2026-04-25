# MQTT 운영 배포 체크리스트

> 도메인·TLS 인증서 확보 후 진행. 코드(`docker-compose.prod.yml`, `mosquitto-prod.conf`)는 이미 완성된 상태.

---

## 1. GitHub Secrets 추가

`deploy.yml`의 `docker run` 명령에 아래 줄 추가:

```yaml
-e MQTT_BROKER_HOST=${{ secrets.MQTT_BROKER_HOST }} \
```

GitHub → Settings → Secrets → Actions에 `MQTT_BROKER_HOST` 등록.  
값: `localhost` (Spring Boot 컨테이너와 Mosquitto가 같은 EC2, 같은 Docker 네트워크일 때)

---

## 2. EC2에서 TLS 인증서 준비

도메인 확보 후 Let's Encrypt로 발급:

```bash
sudo apt install certbot
sudo certbot certonly --standalone -d <your-domain>

sudo mkdir -p /opt/smartoffice/certs
sudo cp /etc/letsencrypt/live/<your-domain>/chain.pem   /opt/smartoffice/certs/ca.crt
sudo cp /etc/letsencrypt/live/<your-domain>/cert.pem    /opt/smartoffice/certs/server.crt
sudo cp /etc/letsencrypt/live/<your-domain>/privkey.pem /opt/smartoffice/certs/server.key
sudo chmod 600 /opt/smartoffice/certs/server.key
```

---

## 3. docker-compose.prod.yml 실행

```bash
export MQTT_CERT_DIR=/opt/smartoffice/certs
docker compose -f docker-compose.prod.yml up -d
```

---

## 4. Mosquitto 패스워드 파일 생성 (최초 1회)

`mosquitto-prod.conf`가 `allow_anonymous false` + `password_file`을 요구:

```bash
docker exec -it smartoffice-mosquitto sh
mosquitto_passwd -c /mosquitto/config/passwd smartoffice-server
# 비밀번호 입력 후 exit
```

> passwd 파일이 컨테이너 내부에만 저장되므로 컨테이너 재생성 시 다시 실행해야 함.  
> 영속화가 필요하면 `docker-compose.prod.yml`에 config 디렉터리를 볼륨으로 마운트.

---

## 5. RPi5 클라이언트 인증서 발급

`require_certificate true` 설정이므로 RPi5도 클라이언트 인증서 필요:

- 자체 CA 구성 후 클라이언트 cert 서명, 또는 서버 CA로 서명
- RPi5 Python Paho 코드:

```python
client.tls_set(ca_certs="ca.crt", certfile="client.crt", keyfile="client.key")
```

---

## 6. EC2 보안 그룹 — 포트 8883 인바운드 허용

AWS 콘솔 → EC2 → Security Groups → Inbound Rules → Add Rule:

| Type       | Protocol | Port | Source              |
|------------|----------|------|---------------------|
| Custom TCP | TCP      | 8883 | RPi5 IP (또는 0.0.0.0/0) |
