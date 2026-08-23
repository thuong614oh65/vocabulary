FROM eclipse-temurin:21-jdk-jammy

# =========================================================
# CÀI PYTHON + PIP + VENV
# =========================================================

RUN apt-get update \
    && apt-get install -y python3 python3-pip python3-venv \
    && rm -rf /var/lib/apt/lists/*

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
# ĐƯA PYTHON VENV VÀO PATH
# =========================================================

ENV PATH="/opt/venv/bin:$PATH"

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
# MAVEN
# =========================================================

RUN chmod +x mvnw

RUN ./mvnw -DskipTests clean package

# =========================================================
# CHẠY SPRING BOOT (PORT 7860 CHO HUGGING FACE SPACES)
# =========================================================

USER user
ENV PORT=7860
EXPOSE 7860

CMD ["sh", "-c", "java -Dserver.port=${PORT:-7860} -jar target/*.jar"]