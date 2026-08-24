FROM eclipse-temurin:21-jdk-jammy

# =========================================================
# CÀI PYTHON + PIP + VENV (KHÔNG CÀI JAVA 11)
# =========================================================

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip python3-venv wget curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# =========================================================
# CÀI MAVEN 3.9.9 TRỰC TIẾP (DÙNG JAVA 21, KHÔNG BỊ XUNG ĐỘT)
# =========================================================

RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar -xz -C /opt \
    && ln -s /opt/apache-maven-3.9.9 /opt/maven

# =========================================================
# TẠO PYTHON VIRTUAL ENVIRONMENT
# =========================================================

RUN python3 -m venv /opt/venv

# =========================================================
# CÀI EDGE-TTS
# =========================================================

RUN /opt/venv/bin/pip install --no-cache-dir edge-tts==7.2.8

# =========================================================
# KIỂM TRA EDGE-TTS
# =========================================================

RUN /opt/venv/bin/python -c "import edge_tts; print('EDGE_TTS_OK')"

# =========================================================
# ĐƯA MAVEN, JAVA 21 VÀ PYTHON VENV VÀO PATH
# =========================================================

ENV MAVEN_HOME="/opt/maven"
ENV JAVA_HOME="/opt/java/openjdk"
ENV PATH="/opt/maven/bin:/opt/java/openjdk/bin:/opt/venv/bin:$PATH"

# =========================================================
# PROJECT
# =========================================================

WORKDIR /app

COPY . .

# Tạo user 1000 cho Hugging Face Spaces và phân quyền
RUN useradd -m -u 1000 user && \
    mkdir -p /app/audio-data && \
    chown -R user:user /app /opt/venv && \
    chmod -R 777 /app /opt/venv

# =========================================================
# MAVEN BUILD
# =========================================================

RUN mvn -DskipTests clean package

# =========================================================
# CHẠY SPRING BOOT (TỰ ĐỘNG NHẬN DIỆN PORT TRÊN RENDER / HF SPACES)
# TỐI ƯU BỘ NHỚ CHO GÓI FREE 512MB RAM
# =========================================================

USER user
EXPOSE 10000 8080 7860

CMD ["sh", "-c", "java -XX:+UseSerialGC -Xss512k -XX:MaxMetaspaceSize=96m -Xms128m -Xmx220m -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar target/*.jar"]
