#!/bin/sh
set -e

KEY_DIR="/keys"
PRIVATE_KEY="$KEY_DIR/private.pem"
PUBLIC_KEY="$KEY_DIR/public.pem"

if [ -f "$PRIVATE_KEY" ] && [ -f "$PUBLIC_KEY" ]; then
  echo "Ключи уже существуют. Пропускаем генерацию."
  exit 0
fi

echo "Генерация ECC-ключевой пары (P-256)..."
mkdir -p "$KEY_DIR"

# Генерируем приватный ключ в PKCS#8
openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out "$PRIVATE_KEY"

# Извлекаем публичный ключ
openssl pkey -in "$PRIVATE_KEY" -pubout -out "$PUBLIC_KEY"

# Устанавливаем права:
chmod 644 "$PRIVATE_KEY"
chmod 644 "$PUBLIC_KEY"

echo "Ключи успешно созданы в $KEY_DIR"