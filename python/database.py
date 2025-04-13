from ps_utils import connect_to_database, create_table
from constants import DATABASE_NAME, POSTGRES_USER_NAME, POSTGRES_PASSWORD, HOST, PORT


conn = connect_to_database(DATABASE_NAME, POSTGRES_USER_NAME, POSTGRES_PASSWORD, host=HOST, port=PORT)
create_table(conn, "reviews", """
             CREATE TABLE IF NOT EXISTS reviews (
              review_id UUID PRIMARY KEY,
              author_name TEXT,
              comment TEXT,
              date TIMESTAMPTZ,
              star_rating INT,
              device TEXT,
              android_os_version TEXT,
              app_version_code TEXT,
              android_sdk TEXT,
              category TEXT
             );""")  

create_table(conn, "faqs", """
             CREATE TABLE IF NOT EXISTS faqs (
            "ID" SERIAL PRIMARY KEY,
            "Chunks" TEXT
        );""")  

conn.close()