import psycopg2
import pandas as pd
from constants import DATABASE_NAME, POSTGRES_USER_NAME, POSTGRES_PASSWORD, HOST, PORT

def fetch_faq_by_id(faq_id):

    conn = psycopg2.connect(
            dbname=DATABASE_NAME,
            user=POSTGRES_USER_NAME,
            password=POSTGRES_PASSWORD,
            host=HOST,
            port=PORT
        )
    cursor = None
    try:
        cursor = conn.cursor()
        
        select_query = """
        SELECT * 
        FROM faqs
        WHERE "ID" = %s;
        """
    
        cursor.execute(select_query, (faq_id,))
        
        # Fetch the row
        row = cursor.fetchone()
        
        if row:
            df = pd.DataFrame([row], columns=['ID', 'Chunks'])
            return df['Chunks'].values[0]
        else:
            print(f"No FAQ found for ID: {faq_id}")
            return None
    
    except Exception as e:
        print(f"Error: {e}")
        return None
    
    finally:
        if cursor:
            cursor.close()