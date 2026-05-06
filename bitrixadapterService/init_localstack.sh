#!/bin/bash

set -e

export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_REGION=us-east-1

sleep

echo "LocalStack запущен!"

# Создание S3 бакета
aws --endpoint-url=http://localhost:4566 s3 mb s3://test-bucket

# Проверка созданного бакета
aws --endpoint-url=http://localhost:4566 s3 ls
