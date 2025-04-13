import os

DATABASE_NAME = "reviews"
POSTGRES_USER_NAME = "postgres"
POSTGRES_PASSWORD = "12345678"
HOST = "localhost"
PORT = "5432"

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
EMBEDDING_MODEL = "text-embedding-ada-002"
MODEL = "gpt-4o-mini"

SERVICE_ACCOUNT_FILE = os.getenv("GOOGLE_SERVICE_ACCOUNT_FILE", "AndroidReview.json")
PACKAGE_NAME = os.getenv("PACKAGE_NAME", "com.zaggle")