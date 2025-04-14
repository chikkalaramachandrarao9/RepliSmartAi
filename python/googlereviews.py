from google.oauth2 import service_account
from googleapiclient.discovery import build
from datetime import datetime
import os
from llm_utils import generate_category
import pandas as pd
from datetime import datetime,timezone
from ps_utils import connect_to_database,upsert_data
from constants import DATABASE_NAME, POSTGRES_USER_NAME, POSTGRES_PASSWORD, HOST, PORT, SERVICE_ACCOUNT_FILE, PACKAGE_NAME

# Initialize Google API service
credentials = service_account.Credentials.from_service_account_file(
    SERVICE_ACCOUNT_FILE, scopes=['https://www.googleapis.com/auth/androidpublisher']
)
service = build('androidpublisher', 'v3', credentials=credentials)

def fetch_all_reviews():
    all_reviews = []
    next_page_token = None

   
    while True:
        # Request for reviews, including pagination
        response = service.reviews().list(
            packageName=PACKAGE_NAME,
            maxResults=200,
            token=next_page_token
        ).execute()

       
        reviews = response.get('reviews', [])
        all_reviews.extend(reviews)

        next_page_token = response.get('nextPageToken', None)
        if not next_page_token:
            break
        
    return all_reviews

def process_reviews(all_reviews):
    result = []

    for review in all_reviews:
        for comment in review.get('comments', []):
            user_comment = comment.get('userComment', {})
            if not user_comment.get('text'):
                continue  

            # Construct metadata string
            metadata = " | ".join([
                f"Device: {user_comment.get('device', '')}",
                f"App version: {user_comment.get('appVersionName', '')}",
                f"Android SDK: {user_comment.get('androidOsVersion', '')}",
            ])

          
            timestamp_seconds = int(user_comment.get('lastModified', {}).get('seconds', 0))
            if timestamp_seconds:
                date_utc = datetime.fromtimestamp(timestamp_seconds, tz=timezone.utc)
                date_formatted = date_utc.isoformat()  
            else:
                date_formatted = None

            category, _ =  generate_category(user_comment).values()

    
            result.append({
                "review_id": review.get('reviewId', ''),
                "author_name": review.get('authorName', ''),
                "comment": user_comment.get('text', ''),
                "date": date_formatted,
                "star_rating": user_comment.get('starRating', 3),
                "device": user_comment.get('device', ''),
                "android_os_version": user_comment.get('androidOsVersion', ''),
                "app_version_code": user_comment.get('appVersionCode', ''),
                "android_sdk": user_comment.get('androidSdk', ''),
                "category": category
            })
    
    df = pd.DataFrame(result, columns=[
        "review_id", "author_name", "comment", "date", 
        "star_rating", "device", "android_os_version", 
        "app_version_code", "android_sdk", "category"
    ])

    conn = connect_to_database(DATABASE_NAME, POSTGRES_USER_NAME, POSTGRES_PASSWORD, host=HOST, port=PORT)

    upsert_data(conn, "reviews", "review_id", data=df)
    return result

def publish_reply_to_review(review_id, reply_text):
    if not review_id or not reply_text:
        return {"error": "Missing review ID or reply text"}, 400

    reply_body = {
        'replyText': reply_text
    }

    try:
        # Post the reply to the Google Play review
        reply_response = service.reviews().reply(
            packageName=PACKAGE_NAME,
            reviewId=review_id,
            body=reply_body
        ).execute()
        reply_result = {
            "message": "Reply published successfully",
            "reply_id": review_id,
            "response": reply_response
        }
        return reply_result

    except Exception as e:
        return {"error": f"Failed to publish reply: {str(e)}"}, 500
