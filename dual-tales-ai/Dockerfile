FROM python:3.11-slim

WORKDIR /app

# 시스템 패키지 (이미지 처리용)
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    fonts-nanum \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# 이미지 저장 디렉토리 생성
RUN mkdir -p story_outputs/images

EXPOSE 8080

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]
