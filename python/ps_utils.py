import psycopg2
import re
import pandas as pd
from llm_utils import generate_category
from psycopg2 import sql

def connect_to_database(dbname, username, password,host="localhost", port="5432"):
    conn = None 
    try:
        # Trying to establish a connection to the database
        conn = psycopg2.connect(
            dbname=dbname,
            user=username,
            password=password,
            host=host,
            port=port
        )
        print("Connected to the database")
        cursor = conn.cursor()
        cursor.execute("SELECT version();")
        db_version = cursor.fetchone()
        print("PostgreSQL version:", db_version)

    except Exception as e:
        print("Error while connecting to PostgreSQL:", e)
    
    return conn

def create_table(conn,table_name, ps_query):
    try:
        cursor = conn.cursor()
        # SQL query to create a table
        create_table_query = ps_query
        cursor.execute(create_table_query)
        conn.commit()
        
        print(f"Table {table_name} created successfully")

    except Exception as e:
        print(f"Error: {e}")
    
    finally:
        cursor.close()

def view_table_data(conn,table_name):
    try:
        cursor = conn.cursor()
        select_query = f"SELECT * FROM {table_name};"
        df = pd.read_sql_query(select_query, conn)
        return df

    except Exception as e:
        print("Error while fetching data from the database:", e)

    finally:
        if cursor:
            cursor.close()


def upsert_data(conn,table_name,conflict_column, data=None):
    try:

        conn.set_client_encoding('UTF8')
        cursor = conn.cursor()
        if isinstance(data, list):
            data = pd.DataFrame(data)
        

        fields = data.columns.tolist()
        # Create the SQL INSERT query dynamically
        columns_str = '", "'.join(fields)
        columns_str = '"' +columns_str + '"'
        placeholders = ", ".join(["%s"] * len(fields))
        update_clause = ", ".join([f'"{col}" = EXCLUDED."{col}"' for col in fields if col != conflict_column])

        insert_query = f"""
        INSERT INTO {table_name} ({columns_str})
        VALUES ({placeholders})
        ON CONFLICT ("{conflict_column}") 
        DO UPDATE SET {update_clause};
        """.format(
            table_name=sql.Identifier(table_name),
            columns_str=sql.SQL(columns_str),
            placeholders=sql.SQL(placeholders),
            conflict_column=sql.Identifier(conflict_column),
            update_clause=sql.SQL(update_clause)
        )


        for _, row in data.iterrows():
            cursor.execute(insert_query, tuple(row))

        conn.commit()

        print("Data inserted or updated successfully")

    except Exception as e:
        print("Error while inserting or updating data:", e)

    finally:
        if cursor:
            cursor.close()

def fetch_documents(conn):
    cursor = None
    try:
        cursor = conn.cursor()
        fetch_query = f"SELECT * FROM faqs ORDER BY \"ID\" ASC;"
        cursor.execute(fetch_query)
        rows = cursor.fetchall()
        documents = [row[1] for row in rows]  
        return documents 
    
    except Exception as e:
        print(f"Error: {e}")
        return []
    
    finally:
        if cursor:
            cursor.close()


def fetch_reviews(conn, category=None, limit=10, page=1):
    cursor = None
    try:
        cursor = conn.cursor()
        
        offset = (page - 1) * limit
        
        select_query = """
        SELECT * 
        FROM reviews
        """
        if category is not None:
            select_query += f" WHERE category = %s"
    
        select_query += " ORDER BY date DESC"
        
    
        select_query += f" LIMIT %s OFFSET %s;"
        
        if category is not None:
            cursor.execute(select_query, (category, limit, offset))
        else:
            cursor.execute(select_query, (limit, offset))
        
        rows = cursor.fetchall()
        df = pd.DataFrame(rows, columns=['ID', 'author_name', 'comment', 'date','star_rating', 'device', 'android_os_version', 'app_version_code', 'android_sdk', 'category'])
        
        return df
    
    except Exception as e:
        print(f"Error: {e}")
        return None
    
    finally:
        if cursor:
            cursor.close()


def fetch_review_by_id(conn, review_id):
    cursor = None
    try:
        cursor = conn.cursor()
        
        select_query = """
        SELECT * 
        FROM reviews
        WHERE "ID" = %s;
        """
    
        cursor.execute(select_query, (review_id,))
        
        row = cursor.fetchone()
        
        if row:
            df = pd.DataFrame([row], columns=['ID', 'date', 'author_name', 'comment', 'star_rating', 'device', 'android_os_version', 'app_version_code', 'android_sdk', 'category'])
            return df
        else:
            print(f"No document found for ID: {review_id}")
            return None
    
    except Exception as e:
        print(f"Error: {e}")
        return None
    
    finally:
        if cursor:
            cursor.close()

def extract_review_id(url):
    match = re.search(r'reviewId=([a-zA-Z0-9\-]+)', url)
    if match:
        return match.group(1)
    return None

def pre_process_reviews_csv(df):
    cleaned_data = df.dropna(subset=["Review Text","Review Submit Date and Time","Star Rating"])
    cleaned_data["Review ID"] = cleaned_data["Review Link"].apply(extract_review_id)
    cleaned_data = cleaned_data[["Review Text","Review Submit Date and Time","Star Rating","Device","App Version Code","App Version Name","Review ID"]]
    cleaned_data["category"], cleaned_data["author_name"] = zip(*cleaned_data["Review Text"].apply(lambda x: generate_category(x).values()))
    cleaned_data['Review Text'] = cleaned_data['Review Text'].apply(lambda x: x.encode('utf-8').decode('utf-8') if isinstance(x, str) else x)
    cleaned_data.rename(columns={
        "Review ID": "review_id",
        "User Name": "author_name",
        "Review Text": "comment",
        "Review Submit Date and Time": "date",
        "Star Rating": "star_rating",
        "Device": "device",
        "App Version Code": "app_version_code",
        }, inplace=True)
    return  cleaned_data
