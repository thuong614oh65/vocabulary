FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update \
    && apt-get install -y python3 python3-pip python3-venv \
    && rm -rf /var/lib/apt/lists/*

RUN python3 -m pip install --break-system-packages edge-tts==7.2.8

RUN python3 -c "import edge_tts; print('EDGE_TTS_OK')"

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw -DskipTests clean package

CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar target/*.jar"]