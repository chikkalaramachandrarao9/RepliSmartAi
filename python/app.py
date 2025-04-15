# Import requirements
import mimetypes
import pandas as pd
from flask import Flask, request, jsonify
from flask_cors import CORS
from werkzeug.utils import secure_filename
from llm_utils import generate_feedback_reply,append_fiass_index_file
from googlereviews import publish_reply_to_review
from ps_utils import connect_to_database,fetch_reviews,upsert_data
from constants import MODEL, DATABASE_NAME, POSTGRES_USER_NAME, POSTGRES_PASSWORD, HOST, PORT
from db_helper import get_last_id

# Initialize Flask and CORS
app = Flask(__name__)
CORS(app, resources={r"/api/*": {"origins": "*"}})

conn = connect_to_database(DATABASE_NAME, POSTGRES_USER_NAME, POSTGRES_PASSWORD, host=HOST, port=PORT)

# Review response route
@app.route('/review-response', methods=['POST'])
def review_response():
    data = request.get_json()
    review = data.get('review')
    rating = data.get('rating')
    username = data.get('username', 'Guest')

    if not review or not isinstance(rating, int) or not (1 <= rating <= 5):
        return jsonify({"error": "Invalid review or rating"}), 400

    feedback = generate_feedback_reply(MODEL, review, username,rating)
    response = {"username": username, "response": feedback}

    return jsonify(response)


# Function to check if the file is an allowed type
def allowed_file(filename: str) -> bool:
    mime_type, _ = mimetypes.guess_type(filename)
    return mime_type == 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'


# Upload file route
@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    filename = secure_filename(file.filename)

    if not allowed_file(filename):
        return jsonify({"error": "Invalid file type. Please upload an Excel file."}), 400

    try:
        df = pd.read_excel(file)
        df_cleaned = df.dropna(subset=['User Query', 'Product Responses'])
        documents = [
                f"User Query: {row['User Query']} | Response: {row['Product Responses']}"
                for _, row in df_cleaned.iterrows()
             ]
        documents_df = pd.DataFrame(documents, columns=['Chunks'])
        last_id = get_last_id(conn, "faqs")
        documents_df["ID"] = range(last_id + 1, last_id + 1 + len(documents_df))
        upsert_data(conn, "faq", "ID", data=documents_df)
        append_fiass_index_file(documents)
        return jsonify({"message": "File uploaded and read successfully", "status": True}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# Reviews route
@app.route("/reviews", methods=['GET'])
def get_reviews():
    page = int(request.args.get('page', 1))
    category = request.args.get('category', None)
    reviews = fetch_reviews(conn,category=category,page=page)
    if reviews.empty:
        return jsonify({"error": "No reviews found"}), 400
    result = []
    for _, review in reviews.iterrows():
        metadata = " | ".join(filter(None, [
            f"Device: {review.get('device', '')}" if review.get('device') else None,
            f"App version: {review.get('app_version_code', '')}" if review.get('app_version_code') else None,
            f"Android SDK: {review.get('android_os_version', '')}" if review.get('android_os_version') else None]))
        print(review['author_name'])
        
        review_data = {
            "review_id": review['ID'], 
            "author_name": review['author_name'],
            "comment": review['comment'],
            "date": review['date'],
            "star_rating": review['star_rating'],
            "device": review['device'],
            "android_os_version": review['android_os_version'],
            "app_version_code": review['app_version_code'],
            "device_metadata": review.get('device_metadata', metadata)  
        }
    
        result.append(review_data)
    
    return jsonify(result)


@app.route('/publish-reply', methods=['POST'])
def publish_reply():
    data = request.get_json()
    review_id = data.get('review_id')
    reply_text = data.get('reply')

    result = publish_reply_to_review(review_id, reply_text)

    if 'error' in result:
        return jsonify(result), 500

    return jsonify(result)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)